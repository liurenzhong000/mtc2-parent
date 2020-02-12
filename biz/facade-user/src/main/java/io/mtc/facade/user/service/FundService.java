package io.mtc.facade.user.service;
import io.mtc.common.dto.CurrencyBean;
import io.mtc.facade.user.constants.PlatformTransferStatus;
import java.util.Date;
import io.mtc.facade.user.constants.PlatformTransferType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.constants.MTCError;
import io.mtc.common.constants.TransactionConstants;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.dto.RequestResult;
import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.*;
import io.mtc.common.web3j.util.MeshTransactionData;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.*;
import io.mtc.facade.user.feign.FacadeBitcoin;
import io.mtc.facade.user.feign.ServiceCurrency;
import io.mtc.facade.user.feign.ServiceEndpointEth;
import io.mtc.facade.user.repository.*;
import io.mtc.facade.user.util.PackageUtil;
import io.mtc.facade.user.util.wallet.EthCreateWalletUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资金service
 *
 * @author Chinhin
 * 2018/7/27
 */
@Slf4j
@Service
@Transactional
public class FundService implements MsgHandler {

    @Resource
    private BillRepository billRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private UserWalletRepository userWalletRepository;

    @Resource
    private UserRepository userRepository;

    @Resource(name = "ethTransPendingProducer")
    private Producer producer;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private ServiceCurrency serviceCurrency;

    @Resource
    private RedisUtil redisUtil;

    @Value("${deposit-address}")
    private String depositAddress;

    @Value("${deposit-path}")
    private String keyStorePath;

    @Value("${deposit-password}")
    private String keyStorePassword;

    @Value("${fee-address}")
    private String feeAddress;

    @Value("${fee-path}")
    private String feeKeyStorePath;

    @Value("${fee-password}")
    private String feeKeyStorePassword;

    @Resource
    private UserService userService;

    @Resource
    private SendCodeService sendCodeService;

    @Resource
    private DepositWithdrawService depositWithdrawService;

    @Resource
    private BalanceService balanceService;

    @Resource
    private FacadeBitcoin facadeBitcoin;

    @Resource
    private PlatformTransferRepository platformTransferRepository;

    @Resource
    private PlatformTransferService platformTransferService;

