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

    @Value("${btcHostAddress:1DjscPGGSS4taT8ACsbX4Qf4Mn7bruXQAS}")
    private String btcHostAddress;

    @Value("${btcWalletPath}")
    private String btcWalletPath;

    @Resource
    private FacadeUser facadeUser;

    @Resource
    private BitcoinService bitcoinService;

    public RequestResult btcWithdraw(BitcoinTypeEnum bitcoinType, String targetAddress, Coin amount, Long billId) throws Exception {
        File walletFile = new File(btcWalletPath);
        if (!walletFile.exists()) {
            return new RequestResult(false, "Wallet File not exists.", null);
        }
        boolean verifyBill = verifyBill(billId, targetAddress, amount);
        // 验证订单失败
        if (!verifyBill) {
            return new RequestResult(false, "提现信息与订单不符", null);
        }

        MainNetParams params = MainNetParams.get();

        // 查询托管地址的utxo
        Object listUTXOObj = bitcoinService.listUTXO(bitcoinType, btcHostAddress);
        Coin hostBalance = getBalanceByUtxo((Map) listUTXOObj);

        // 创建交易
        Transaction tx = new Transaction(params);
        // 给目标地址转账
        Address toAddress = Address.fromBase58(params, targetAddress);
        tx.addOutput(amount, toAddress);

        // 找零为余额减去手续费减去转账金额
        Coin change = hostBalance.subtract(Coin.parseCoin(Constants.BTC_WITHDRAW_FEE))
                .subtract(amount);
        Address balanceAddress = Address.fromBase58(params, btcHostAddress);
        tx.addOutput(change, balanceAddress);

        // 给输入签名
        txAddSignInput(tx, params, walletFile, (Map) listUTXOObj);

        String signStr = Hex.toHexString(tx.bitcoinSerialize());
        // 转账结果
        Object sendResultObj = bitcoinService.sendTransaction(bitcoinType, signStr, "提现交易 [" + billId + "]");
        Map<String, Object> sendResult = (Map<String, Object>) sendResultObj;
        Integer status = (Integer) sendResult.get("status");
        // 转账成功
        if (status == 200) {
            return new RequestResult(true, "发送交易成功", sendResult.get("result"));
        }
        return new RequestResult(false, "广播交易发生了错误", null);
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

    /**
     * 根据utxo获取余额
     * @param listUTXOObj json对象
     * @return 结果
     */
    private Coin getBalanceByUtxo(Map listUTXOObj) {
        JSONArray UTXOs = (JSONArray) listUTXOObj.get("result");
        BigDecimal balance = BigDecimal.ZERO;
        for (Object tempObj : UTXOs) {
            JSONObject temp = (JSONObject) tempObj;
            balance = balance.add(temp.getBigDecimal("amount"));
        }
        return Coin.parseCoin(balance.toString());
    }

    private void txAddSignInput(Transaction tx, NetworkParameters params, File walletFile, Map listUTXOObj) throws UnreadableWalletException {
        Wallet wallet = Wallet.loadFromFile(walletFile);
        wallet.decrypt("shenbin");
        List<ECKey> importedKeys = wallet.getImportedKeys();
        ECKey fromKey = importedKeys.get(0);

        JSONArray UTXOs = (JSONArray) listUTXOObj.get("result");
        Script utxo_script = ScriptBuilder.createOutputScript(fromKey.toAddress(params));
        for (Object tempObj : UTXOs) {
            JSONObject temp = (JSONObject) tempObj;
            String txid = temp.getString("txid");
            int vout = temp.getIntValue("vout");

            TransactionOutPoint txout = new TransactionOutPoint(params, vout, Sha256Hash.wrap(txid));
            tx.addSignedInput(txout, utxo_script, fromKey, Transaction.SigHash.ALL, true);
        }
    }

}
