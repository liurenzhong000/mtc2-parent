package io.mtc.facade.user.service;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.feign.FacadeBitcoin;
import io.mtc.facade.user.feign.ServiceEndpointEth;
import io.mtc.facade.user.repository.BillRepository;
import io.mtc.facade.user.repository.UserBalanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值提现扩展服务
 *
 * @author Chinhin
 * 2019/1/14
 */
@Slf4j
@Service
public class DepositWithdrawService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private BillRepository billRepository;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private FacadeBitcoin facadeBitcoin;

    //TODO 修改地址
    @Value("${btcHostAddress:1DjscPGGSS4taT8ACsbX4Qf4Mn7bruXQAS}")
    private String btcHostAddress;

    /**
     * 完成充值
     * @param transInfo 充值信息
     */
    public void completeDeposit(EthTransObj transInfo) {
        Bill bill = billRepository.findById(transInfo.getTxId()).orElse(null);
        // 如果该充值记录已经处理过
        if (bill == null || bill.getStatus() == BillStatus.SUCCESS) {
            return;
        }

        BigInteger income = new BigInteger(transInfo.getAmount());
        if (transInfo.getCoinType() != null && transInfo.getCoinType() == 1) {
            // ############ 余额修正 ##############
            String currencyAddress = bill.getBalance().getCurrencyAddress();
            Integer balanceDecimals = redisUtil.get(RedisKeys.DECIMALS_TOKEN(currencyAddress), Integer.class);
            if (balanceDecimals == null) {
                balanceDecimals = serviceEndpointEth.getBalanceDecimals(currencyAddress);
            }
            income = CommonUtil.balanceCorrect(income, balanceDecimals);
            // ############ 余额修正 ##############
        }

        // 充值成功
        if (transInfo.getStatus() == 1) {
            bill.setStatus(BillStatus.SUCCESS);
            bill.setIncome(income);
        } else if (transInfo.getStatus() == 2) {
            bill.setStatus(BillStatus.FAILURE);
        }

        // 充值成功
        if (transInfo.getStatus() == 1) {
            // 更新余额
            UserBalance balance = bill.getBalance();
            balance.setBalance(balance.getBalance().add(bill.getIncome()));
            userBalanceRepository.save(balance);

            bill.setCurrentBalance(balance.getBalance());

            if (transInfo.getCoinType() != null && transInfo.getCoinType() == 1) {
                // 之前相同的充值记录全置换为 失败
                // 条件1：BillStatus.PROCESSING
                // 条件2：相同的充值地址RelatedAddress
                // 条件3：nonce小于这个的
                billRepository.setExpireBillStatus(BillStatus.FAILURE,
                        BillStatus.PROCESSING, bill.getRelatedAddress(), bill.getTxNonce());
            }
        }
        billRepository.save(bill);
    }

    /**
     * 完成提现
     * @param transInfo 交易信息
     */
    public void completeWithdraw(EthTransObj transInfo) {
        Bill bill = billRepository.findById(transInfo.getTxId()).get();
        // 如果该充值记录已经处理过
        if (bill.getStatus() == BillStatus.SUCCESS) {
            return;
        }
        // 提现成功
        if (transInfo.getStatus() == 1) {
            bill.setStatus(BillStatus.SUCCESS);
        } else if (transInfo.getStatus() == 2) {
            bill.setStatus(BillStatus.FAILURE);
        }

        BigInteger outcome = bill.getOutcome();
        // EOS需要核对提现金额
        if (transInfo.getCoinType() != null && transInfo.getCoinType() == 3) {
            BigInteger actOutcome = new BigInteger(transInfo.getAmount());
            // 不同
            if (actOutcome.compareTo(outcome) != 0) {
                log.error("提现金额订单与回调返回不同");
                return;
            }
        }
        UserBalance userBalance = bill.getBalance();
        // 扣除冻结金额
        BigInteger freezingAmount = userBalance.getFreezingAmount();
        userBalance.setFreezingAmount(freezingAmount.subtract(outcome));
        // 成功的情况下扣除余额
        if (transInfo.getStatus() == 1) {
            BigInteger balance = userBalance.getBalance();
            userBalance.setBalance(balance.subtract(outcome));
        }
        userBalanceRepository.save(userBalance);

        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        // 查看是否有其他的处理中的提现记录，让他重新排队
        if (transInfo.getStatus() == 1) {
            billRepository.setExpireBillStatus(BillStatus.PENDING, BillStatus.PROCESSING, bill.getTxNonce());
        }
    }

    /**
     * 根据订单验证该交易是否正常提交
     * 如果正常则走完成订单流程
     *
     * @param bill 订单
     * @return 错误信息，为null表示正常
     */
    String validateBitcoinDeposit(Bill bill) {
        // 不是比特系的直接返回
        if (bill.getCurrencyType() != 2 && bill.getCurrencyType() != 4) {
            return null;
        }
        // 订单已经结束
        if (bill.getStatus() == BillStatus.SUCCESS || bill.getStatus() == BillStatus.FAILURE) {
            return "该订单状态为已完成";
        }
        BitcoinTypeEnum type = BitcoinTypeEnum.BTC;
        if (bill.getCurrencyType() == 2) {
            type = BitcoinTypeEnum.BCH;
        }
        HashMap result = (HashMap) facadeBitcoin.txDetail(type, bill.getTxHash());
        HashMap resultMap = (HashMap) result.get("result");

        // [{sequence=4294967295, addr=1NhTXMmcz5rVfyborHitVz29zX1NxVUAT8, value=0.005851, n=1}]
        List<Map<String, Object>> vin = (List<Map<String, Object>>) resultMap.get("vin");
        boolean isVinOk = false;
        for (Map<String, Object> temp : vin) {
            if (temp.get("addr").toString().equals(bill.getRelatedAddress())) {
                isVinOk = true;
            }
        }
        if (!isVinOk) {
            return "该交易的输入与订单不匹配";
        }

        // [{"addr":"1CbjjM7LDBvmtdvyyZbbb8hp3B8gwUEaio","value":"50.00000000","n":0}]
        List<Map<String, Object>> vout = (List<Map<String, Object>>) resultMap.get("vout");
        boolean isVoutOK = false;
        for (Map<String, Object> temp : vout) {
            // 如果与托管总账户地址相同
            if (temp.get("addr").toString().equals(btcHostAddress)) {
                BigInteger value = CommonUtil.btc2wei(temp.get("value").toString());
                if (value.compareTo(bill.getIncome()) == 0) {
                    isVoutOK = true;
                } else {
                    return "该交易的充值金额";
                }
            }
        }
        if (!isVoutOK) {
            return "该交易的充值地址不正确";
        }

        EthTransObj transInfo = new EthTransObj();
        transInfo.setStatus(1);
        transInfo.setAmount(bill.getIncome().toString());
        transInfo.setTxId(bill.getId());
        completeDeposit(transInfo);
        return null;
    }

}
