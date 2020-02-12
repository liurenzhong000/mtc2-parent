package io.mtc.facade.bitcoin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.constants.Constants;
import io.mtc.common.dto.RequestResult;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.bitcoin.feign.FacadeUser;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 交易服务
 *
 * @author Chinhin
 * 2019-01-17
 */
@Service
public class TxService {

    @Value("${usdt-deposit-address}")
    private String usdtDepositAddress;

    @Resource
    private FacadeUser facadeUser;

    @Resource
    private BitcoinService bitcoinService;

    public RequestResult btcWithdraw(String targetAddress, Coin amount, Long billId) throws Exception {
        boolean verifyBill = verifyBill(billId, targetAddress, amount);
        // 验证订单失败
        if (!verifyBill) {
            return new RequestResult(false, "提现信息与订单不符", null);
        }
        // 转账结果
        try {
            String txId = bitcoinService.sendToAddress(targetAddress, amount.toPlainString());
            return new RequestResult(true, "发送交易成功", txId);
        } catch (Exception e) {
            return new RequestResult(false, e.getMessage(), null);
        }

    }

    public RequestResult usdtWithdraw(String targetAddress, Coin amount, Long billId) {
        boolean verifyBill = verifyBill(billId, targetAddress, amount);
        // 验证订单失败
        if (!verifyBill) {
            return new RequestResult(false, "提现信息与订单不符", null);
        }
        try {
            // 转账结果
            String txId = bitcoinService.sendUsdtTransaction(usdtDepositAddress, targetAddress, 31, amount.toPlainString(), usdtDepositAddress);
            return new RequestResult(true, "发送交易成功", txId);
        } catch (Exception e) {
            return new RequestResult(false, e.getMessage(), null);
        }
    }

    /**
     * 核对账单与提现信息是否一致
     * @param billId 订单id
     * @param targetAddress 提现地址
     * @param amount 提现金额
     * @return true表示验证通过
     */
    private boolean verifyBill(Long billId, String targetAddress, Coin amount) {
        String resultStr = facadeUser.billDetail(billId);
        JSONObject resultJson = JSONObject.parseObject(resultStr);
        String relatedAddress = resultJson.getString("relatedAddress");
        if (!relatedAddress.equals(targetAddress)) {
            return false;
        }
        BigInteger outcome = resultJson.getBigInteger("outcome");
        BigInteger outComeFee = resultJson.getBigInteger("outComeFee");
        Coin outcomeCoin = CommonUtil.wei2btc(outcome.subtract(outComeFee));
        return outcomeCoin.equals(amount);
    }

}
