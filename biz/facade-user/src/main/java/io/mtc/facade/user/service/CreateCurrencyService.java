package io.mtc.facade.user.service;

import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.dto.CreateCurrencyDTO;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.feign.ServiceCurrency;
import io.mtc.facade.user.repository.BillRepository;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 发币
 *
 * @author Chinhin
 * 2018/8/15
 */
@Service
public class CreateCurrencyService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private BillRepository billRepository;

    @Value("${create-contract-fee}")
    private BigInteger createContractFee;

    @Resource
    private ServiceCurrency serviceCurrency;

    @Resource
    private EthRedisUtil ethRedisUtil;

    /**
     * 获取所有分类
     * @return 所有分类
     */
    public Object allCategory() {
        Object result = ethRedisUtil.get(RedisKeys.CURRENCY_ALL_CATEGORIES);
        if (result == null) {
            result = CommonUtil.jsonToMap(serviceCurrency.allCategory());
            ethRedisUtil.set(RedisKeys.CURRENCY_ALL_CATEGORIES, result, 30);
        }
        return result;
    }

    /**
     * 获取创建合约的手续费
     * @return 手续费
     */
    public Object createContractFee() {
        Object result = ethRedisUtil.get(RedisKeys.CREATE_TOKEN_FEE_OBJ);
        if (result == null) {
            BigDecimal mtcPrice = ethRedisUtil.get(RedisKeys.ETH_TOKEN_PRICE(Constants.MTC_ADDRESS), BigDecimal.class);
            BigDecimal mtcPriceCny = ethRedisUtil.get(RedisKeys.ETH_TOKEN_CNY_PRICE(Constants.MTC_ADDRESS), BigDecimal.class);
            if (mtcPrice == null || mtcPriceCny == null) {
                return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("amount", createContractFee);
            BigDecimal amount = CommonUtil.getFormatAmount(createContractFee.toString());
            resultMap.put("money", amount.multiply(mtcPrice));
            resultMap.put("moneyCny", amount.multiply(mtcPriceCny));
            ethRedisUtil.set(RedisKeys.CREATE_TOKEN_FEE_OBJ, resultMap, 300);
            return ResultUtil.successObj(resultMap);
        }
        return ResultUtil.successObj(result);
    }

    /**
     * 创建代币
     * @param createCurrencyDTO 代币封装类
     * @return 结果
     */
    public Object createCurrency(Long uid, CreateCurrencyDTO createCurrencyDTO) {
        // 余额check
        User user = userRepository.findById(uid).get();
        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, Constants.MTC_ADDRESS, 1);
        // 余额为0
        if (userBalance == null) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }
        if (createCurrencyDTO.getCategoryId() == null || createCurrencyDTO.getCategoryId() == 0) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 可用余额
        BigInteger availableBalance = userBalance.getBalance().subtract(userBalance.getFreezingAmount());
        // 余额不足
        if (availableBalance.compareTo(createContractFee) < 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }

        // 创建createCurrency记录
        createCurrencyDTO.setUid(uid);
        Map<String, Object> result = CommonUtil.jsonToMap(serviceCurrency.createCurrency(createCurrencyDTO));
        int status = (int) result.get("status");
        if (status == 200) {
            // 减余额
            userBalance.setBalance(userBalance.getBalance().subtract(createContractFee));
            userBalanceRepository.save(userBalance);

            // 创建账单
            Bill bill = new Bill();
            bill.setOutcome(createContractFee);
            bill.setStatus(BillStatus.SUCCESS);
            bill.setType(BillType.CREATE_CONTRACT);
            bill.setBalance(userBalance);
            bill.setCurrentBalance(userBalance.getBalance());
            bill.setRelatedAddress(createCurrencyDTO.getOwnerAddress());
            bill.setNote(createCurrencyDTO.getName());
            String resultStr = result.get("result").toString();
            bill.setRelativeId(Long.valueOf(resultStr));
            billRepository.save(bill);

            return ResultUtil.successObj();
        }
        return result;
    }

    /**
     * 分页查看发币记录
     * @param uid 用户id
     * @param categoryId 分类id
     */
    public Object list(Long uid, Long categoryId,
                       Integer pageNumber, Integer pageSize, String order, String sort) {
        return CommonUtil.jsonToMap(serviceCurrency.select(uid, categoryId, pageNumber, pageSize, order, sort));
    }

}
