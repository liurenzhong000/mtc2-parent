package io.mtc.facade.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.*;
import io.mtc.facade.user.bean.EosFee;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.repository.BillRepository;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserRepository;
import io.mtc.facade.user.util.EosEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.utils.Convert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * user的service
 *
 * @author Chinhin
 * 2018/7/24
 */
@Slf4j
@Service
public class EosService {

    // 获取创建eos手续费的链接
    @Value("${eos-fee-url}")
    private String eosFeeUrl;

    // 创建eos的链接
    @Value("${create-eos-url}")
    private String createEosUrl;

    @Resource
    private RateCacheUtil rateCacheUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private BillRepository billRepository;

    @Resource
    private FundService fundService;

    /**
     * 获取创建eos的手续费
     * @param uid 用户id
     * @return 结果
     */
    public Object createEosAccountFee(Long uid) {
        EosFee eosFee = getEosFee();
        if (eosFee == null) {
            return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
        }

        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        UserBalance eth = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.ETH_ADDRESS, 1);
        if (eth != null) {
            eosFee.setEthBalance(eth.getBalance());
        }

        UserBalance mtc = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.MTC_ADDRESS, 1);
        if (mtc != null) {
            eosFee.setMtcBalance(mtc.getBalance());
        }

        UserBalance eos = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.EOS_ADDRESS, 3);
        if (eos != null) {
            eosFee.setEosBalance(eos.getBalance());
        }
        return ResultUtil.successObj(eosFee);
    }

    private EosFee getEosFee() {

        EosFee result = redisUtil.get(RedisKeys.CREATE_EOS_FEE, EosFee.class);
        if (result == null) {
            result = new EosFee();

            String string = HttpUtil.get(eosFeeUrl);
            if (StringUtil.isBlank(string)) {
                return null;
            }
            JSONObject json = JSON.parseObject(string);
            BigDecimal totalCost = json.getBigDecimal("totalCost");
            BigInteger eosCost = json.getBigInteger("eosCost");

            // 美元人民币汇率
            BigDecimal rate = rateCacheUtil.getUSD2CNY();

            result.setMoney(totalCost);
            result.setMoneyCny(totalCost.multiply(rate));
            result.setNeedEos(eosCost);

            // mtc价格
            BigDecimal mtcPrice = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(Constants.MTC_ADDRESS), BigDecimal.class);
            BigDecimal tempMtc = Convert.toWei(totalCost, Convert.Unit.MWEI).divide(mtcPrice, RoundingMode.UP); // 这样处理只是为了好看，后面都是0，而不是乱乱的数字
            // 需要mtc个数
            result.setNeedMtc(Convert.toWei(tempMtc, Convert.Unit.SZABO).toBigInteger());

            // ether价格
            BigDecimal ethPrice = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(Constants.ETH_ADDRESS), BigDecimal.class);
            BigDecimal tempEth = Convert.toWei(totalCost, Convert.Unit.MWEI).divide(ethPrice, RoundingMode.UP);
            // 需要ether的个数
            result.setNeedEth(Convert.toWei(tempEth, Convert.Unit.SZABO).toBigInteger());

            redisUtil.set(RedisKeys.CREATE_EOS_FEE, result, 1800);
        }
        return result;
    }

    /**
     * 创建eos账户
     * @param payType 支付方式(1:MTC, 2:ETH, 3:BCH, 4:EOS)
     */
    public Object createEosAccount(Long uid, Integer payType, String accountName, String ownerKey, String activeKey,
                                   String fundPassword, Boolean isValidByPhone, String validCode) {
        User user = userRepository.findById(uid).get();
        Object errorInfo = fundService.userVerify(user, fundPassword, isValidByPhone, validCode);
        if (errorInfo != null) {
            return errorInfo;
        }
        EosFee eosFee = getEosFee();
        if (eosFee == null) {
            return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
        }
        UserBalance userBalance;
        BigInteger fee;
        if (payType == 1) { // mtc支付
            fee = eosFee.getNeedMtc();
            userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.MTC_ADDRESS, 1);
        } else if (payType == 2) { // eth支付
            fee = eosFee.getNeedEth();
            userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.ETH_ADDRESS, 3);
        } else if (payType == 4) { // EOS
            fee = eosFee.getNeedEos();
            userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, Constants.EOS_ADDRESS, 3);
        } else {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 余额不足
        if (userBalance.getBalance().compareTo(fee) < 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }
        // 请求eos的service创建账户
        Map<String, String> params = new HashMap<>();
        params.put("accountName", accountName);
        params.put("ownerKey", ownerKey);
        params.put("activeKey", activeKey);
        String requestTime = String.valueOf(System.currentTimeMillis());
        params.put("requestTimeMills", requestTime);
        String signature = EosEncryptUtil.getSignature(requestTime, accountName, ownerKey, activeKey);
        params.put("signature", signature);

        String encryptParams = AesCBC.getInstance().simpleEncrypt(CommonUtil.toJson(params), Constants.EOS_SERVER_SECRET);
        if (encryptParams == null) {
            return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
        }
        String requestResult = HttpUtil.post(createEosUrl, encryptParams);
        log.info("time:{}, msg:{}", DateUtil.formatStandardDate(new Date()), requestResult);
        if (requestResult == null) {
            return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
        }
        JSONObject result = JSONObject.parseObject(requestResult);
        Integer statusCode = result.getInteger("StatusCode");
        if (statusCode != 200) {
            return ResultUtil.successObj(result);
        }

        // 扣除余额
        userBalance.setBalance(userBalance.getBalance().subtract(fee));
        userBalanceRepository.save(userBalance);

        // 账单入库
        Bill bill = new Bill();
        bill.setOutcome(fee);
        bill.setStatus(BillStatus.SUCCESS);
        bill.setType(BillType.CREATE_EOS);
        bill.setBalance(userBalance);
        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        return ResultUtil.successObj(result);
    }

}
