package io.mtc.facade.user.service;

import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.constants.Constants;
import io.mtc.common.dto.EthHostWalletAddressTrans;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.redis.util.UsdtRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.bean.CreateWalletResultBean;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserWallet;
import io.mtc.facade.user.feign.FacadeBitcoin;
import io.mtc.facade.user.repository.BillRepository;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserRepository;
import io.mtc.facade.user.repository.UserWalletRepository;
import io.mtc.facade.user.util.EnvUtil;
import io.mtc.facade.user.util.wallet.BtcCreateWalletUtil;
import io.mtc.facade.user.util.wallet.EthCreateWalletUtil;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 余额服务
 *
 * @author Chinhin
 * 2019-01-18
 */
@Slf4j
@Service
public class BalanceService implements MsgHandler {

    @Resource
    private UserWalletRepository userWalletRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private DepositWithdrawService depositWithdrawService;

    @Resource
    private BillRepository billRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private UsdtRedisUtil usdtRedisUtil;

    @Resource
    private FacadeBitcoin facadeBitcoin;

    /**
     * 初始化要监控的钱包地址
     */
    public void initMonitorAddress() {
        // 初始化ETH托管用户地址
        if (!ethRedisUtil.monitorUserHostWalletInitFinish()) {
            log.info("开始初始化需要监控的ETH托管账户钱包地址");
            // 将所有用户的托管账户的钱包地址都加在监控中（这里只初始化了ETH币系的）
            userWalletRepository.findAllByCurrencyType(1).forEach(it -> {
                ethRedisUtil.monitorUserHostWallet(it.getWalletAddress(), it.getUser().getId());
            });
            ethRedisUtil.finishInitMonitorUserHostWallet();
            log.info(">>> 初始化需要监控的ETH托管账户钱包地址 【完成】");
        }

        // 初始化USDT托管用户地址
        if (!usdtRedisUtil.monitorUserHostWalletInitFinish()) {
            log.info("开始初始化需要监控的USDT托管账户钱包地址");
            // 将所有用户的托管账户的钱包地址都加在监控中（这里只初始化了ETH币系的）
            userWalletRepository.findAllByCurrencyType(5).forEach(it -> {
                usdtRedisUtil.monitorUserHostWallet(it.getWalletAddress(), it.getUser().getId());
            });
            usdtRedisUtil.finishInitMonitorUserHostWallet();
            log.info(">>> 初始化需要监控的USDT托管账户钱包地址 【完成】");
        }
    }