    /**
     * 获取单个代币的托管账户的余额
     * @param uid 用户id
     * @param currencyAddress 代币地址
     * @return 结果
     */
    public Object currencyBalance(Long uid, String currencyAddress, Integer currencyType) {
        User user = userRepository.findById(uid).get();
        UserBalance balance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);
        if (balance == null) {
            return ResultUtil.successObj(BigInteger.ZERO);
        }
        return ResultUtil.successObj(balance.getBalance());
    }

    /**
     * 获取所有托管代币的余额
     *
     * @param uid 用户id
     * @return 结果
     */
    @Transactional(readOnly = true)
    public Object allCurrency(Long uid) {
        User user = userRepository.findById(uid).get();

        Map<String, Object> balanceMap = new HashMap<>();
        user.getBalances().forEach(it -> {
            balanceMap.put(it.getCurrencyAddress(), it.getBalance());
        });

        JSONArray currencyList = getCurrencyList();
        for (Object tempObj : currencyList) {
            JSONObject temp = (JSONObject) tempObj;
            temp.remove("createTime");
            temp.remove("fee");
            temp.remove("hostEnabled");
            temp.remove("id");
            temp.remove("isDefaultVisible");
            temp.remove("isEnabled");
            temp.remove("redPacketEnabled");
            temp.remove("sourceType");
            temp.remove("updateTime");
            BigInteger balance = (BigInteger) balanceMap.get(temp.getString("address"));
            if (balance == null) {
                temp.put("money", 0);
                temp.put("cnyMoney", 0);
                temp.put("balance", 0);
                continue;
            }
            temp.put("balance", balance);
            BigDecimal balanceDeci = CommonUtil.getFormatAmount(balance.toString());

            // 币种价格
            String tokenAddress = temp.getString("address");
            BigDecimal cnyPrice = redisUtil.get(RedisKeys.ETH_TOKEN_CNY_PRICE(tokenAddress), BigDecimal.class);
            if (cnyPrice == null) {
                cnyPrice = temp.getBigDecimal("cnyPrice");
            }
            BigDecimal price = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(tokenAddress), BigDecimal.class);
            if (price == null) {
                price = temp.getBigDecimal("price");
            }
            temp.put("cnyPrice", cnyPrice);
            temp.put("price", price);

            // 币种金额
            BigDecimal cnyMoney = cnyPrice.multiply(balanceDeci);
            BigDecimal money = price.multiply(balanceDeci);
            temp.put("money", NumberUtil.scale2(money));
            temp.put("cnyMoney", NumberUtil.scale2(cnyMoney));
        }
        return ResultUtil.successObj(currencyList);
    }

    /**
     * 获取币种一览
     * @return 币种一览
     */
    private JSONArray getCurrencyList() {
        Map result;
        // 币种一览
        Object listObj = redisUtil.get(RedisKeys.ENABLE_HOST_CURRENCY);
        if (listObj != null) {
            result = (Map) listObj;
        } else {
            String s = serviceCurrency.hostEnableCurrency();
            result = CommonUtil.fromJson(s, HashMap.class);
            // 60秒钟刷新一次缓存
            redisUtil.set(RedisKeys.ENABLE_HOST_CURRENCY, result, 60);
        }
        return (JSONArray) result.get("result");
    }

    /**
     * 托管代币的账单
     * @param uid 用户id
     * @param currencyAddress 代币地址
     * @param yearMonth 过滤时间，格式为yyyy/MM(可不传)
     * @param pageModel 翻页信息
     * @return 结果
     */
    @Transactional(readOnly = true)
    public Object bills(Long uid, String currencyAddress, Integer currencyType, String yearMonth, PagingModel pageModel) {
        User user = userRepository.findById(uid).get();
        UserBalance balanceBean = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);
        // 设置默认排序
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setOrder("DESC");
            pageModel.setSort("createTime");
        }
        Page<Bill> allByBalance;
        if (StringUtil.isBlank(yearMonth)) {
            allByBalance = billRepository.findAllByBalance(balanceBean, pageModel.make());
        } else {
            Date date = DateUtil.parseDate(yearMonth, "yyyy/MM");
            allByBalance = billRepository.findAllByBalanceAndCreateTimeBetween(balanceBean,
                    date, DateUtil.getFirstDayOfNextMonth(date), pageModel.make());
        }
        return JSON.parseObject(PagingResultUtil.list(allByBalance));
    }

    /**
     * 更新充值的订单
     * @param uid 用户id
     * @param billId 账单id
     * @param txHash 交易hash
     * @param isSuccess 打包是否成功
     * @return 结果
     */
    public Object depositCompensate(Long uid, Long billId, String txHash, boolean isSuccess) {
        Bill bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        if (!bill.getBalance().getUser().getId().equals(uid)) {
            return ResultUtil.errorObj(MTCError.BILL_UPDATE_NO_AUTH);
        }
        if (bill.getTxHash() != null) {
            return ResultUtil.errorObj(MTCError.BILL_UPDATE_NO_AUTH);
        }
        Bill checkBill = billRepository.findByTxHashAndCurrencyTypeAndType(txHash, bill.getCurrencyType(), bill.getType());
        if (checkBill != null) {
            return ResultUtil.errorObj("该交易hash已使用", 500);
        }
        bill.setTxHash(txHash);
        if (!isSuccess) {
            bill.setStatus(BillStatus.FAILURE);
        } else {
            String error = depositWithdrawService.validateBitcoinDeposit(bill);
            if (error != null) {
                bill.setTxHash(null);
                return ResultUtil.error(error, 500);
            }
        }
        billRepository.save(bill);
        return ResultUtil.successObj();
    }

    /**
     * 充值
     */
    public Object deposit(Long uid, Integer currencyType, String toAddress, String currencyAddress,
                          BigInteger nonce, BigInteger income, String fromAddress) {
        // 不是充值给平台的交易
        if (!toAddress.equals(depositAddress) && currencyType == 1) {
            return ResultUtil.errorObj(MTCError.DEPOSIT_DEST_ERROR);
        }

        User user = userRepository.findById(uid).get();
        UserBalance balanceBean = balanceService.addBalance(user, currencyType, currencyAddress, BigInteger.ZERO);
        Bill bill = new Bill();
        bill.setTxNonce(nonce);
        bill.setBalance(balanceBean);
        bill.setCurrentBalance(balanceBean.getBalance());
        bill.setIncome(income);
        bill.setStatus(BillStatus.PROCESSING);
        bill.setType(BillType.DEPOSIT);
        bill.setCurrencyType(currencyType);
        bill.setRelatedAddress(fromAddress);
        bill.setTxHash(null);
        billRepository.save(bill);

        return ResultUtil.successObj(bill.getId());
    }

    @Override
    public Action doConsume(String json) {
        log.info("完成交易:{}", json);
        EthTransObj transInfo = CommonUtil.fromJson(json, EthTransObj.class);
        if (BillType.DEPOSIT.getKey().equals(transInfo.getTxType())) { // 充值
            depositWithdrawService.completeDeposit(transInfo);
        } else if (BillType.WITHDRAW.getKey().equals(transInfo.getTxType())) { // 提现
            depositWithdrawService.completeWithdraw(transInfo);
        } else if (transInfo.getTxType().equals(EthTransObj.TxType.FEE.ordinal())) {//转入手续费
            //更新平台记录状态
            platformTransferService.completeFeeToUser(transInfo);
        } else if (transInfo.getTxType().equals(EthTransObj.TxType.TO_MAIN.ordinal())){//转入手续费
            platformTransferService.completeUserToMain(transInfo);
        }
        return Action.CommitMessage;
    }

    /**
     * 提现手续费
     * @param currencyAddress 代币地址
     * @return 结果
     */
    public Object withdrawFee(String currencyAddress, Integer currencyType) {
        return ResultUtil.successObj(getWithDrawFeeOfCurrency(currencyAddress, currencyType));
    }

    /**
     * 获取代币的手续费
     * @param currencyAddress 代币地址
     * @return 对应需要扣除多少代币
     */
    private BigInteger getWithDrawFeeOfCurrency(String currencyAddress, Integer currencyType) {
        if (currencyType == 1) {
//            return getEthWithdrawFee(currencyAddress);
            return getWithdrawFeeFixation(currencyAddress);
        // 比特币
        } else if (currencyType == 4) {
            return CommonUtil.btc2wei(io.mtc.common.constants.Constants.BTC_WITHDRAW_FEE);
        } else if (currencyType == 5) {//usdt
            return getWithdrawFeeFixation(io.mtc.common.constants.Constants.USDT_CURRENCY_ADDRESS);
        } else {
            return BigInteger.ZERO;
        }
    }

    //获取eth币系的提现手续费
    private BigInteger getEthWithdrawFee(String currencyAddress) {
        BigInteger fee = redisUtil.get(RedisKeys.WITHDRAW_FEE(currencyAddress), BigInteger.class);
        if (fee == null) {
            // 实际使用的gasPrice 比需要的gasPrice高 10%
            BigInteger useGasPrice = TransactionConstants.getUseGasPrice(serviceEndpointEth.gasPrice());
            // 需要的手续费金额
            BigInteger total = useGasPrice.multiply(TransactionConstants.GAS_AMOUNT);
            fee = serviceCurrency.ether2currency(currencyAddress, total);
            redisUtil.set(RedisKeys.WITHDRAW_FEE(currencyAddress), fee, 30);
        }
        return fee;
    }

    //根据后台的设置来计算需要的手续费
    private BigInteger getWithdrawFeeFixation(String currencyAddress) {
        BigInteger fee = redisUtil.get(RedisKeys.WITHDRAW_FEE(currencyAddress), BigInteger.class);
        if (fee == null) {
            fee = serviceCurrency.getWithdrawFee(currencyAddress);
            redisUtil.set(RedisKeys.WITHDRAW_FEE(currencyAddress), fee, 30);
        }
        return fee;
    }

    /**
     * 提现
     * @param uid 用户id
     * @param walletAddress 提现到的钱包地址
     * @param amountStr 提现金额
     * @return 结果
     */

    public Object withdraw(Long uid, String currencyAddress, Integer currencyType, String walletAddress, String amountStr,
                           String fundPassword, Boolean isValidByPhone, String validCode) {
        User user = userRepository.findById(uid).get();
        Object errorInfo = userVerify(user, fundPassword, isValidByPhone, validCode);
        if (errorInfo != null) {
            return errorInfo;
        }
        // 可以提现时间 是否 晚于现在时间
        if (user.getCanWithdrawTime() != null && user.getCanWithdrawTime().after(new Date())) {
            return ResultUtil.errorObj(MTCError.CAN_WITHDRAW_TIME_NOT_REACH);
        }
        // 提现手续费计算
        BigInteger fee = getWithDrawFeeOfCurrency(currencyAddress, currencyType);
        BigInteger amount = NumberUtil.toBigInteger(amountStr);
        if (amount.compareTo(BigInteger.ZERO) < 0) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }

        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);

        // 提现金额 最多为 (余额 - 冻结金额 - 手续费)
        BigInteger freezingAmount = userBalance.getFreezingAmount();
        BigInteger needLessThan = userBalance.getBalance().subtract(freezingAmount).subtract(fee);

        // 提现金额 大于最多能提现的金额
        if (amount.compareTo(needLessThan) > 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }

        Bill bill = new Bill();
        bill.setOutcome(amount.add(fee));
        bill.setOutComeFee(fee);
        bill.setStatus(BillStatus.WAIT_AUDIT);
        bill.setType(BillType.WITHDRAW);
        bill.setCurrencyType(currencyType);
        // 提现地址
        bill.setRelatedAddress(walletAddress);
        bill.setBalance(userBalance);
        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        // 增加冻结金额
        userBalance.setFreezingAmount(freezingAmount.add(bill.getOutcome()));
        userBalanceRepository.save(userBalance);
        return ResultUtil.successObj();
    }


    /**
     * 提现
     * @param walletAddress 提现到的钱包地址
     * @param amountStr 提现金额
     * @return 结果
     */
    public Object withdrawAIP(Integer currencyType, Long uid, String walletAddress, String amountStr, String fundPassword) {

        String currencyAddress = "0xbac2185ebe654960aed653296376c906eedac3be";
        //        0xbac2185ebe654960aed653296376c906eedac3be
        User user = userRepository.findById(uid).get();
        Object errorInfo = userVerifyAIP(user, fundPassword);
        if (errorInfo != null) {
            // return errorInfo;
        }
        // 提现手续费计算
        BigInteger fee = getWithDrawFeeOfCurrency(currencyAddress, currencyType);
//        BigInteger amount = NumberUtil.toBigInteger(amountStr);
        
        BigInteger ONE_ETHER = new BigInteger("1000000000000000000");
        BigInteger amount = NumberUtil.toBigInteger(amountStr).multiply(ONE_ETHER);

        if (amount.compareTo(BigInteger.ZERO) < 0) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }

        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);

        // 提现金额 最多为 (余额 - 冻结金额 - 手续费)
        BigInteger freezingAmount = userBalance.getFreezingAmount();
        BigInteger needLessThan = userBalance.getBalance().subtract(freezingAmount).subtract(fee);

        // 提现金额 大于最多能提现的金额
        if (amount.compareTo(needLessThan) > 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }

        Bill bill = new Bill();
        bill.setOutcome(amount.add(fee));
        bill.setOutComeFee(fee);
        bill.setStatus(BillStatus.PENDING);
        bill.setType(BillType.WITHDRAW);
        // 提现地址
        bill.setRelatedAddress(walletAddress);
        bill.setBalance(userBalance);
        bill.setCurrencyType(currencyType);
        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        // 增加冻结金额
        userBalance.setFreezingAmount(freezingAmount.add(bill.getOutcome()));
        userBalanceRepository.save(userBalance);
        return ResultUtil.success(String.valueOf(bill.getId()));
        // return ResultUtil.successObj();
    }

    /**
     * 验证用户资金密码是否正确
     * @param uid 用户id
     * @param fundPassword 资金密码
     * @return 结果
     */
    public Object fundPasswordVerify(Long uid, String fundPassword) {
        User user = userRepository.findById(uid).get();
        if (!user.getFundPassword().equals(CodecUtil.digestStrSHA1(fundPassword))) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_ERROR);
        }
        return ResultUtil.success();
    }

    /**
     * 提现或转账的身份验证
     * @param user 用户
     * @param fundPassword 资金密码
     * @param isValidByPhone 是否用手机号验证
     * @param validCode 验证码
     * @return 验证没问题返回null
     */
    Object userVerify(User user, String fundPassword, Boolean isValidByPhone, String validCode) {
        if (StringUtil.isBlank(user.getFundPassword())) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_NOT_EXIST);
        }
        if (StringUtil.isBlank(fundPassword)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        if (!user.getFundPassword().equals(CodecUtil.digestStrSHA1(fundPassword))) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_ERROR);
        }
        // 身份验证 失败
        if (!(isValidByPhone && sendCodeService.checkAndDel(user.getPhone(), validCode))
                && !(!isValidByPhone && sendCodeService.emailCheckAndDel(user.getEmail(), validCode))) {
            return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
        }
        return null;
    }

    /**
     * 提现或转账的身份验证
     * @param user 用户
     * @param fundPassword 资金密码

     * @return 验证没问题返回null
     */
    Object userVerifyAIP(User user, String fundPassword) {
        if (StringUtil.isBlank(user.getFundPassword())) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_NOT_EXIST);
        }
        if (StringUtil.isBlank(fundPassword)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        if (!user.getFundPassword().equals(CodecUtil.digestStrSHA1(fundPassword))) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_ERROR);
        }

        // 可以提现时间 是否 晚于现在时间
        if (user.getCanWithdrawTime() != null && user.getCanWithdrawTime().after(new Date())) {
            return ResultUtil.errorObj(MTCError.CAN_WITHDRAW_TIME_NOT_REACH);
        }
        return null;
    }

    /**
     * 转账
     * @param uid 用户id
     * @param currencyAddress 代币地址
     * @param target 目标手机号或邮箱地址
     * @param amountStr 金额字符串
     * @return 结果
     */
    public Object transfer(Long uid, String currencyAddress, Integer currencyType, String target, String amountStr,
                           String fundPassword) { // , Boolean isValidByPhone, String validCode
        log.info("托管账户转账 {}", amountStr);
        User user = userRepository.findById(uid).get();
        Object errorInfo = userVerifyAIP(user, fundPassword);
        if (errorInfo != null) {
            return errorInfo;
        }
        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);

        BigInteger amount = NumberUtil.toBigInteger(amountStr);
        if (amount.compareTo(BigInteger.ONE) < 0) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 转账金额 最多为 (余额 - 冻结金额)
        BigInteger freezingAmount = userBalance.getFreezingAmount();
        BigInteger needLessThan = userBalance.getBalance().subtract(freezingAmount);
        // 转账金额 大于最多能转账的金额
        if (amount.compareTo(needLessThan) > 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }

        // 收款方
        User toUser = userService.findUser(target);
        if (toUser == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        // 自己转给自己
        if (toUser.getId().equals(user.getId())) {
            return ResultUtil.errorObj(MTCError.TRANSFER_TARGET_USER_CANT_BE_SELF);
        }

        // 更新转出方余额
        userBalance.setBalance(userBalance.getBalance().subtract(amount));
        userBalanceRepository.save(userBalance);
        // 增加转出方账单
        Bill fromBill = new Bill();
        fromBill.setOutcome(amount);
        fromBill.setStatus(BillStatus.SUCCESS);
        fromBill.setType(BillType.TRANSFER_FROM);
        fromBill.setNote(toUser.getUserName());
        fromBill.setNote2(toUser.getPhoto());
        fromBill.setBalance(userBalance);
        fromBill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(fromBill);

        // 收款方更新余额
        UserBalance toUserBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                toUser, currencyAddress, currencyType);
        if (toUserBalance == null) {
            toUserBalance = new UserBalance();
            toUserBalance.setWalletAddress(userBalance.getWalletAddress());
            toUserBalance.setCurrencyType(userBalance.getCurrencyType());
            toUserBalance.setCurrencyAddress(currencyAddress);
            toUserBalance.setUser(toUser);
        }
        toUserBalance.setBalance(toUserBalance.getBalance().add(amount));

        // 增加收款方收款账单
        Bill toBill = new Bill();
        toBill.setIncome(amount);
        toBill.setStatus(BillStatus.SUCCESS);
        toBill.setType(BillType.TRANSFER_TO);
        toBill.setNote(user.getUserName());
        toBill.setNote2(user.getPhoto());
        toBill.setCurrentBalance(toUserBalance.getBalance());
        toBill.setBalance(toUserBalance);
        billRepository.save(toBill);

        userService.addWheelNum(user, true);
        return ResultUtil.successObj();
    }

    /**
     * 等待打包的提现交易消费（只是BTC）
     */
    public void btcPendingTransConsume() {
        Bill bill = billRepository.findTopByStatusAndTypeAndCurrencyTypeOrderByIdAsc(
                BillStatus.PENDING, BillType.WITHDRAW, 4);
        if (bill == null) {
            return;
        }
        // 获取锁失败则直接返回
        if (!redisUtil.distributeLock(RedisKeys.BTC_PENDING_WITHDRAW_PROCESS, 20)) {
            return;
        }
        try {
            log.info("准备发起提现： {}", CommonUtil.toJson(bill));
            BigInteger withdrawAmount = bill.getOutcome().subtract(bill.getOutComeFee());
            RequestResult result = facadeBitcoin.withdraw(BitcoinTypeEnum.BTC, bill.getRelatedAddress(), bill.getId(),
                    CommonUtil.wei2btc(withdrawAmount).toPlainString());
            log.info("调用facadeBitcoin申请提现BTC成功： {}", CommonUtil.toJson(result));
            // 成功
            if (result.getIsSuccess()) {
                bill.setTxHash(result.getAdditionInfo().toString());

                EthTransObj transInfo = new EthTransObj();
                transInfo.setTxId(bill.getId());
                transInfo.setStatus(1);
                depositWithdrawService.completeWithdraw(transInfo);
                log.info("完成账单-提现BTC成功： {}", bill.getId());

                billRepository.save(bill);
            }else {
                EthTransObj transInfo = new EthTransObj();
                transInfo.setTxId(bill.getId());
                transInfo.setStatus(2);
                depositWithdrawService.completeWithdraw(transInfo);
                log.info("完成账单-提现BTC失败： {}", bill.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redisUtil.delete(RedisKeys.PENDING_WITHDRAW_PROCESS);
        }

    }

    /**
     * 等待打包的提现交易消费（只是USDT）
     */
    public void usdtPendingTransConsume() {
        Bill bill = billRepository.findTopByStatusAndTypeAndCurrencyTypeOrderByIdAsc(
                BillStatus.PENDING, BillType.WITHDRAW, 5);
        if (bill == null || StringUtils.isNotBlank(bill.getTxHash())) {//有hash也不行
            return;
        }

        // 获取锁失败则直接返回
        if (!redisUtil.distributeLock(RedisKeys.USDT_PENDING_WITHDRAW_PROCESS, 20)) {
            return;
        }

        try {
            log.info("准备发起提现： {}", CommonUtil.toJson(bill));
            BigInteger withdrawAmount = bill.getOutcome().subtract(bill.getOutComeFee());
            RequestResult result;
            try {
                result = facadeBitcoin.withdraw(BitcoinTypeEnum.USDT, bill.getRelatedAddress(), bill.getId(),
                        CommonUtil.wei2btc(withdrawAmount).toPlainString());
            } catch (Exception e) {
                result = new RequestResult(false, "服务调用请求失败", null);
            }

            log.info("调用facadeBitcoin申请提现USDT成功： {}", CommonUtil.toJson(result));
            // 成功
            if (result.getIsSuccess()) {
                bill.setTxHash(result.getAdditionInfo().toString());
                EthTransObj transInfo = new EthTransObj();
                transInfo.setTxId(bill.getId());
                transInfo.setStatus(1);
                depositWithdrawService.completeWithdraw(transInfo);
                log.info("完成账单-提现USDT成功： {}", bill.getId());
                billRepository.save(bill);
            } else {
                EthTransObj transInfo = new EthTransObj();
                transInfo.setTxId(bill.getId());
                transInfo.setStatus(2);
                depositWithdrawService.completeWithdraw(transInfo);
                log.info("完成账单-提现USDT失败： {}", bill.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redisUtil.delete(RedisKeys.PENDING_WITHDRAW_PROCESS);
        }
    }

    /**
     * 等待打包的提现交易消费（只是以太坊）
     */
    public void pendingTransConsume() {
        Bill bill = billRepository.findTopByStatusAndTypeAndCurrencyTypeOrderByIdAsc(
                BillStatus.PENDING, BillType.WITHDRAW, 1);
        if (bill == null) {
            return;
        }
        // 获取锁失败则直接返回
        if (!redisUtil.distributeLock(RedisKeys.PENDING_WITHDRAW_PROCESS, 20)) {
            return;
        }
        try {
            // 是否有还未过期的处理中的提现记录
            Set<Bill> pending = billRepository.findAllByStatusAndTypeAndCurrencyTypeAndWithdrawExpireTimeGreaterThan(
                    BillStatus.PROCESSING,
                    BillType.WITHDRAW,
                    1,
                    System.currentTimeMillis()
            );
            // 有正在提现的交易，则不做处理
            if (pending.size() > 0) {
                return;
            }
            log.info("开始提现 {}", bill.getId());
            bill.setStatus(BillStatus.PROCESSING);

            BigInteger[] gasPriceAndNonce = serviceEndpointEth.getGasPriceAndNonce(depositAddress);

            String currencyAddress = bill.getBalance().getCurrencyAddress();

            String signedTransactionData;

            // ############ 余额修正 ##############
            Integer balanceDecimals = redisUtil.get(RedisKeys.DECIMALS_TOKEN(currencyAddress), Integer.class);
            if (balanceDecimals == null) {
                balanceDecimals = serviceEndpointEth.getBalanceDecimals(currencyAddress);
            }
            BigInteger outcome = bill.getOutcome().subtract(bill.getOutComeFee());
            outcome = CommonUtil.balanceUnCorrect(outcome, balanceDecimals);
            // ############ 余额修正 ##############

            // 以太坊交易
            if (io.mtc.common.constants.Constants.ETH_ADDRESS.equals(currencyAddress)) {
                signedTransactionData = PackageUtil.packageEther(gasPriceAndNonce, outcome,
                        bill.getRelatedAddress(), keyStorePath, keyStorePassword);
            } else {
                signedTransactionData = PackageUtil.packageCurrency(gasPriceAndNonce, outcome,
                        bill.getRelatedAddress(), currencyAddress, keyStorePath, keyStorePassword);
            }

            MeshTransactionData transactionData = MeshTransactionData.from(signedTransactionData);

            // 签名失败
            if (transactionData.txHash == null) {
                bill.setStatus(BillStatus.FAILURE);
                billRepository.save(bill);
                return;
            }

            bill.setTxHash(transactionData.txHash);
            bill.setTxNonce(transactionData.nonce);
            // 设置本次提现过期时间为10分钟后
            bill.setWithdrawExpireTime(System.currentTimeMillis() + 1000 * 60 * 10);
            // 打包请求
            EthTransObj request = new EthTransObj();
            request.setSignedTransactionData(signedTransactionData);
            // 类型：平台提现的记录
            request.setTxType(2);
            request.setTxId(bill.getId());
            request.setCurrencyAddress(bill.getBalance().getCurrencyAddress());

            boolean sendResult = producer.send(
                    Constants.Topic.MTC_BIZ_TRANS,
                    Constants.Tag.ETH_BIZ_TRANS_PENDING,
                    request,
                    Constants.Tag.ETH_BIZ_TRANS_PENDING.name() + bill.getId());

            if (sendResult) {
                billRepository.save(bill);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redisUtil.delete(RedisKeys.PENDING_WITHDRAW_PROCESS);
        }
    }

    /***
     * 给本地托管用户转入eth手续费
     * 1.从数据库中获取支持托管的币种
     * 2.从用户托管钱包中获取所有托管用户的eth钱包汇总
     * 3.循环遍历钱包地址，查询支持的币种的余额，如果达到汇总阈值，累计计算手续费
     * 4.判断地址的eth余额和需要的手续费进行对比，如果不足，计算需要转入的手续费
     * 5.打包交易，发送mq消息，给到service-endpoint-eth进行打包进行处理
     * 6.生成平台内转账记录
     */
    public void ethFeeToHostUser() {
        //获取支持托管的币种
        JSONArray hostEnableCurrencyArray = getCurrencyList();
        log.info("hostEnableCurrencyArray={}", JSON.toJSONString(hostEnableCurrencyArray));
        if (hostEnableCurrencyArray == null) {
            log.warn("给本地托管用户转入eth手续费 job - 未从redis中获取到支持的币种数据");
            return;
        }
        List<CurrencyBean> currencyList = hostEnableCurrencyArray
                .stream()
                .map(obj -> JSON.toJavaObject(((JSONObject) obj), CurrencyBean.class))
                .collect(Collectors.toList());

        //从redis中获取获取用户托管钱包集合
        Map<String, Long> hostUserWalletMap = redisUtil.hget(RedisKeys.ETH_HOST_WALLET_ADDRESS);
        if (hostUserWalletMap == null) {
            log.warn("给本地托管用户转入eth手续费 job - 未从redis中获取到本地托管用户数据");
            return;
        }
        Set<String> walletAddressSet = hostUserWalletMap.keySet();
        log.info("walletAddressSet={}",JSON.toJSONString(walletAddressSet));
        log.info("currencyList={}",JSON.toJSONString(currencyList));
        BigInteger[] gasPriceAndNonce = serviceEndpointEth.getGasPriceAndNonce(feeAddress);
        for (String walletAddress : walletAddressSet) {
            log.info("nonce={}", gasPriceAndNonce[1]);
            BigInteger needFee = BigInteger.ZERO; //当前用户所需的总手续费
            //获取地址的ETH余额
            BigInteger ethBalance = serviceEndpointEth.balance(walletAddress, io.mtc.common.constants.Constants.ETH_ADDRESS);
            log.info("获取用户地址的ETH余额={} walletAddress={}",JSON.toJSONString(ethBalance), walletAddress);
            for (CurrencyBean currencyBean : currencyList) {
                BigInteger tokenBalance = serviceEndpointEth.balance(walletAddress, currencyBean.getAddress());
                BigInteger outQtyToMainAddress = new BigInteger(currencyBean.getOutQtyToMainAddress());
                log.info("address={}, tokenBalance={},outQtyToMainAddress={}", currencyBean.getAddress(), tokenBalance, outQtyToMainAddress);
                if (tokenBalance.compareTo(outQtyToMainAddress) > 0) {
                    BigInteger gasPrice = serviceEndpointEth.gasPrice();
                    BigInteger fee = TransactionConstants.getUseGasPrice(gasPrice).multiply(TransactionConstants.GAS_AMOUNT);
                    log.info("gasPrice={}, fee={}", gasPrice, fee);
                    needFee = needFee.add(fee);
                }
            }
            needFee = needFee.subtract(ethBalance); // 剩余eth-需要的eth = 要转入的eth

            //需不需要转入eth
            if (needFee.compareTo(BigInteger.ZERO) > 0) {
                log.info("开始转入手续费：needFee={}, walletAddress={}", needFee, walletAddress);
                String signedTransactionData = PackageUtil.packageEther(gasPriceAndNonce, needFee,
                        walletAddress, feeKeyStorePath, feeKeyStorePassword);
                MeshTransactionData transactionData = MeshTransactionData.from(signedTransactionData);

                PlatformTransfer platformTransfer = new PlatformTransfer();
                platformTransfer.setType(PlatformTransferType.FEE);
                platformTransfer.setStatus(PlatformTransferStatus.PACKAGE);
                platformTransfer.setCurrencyAddress(io.mtc.common.constants.Constants.ETH_ADDRESS);
                platformTransfer.setFromAddress(feeAddress);
                platformTransfer.setToAddress(walletAddress);
                platformTransfer.setQty(needFee);
                platformTransfer.setTxHash(transactionData.txHash);
//                platformTransfer.setGasPrice(new BigInteger());
//                platformTransfer.setGasLimit(new BigInteger());
                platformTransfer.setJobExpireDate(DateUtil.plusSeconds(new Date(), 30 * 60));

                // 签名失败
                if (transactionData.txHash == null) {
                    //平台转账记录
                    platformTransfer.setStatus(PlatformTransferStatus.FAIL);
                    platformTransferRepository.save(platformTransfer);
                    log.error("给本地托管用户转入eth手续费 job - 签名失败 - walletAddress={}", walletAddressSet);
                    continue;
                }
                platformTransferRepository.save(platformTransfer);

                // 打包请求,发送MQ消息到service-endpoint-eth进行处理
                EthTransObj request = new EthTransObj();
                request.setSignedTransactionData(signedTransactionData);
                // 类型：平台转入手续费
                request.setTxType(EthTransObj.TxType.FEE.ordinal());
                request.setTxId(platformTransfer.getId());
                request.setCurrencyAddress(io.mtc.common.constants.Constants.ETH_ADDRESS);
                log.info("EthTransObj={}", JSON.toJSONString(request));
                boolean sendResult = producer.send(
                        Constants.Topic.MTC_BIZ_TRANS,
                        Constants.Tag.ETH_BIZ_TRANS_PENDING,
                        request,
                        Constants.Tag.ETH_BIZ_TRANS_PENDING.name() + platformTransfer.getId());

                log.info("发送mq消息结果：sendResult={}");
                if (sendResult) {
                    platformTransfer.setStatus(PlatformTransferStatus.SEND);
                    gasPriceAndNonce[1] = gasPriceAndNonce[1].add(BigInteger.ONE);
                } else {
                    platformTransfer.setStatus(PlatformTransferStatus.FAIL);
                }
                platformTransferRepository.save(platformTransfer);
                log.info("开始转入手续费成功：needFee={}", needFee);
            }
        }

    }

    /***
     * eth
     * 本地托管用户向主钱包地址进行汇总
     * 1.从数据库中获取支持托管的币种
     * 2.从用户托管钱包中获取所有托管用户的eth钱包集合
     * 3.循环遍历钱包地址，查询支持的币种的余额，如果达到汇总阈值，添加平台记录
     * 4.判断地址的eth余额和需要的手续费进行对比，如果不足，不进行操作，如果充足，进行转账汇总操作
     * 5.打包交易，发送mq消息，给到service-endpoint-eth进行打包进行处理
     * 6.生成平台内转账记录
     */
    public void ethUserToMainAddress() {
        //获取支持托管的币种
        JSONArray hostEnableCurrencyArray = getCurrencyList();
        if (hostEnableCurrencyArray == null) {
            log.warn("本地托管用户向主钱包地址进行汇总 job - 未从redis中获取到支持的币种数据");
            return;
        }
        List<CurrencyBean> currencyList = hostEnableCurrencyArray
                .stream()
                .map(obj -> JSON.toJavaObject(((JSONObject) obj), CurrencyBean.class))
                .collect(Collectors.toList());

        //从redis中获取获取用户托管钱包集合
        Map<String, Long> hostUserWalletMap = redisUtil.hget(RedisKeys.ETH_HOST_WALLET_ADDRESS);
        if (hostUserWalletMap == null) {
            log.warn("本地托管用户向主钱包地址进行汇总 job - 未从redis中获取到本地托管用户数据");
            return;
        }
        Set<String> walletAddressSet = hostUserWalletMap.keySet();
        for (String walletAddress : walletAddressSet) {
            //获取地址的ETH余额
            BigInteger ethBalance = serviceEndpointEth.balance(walletAddress, io.mtc.common.constants.Constants.ETH_ADDRESS);
            //余额为0，没有手续费，不进行操作
            if (ethBalance.compareTo(BigInteger.ZERO) < 0) continue;
            for (CurrencyBean currencyBean : currencyList) {
                BigInteger tokenBalance = serviceEndpointEth.balance(walletAddress, currencyBean.getAddress());
                BigInteger outQtyToMainAddress = new BigInteger(currencyBean.getOutQtyToMainAddress());
                if (tokenBalance.compareTo(outQtyToMainAddress) > 0) {
                    List<PlatformTransfer> notVerifyList = platformTransferRepository.findByFromAddressAndStatus(walletAddress, PlatformTransferStatus.SEND);
                    if (CollectionUtils.isNotEmpty(notVerifyList)) {
                        continue; //存在未处理完成的记录
                    }
                    UserWallet userWallet = userWalletRepository.findByWalletAddress(walletAddress);
                    if (userWallet == null || StringUtils.isBlank(userWallet.getSecret())){
                        log.error("本地托管用户向主钱包地址进行汇总 job - 获取userWallet数据失败 - {}", walletAddressSet);
                        continue;
                    }
                    //解密私钥
                    String privateKey = EthCreateWalletUtil.decryptPrivateKey(userWallet.getSecret(), userWallet.getUser().getId().toString());

                    BigInteger[] gasPriceAndNonce = serviceEndpointEth.getGasPriceAndNonce(walletAddress);
                    String signedTransactionData;
                    MeshTransactionData transactionData;
                    if (currencyBean.getAddress().equals(io.mtc.common.constants.Constants.ETH_ADDRESS)){//汇总ETH
                        signedTransactionData = PackageUtil.packageEtherByPrivateKey(gasPriceAndNonce, tokenBalance,
                                depositAddress, privateKey);
                        transactionData = MeshTransactionData.from(signedTransactionData);
                    } else {
                        signedTransactionData = PackageUtil.packageCurrencyByPrivateKey(gasPriceAndNonce, tokenBalance,
                                depositAddress, currencyBean.getAddress() , privateKey);
                        transactionData = MeshTransactionData.from(signedTransactionData);
                    }

                    PlatformTransfer platformTransfer = new PlatformTransfer();
                    platformTransfer.setType(PlatformTransferType.TO_MAIN);
                    platformTransfer.setStatus(PlatformTransferStatus.PACKAGE);
                    platformTransfer.setCurrencyAddress(currencyBean.getAddress());
                    platformTransfer.setFromAddress(walletAddress);
                    platformTransfer.setToAddress(depositAddress);
                    platformTransfer.setQty(tokenBalance);
                    platformTransfer.setTxHash(transactionData.txHash);
//                platformTransfer.setGasPrice(new BigInteger());
//                platformTransfer.setGasLimit(new BigInteger());
                    platformTransfer.setJobExpireDate(DateUtil.plusSeconds(new Date(), 30 * 60));

                    // 签名失败
                    if (transactionData.txHash == null) {
                        //平台转账记录
                        platformTransfer.setStatus(PlatformTransferStatus.FAIL);
                        platformTransferRepository.save(platformTransfer);
                        log.error("给本地托管用户转入eth手续费 job - 签名失败 - walletAddress={}", walletAddressSet);
                        continue;
                    }
                    platformTransferRepository.save(platformTransfer);

                    // 打包请求,发送MQ消息到service-endpoint-eth进行处理
                    EthTransObj request = new EthTransObj();
                    request.setSignedTransactionData(signedTransactionData);
                    // 类型：平台转入手续费
                    request.setTxType(EthTransObj.TxType.TO_MAIN.ordinal());
                    request.setTxId(platformTransfer.getId());
                    request.setCurrencyAddress(currencyBean.getAddress());

                    boolean sendResult = producer.send(
                            Constants.Topic.MTC_BIZ_TRANS,
                            Constants.Tag.ETH_BIZ_TRANS_PENDING,
                            request,
                            Constants.Tag.ETH_BIZ_TRANS_PENDING.name() + platformTransfer.getId());

                    if (sendResult) {
                        platformTransfer.setStatus(PlatformTransferStatus.SEND);
                    } else {
                        platformTransfer.setStatus(PlatformTransferStatus.FAIL);
                    }
                    platformTransferRepository.save(platformTransfer);
                }
            }
        }

    }
}