    /**
     * 获取或分配 用户的钱包地址
     * @param uid 用户ID
     * @param currencyType coin类型
     * @return 钱包地址, null表示创建失败
     */
    @Transactional
    public String getWalletAddress(Long uid, Integer currencyType) throws Exception {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return null;
        }
        String address = user.getWalletAddressByCurrencyType(currencyType);
        if (address == null) {
            String ethCreateWalletLock = RedisKeys.ETH_CREATE_WALLET_LOCK(user.getId(), currencyType);
            Boolean lock = ethRedisUtil.distributeLock(ethCreateWalletLock, 10);
            if (!lock) {
                return null;
            }
            try {
                CreateWalletResultBean result;
                // 以太坊
                if (currencyType == 1) {
                    result = EthCreateWalletUtil.createWallet(user);
                } else if (currencyType == 4) {
                    HashMap map = (HashMap) facadeBitcoin.getNewAddress(BitcoinTypeEnum.BTC);
                    String newAddress = (String) map.get("result");
                    result = new CreateWalletResultBean("", newAddress);
                } else if (currencyType == 5){
                    HashMap map = (HashMap) facadeBitcoin.getNewAddress(BitcoinTypeEnum.USDT);
                    String newAddress = (String) map.get("result");
                    result = new CreateWalletResultBean("", newAddress);
                } else {
                    // TODO 其他方式
                    return null;
                }
                if (result == null) {
                    return null;
                }
                address = result.getAddress();

                UserWallet wallet = new UserWallet();
                wallet.setCurrencyType(currencyType);
                wallet.setUser(user);
                wallet.setWalletAddress(result.getAddress());
                wallet.setSecret(result.getEncryptPrivateKey());
                userWalletRepository.save(wallet);

                // 以太坊
                if (currencyType == 1) {
                    ethRedisUtil.monitorUserHostWallet(result.getAddress(), user.getId());
                } else if (currencyType == 5) {
                    usdtRedisUtil.monitorUserHostWallet(result.getAddress(), user.getId());
                }
            } finally {
                ethRedisUtil.delete(ethCreateWalletLock);
            }
        }
        return address;
    }

    /**
     * 增加余额
     * @param user 用户
     * @param currencyType coin类型
     * @param currencyAddress 代币地址
     * @param addBalance 增加多少(wei)，减少为负数
     * @return 对象(未保存)
     */
    public UserBalance addBalance(User user, Integer currencyType, String currencyAddress, BigInteger addBalance) {
        UserBalance balanceBean = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);
        // 该代币还未充值过
        if (balanceBean == null) {
            balanceBean = new UserBalance();
            balanceBean.setWalletAddress("Deprecated");
            balanceBean.setUser(user);
            balanceBean.setCurrencyType(currencyType);
            balanceBean.setCurrencyAddress(currencyAddress);
        }
        balanceBean.setBalance(balanceBean.getBalance().add(addBalance));
        return balanceBean;
    }

    /**
     * 监控的钱包地址有交易时，会调用此接口
     * @param json 信息
     * @return 结果
     */
    @Transactional
    @Override
    public Action doConsume(String json) {
        log.info("托管钱包地址交易:{}", json);

        EthHostWalletAddressTrans hostTransInfo = CommonUtil.fromJson(json, EthHostWalletAddressTrans.class);
        completeWalletDeposit(hostTransInfo, 1);
        return Action.CommitMessage;
    }

    /**
     * 比特币的钱包地址有充值到账
     * @param info 交易信息
     * @param walletAddress 比特币的钱包地址
     * @param fromAddresses 转入地址
     * @param amount 金额
     */
    public void btcWalletDesposit(Map<String, Object> info, String walletAddress, String fromAddresses, Coin amount) {
        EthHostWalletAddressTrans hostTransInfo = new EthHostWalletAddressTrans();
        hostTransInfo.setNonce(BigInteger.ZERO);
        hostTransInfo.setTxHash(info.get("txid").toString());
        hostTransInfo.setTokenAddress(BitcoinTypeEnum.BTC.name());
        hostTransInfo.setWalletAddress(walletAddress);
        hostTransInfo.setFromAddress(fromAddresses);
        String amountStr = amount.toPlainString();
        hostTransInfo.setIncome(CommonUtil.btc2wei(amountStr));

        completeWalletDeposit(hostTransInfo, 4);
    }

    /**
     * USDT的钱包地址有充值到账
     * @param txHash 交易hash
     * @param walletAddress 比特币的钱包地址
     * @param fromAddresses 转入地址
     * @param amount 金额
     */
    public void usdtWalletDesposit(String txHash, String walletAddress, String fromAddresses, BigDecimal amount) {
        EthHostWalletAddressTrans hostTransInfo = new EthHostWalletAddressTrans();
        hostTransInfo.setNonce(BigInteger.ZERO);
        hostTransInfo.setTxHash(txHash);
        hostTransInfo.setTokenAddress(Constants.USDT_CURRENCY_ADDRESS);
        hostTransInfo.setWalletAddress(walletAddress);
        hostTransInfo.setFromAddress(fromAddresses);
        String amountStr = amount.toPlainString();
        hostTransInfo.setIncome(CommonUtil.btc2wei(amountStr));
        completeWalletDeposit(hostTransInfo, 5);
    }

    /**
     * 完成托管用户钱包的充值
     */
    private void completeWalletDeposit(EthHostWalletAddressTrans hostTransInfo, Integer currencyType) {
        // 已经用过的交易hash则直接返回
        Bill repeatBill = billRepository.findByTxHashAndCurrencyTypeAndType(hostTransInfo.getTxHash(), currencyType, BillType.DEPOSIT);
        if (repeatBill != null) {
            return;
        }

        UserWallet userWallet = userWalletRepository.findByWalletAddress(hostTransInfo.getWalletAddress());
        UserBalance balanceBean = addBalance(userWallet.getUser(), currencyType, hostTransInfo.getTokenAddress(), BigInteger.ZERO);

        Bill bill = new Bill();
        bill.setTxNonce(hostTransInfo.getNonce());
        bill.setBalance(balanceBean);
        bill.setCurrentBalance(balanceBean.getBalance());
        bill.setIncome(hostTransInfo.getIncome());
        bill.setStatus(BillStatus.PROCESSING);
        bill.setType(BillType.DEPOSIT);
        bill.setCurrencyType(currencyType);
        bill.setRelatedAddress(hostTransInfo.getFromAddress());
        bill.setTxHash(hostTransInfo.getTxHash());
        billRepository.save(bill);

        EthTransObj transInfo = new EthTransObj();
        transInfo.setTxId(bill.getId());
        transInfo.setCoinType(currencyType);
        transInfo.setStatus(1);
        transInfo.setAmount(hostTransInfo.getIncome().toString());

        depositWithdrawService.completeDeposit(transInfo);
    }

}
