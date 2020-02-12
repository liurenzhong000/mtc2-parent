package io.mtc.facade.bitcoin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonUtil;
import io.mtc.common.http.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.util.Base64;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hyp
 * @Date: 2019/3/14 15:17
 * @Description: Omni钱包交互类
 * 注意account 和 address 的区别
 */
@Slf4j
public class BtcRpcUtil {

    private static BtcRpcUtil instance;

    private JsonRpcHttpClient client;

    private static void init() {
        if (null == instance) {
            instance = new BtcRpcUtil();
        }
    }

    public BtcRpcUtil() {
        // 身份认证
        String cred = Base64.encodeBytes((OmniConstants.RPC_USER + ":" + OmniConstants.RPC_PASSWORD).getBytes());
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Basic " + cred);
        try {
            client = new JsonRpcHttpClient(new URL("http://"+OmniConstants.RPC_ALLOWIP+":"+OmniConstants.RPC_PORT), headers);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static BtcRpcUtil getInstance() {
        init();
        return instance;
    }

    public BtcRpcResult baseInvoke(String method, Object... argument){
        BtcRpcResult btcRpcResult = new BtcRpcResult();
        try {
            Object resultObj = client.invoke(method, argument, Object.class);
            String result;
            try {
                result = (String) resultObj;
            } catch (Exception e) {
                result = JSON.toJSONString(resultObj);
            }
            btcRpcResult.setResult(result);
            btcRpcResult.setSuccess(true);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            btcRpcResult.setCode("1");
            btcRpcResult.setMsg(throwable.getMessage());
        }
        return btcRpcResult;
    }

    /**
     * 创建地址,返回地址 getnewaddress
     * @return 一个新地址
     */
    public BtcRpcResult getNewAddress(){
        return getNewAddress(null);
    }

    /**
     * 创建地址,返回地址 getnewaddress
     * @param account 账户名，如果为空，生成在默认账户下（""）
     * @return 一个新地址
     */
    public BtcRpcResult getNewAddress(String account){
        Object[] params = {};
        if (StringUtils.isNotBlank(account)) {
            params = new Object[]{account};
        }
        return baseInvoke("getnewaddress", params);
    }

    /**
     * 获取accountName账户下的第一个地址(如果不存在，生成一个地址)
     * @param account 账户名
     * @return 返回账户下的第一个地址
     */
    public BtcRpcResult getAccountAddress(String account){
        return baseInvoke("getaccountaddress", new Object[]{account});
    }

    /**
     * 获取账户名下的所有地址list
     * @param account 账户名
     * @return 获取当前账户名下的所有地址list
     */
    /*
    [
      "1Cmkeuk1XBHc7H6SB6UyfQYtKcsz1EPRaf",
      "1G12tmtWQudVaWy5z1pLSumbUrNpMiaQkA",
      "1HFPeviRooT2Zm4CbcmnGD3upBmhHBaBxU",
      "1PA1D8aUi5EXTtHW1hsXwxX7Mpsrt91i3L"
    ]
     */
    public BtcRpcResult getAddressesByAccount(String account){
        return baseInvoke("getaddressesbyaccount", new Object[]{account});
    }

    /**
     * 通过btc地址，获取账户名
     * @param address btc地址（可以是自己钱包的，也可以是别人钱包的）
     * @return 地址对应的账户名（账户名是相对自己钱包而言）
     */
    public BtcRpcResult getAccount(String address){
        return baseInvoke("getaccount", new Object[]{address});
    }

    /**
     * 返回钱包所有的账户
     *@return 返回以帐户名称为键、帐户余额为值的对象。
     */
    public BtcRpcResult listAccounts(){
        return baseInvoke("listaccounts", new Object[]{1});
    }

    /**
     * 验证是否未btc的有效地址
     * 验证地址是否存在 validateaddress
     * @param address
     * @return
     * @throws Throwable
     */
    /*
    //非本钱包的地址
    {
	"isvalid": true, //地址是否有效
	"address": "3KF9nXowQ4asSGxRRzeiTpDjMuwM2nypAN",
	"scriptPubKey": "a914c08e030911ba85f4a3c324ec6aa6d6722250be7487",
	"ismine": false, //是否本钱包的
	"iswatchonly": false,
	"isscript": true
    }
    //是本钱包的地址
    {
	"isvalid": true,
	"address": "18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ",
	"scriptPubKey": "76a9144fe664188617c76d63b898cdc7959e82ab3a550a88vc",
	"ismine": true,
	"iswatchonly": false,
	"isscript": false,
	"pubkey": "035d87996b4930d27642da17329b7dc8514ee62f14e592168dfbfeb221bc6e2459",
	"iscompressed": true,
	"account": "",
	"hdkeypath": "m/0'/0'/4'",
	"hdmasterkeyid": "8d318049913b613275027d0eefd2bd9f9e5ac3be"
     }
     */
    public BtcRpcResult validateAddress(String address){
        return baseInvoke("validateaddress", new Object[] {address});
    }

    /**
     * 导出地址私钥 dumpprivkey
     */
    public BtcRpcResult dumpPrivkey(String address) {
        return baseInvoke("dumpprivkey", new Object[] {address});
    }

    /**
     * 导入地址和私钥
     */
    public BtcRpcResult importprivkey(String privateKey) {
        return baseInvoke("importprivkey", new Object[] {privateKey});
    }

    /**
     * 获取区块高度
     */
    public BtcRpcResult getBlockCount(){
        return baseInvoke("getblockcount", new Object[] {});
    }

    /**
     * 获取账户对应的btc余额
     * --查询一个地址的余额，要先查到这个地址的用户
     * @param account 钱包里的账户名
     * @return 余额
     */
    public BtcRpcResult getBalance(String account){
        Object[] params = {};
        if (StringUtils.isNotBlank(account)) {
            params = new Object[]{account};
        }
        return baseInvoke("getbalance", params);
    }

    /**
     * 转账 sendtoaddress
     * amount 的 单位是：btc
     * 1K9mpEUQ25rdMmvndDvSdKkHq2rQP27hHS
     * 861a40de00a905df8efb659f9862f2daa2e75256ce4614d098a4a58931209f1a
     */
    public BtcRpcResult sendToAddress(String address, BigDecimal amount) {
        return baseInvoke("sendtoaddress", new Object[] {address, amount});
    }

    /**
     * 获取账户下的交易记录 listtransactions
     * @param account 账户
     * @param count 每页的个数
     * @param offset 第几页
     * @return
     */
    public BtcRpcResult listTransactions(String account, int count ,int offset) {
        return baseInvoke("listtransactions", new Object[] {account, count, offset});
    }

    /**
     * 获取钱包内所有的交易记录
     * @param count
     * @param offset
     * @return
     */
    public BtcRpcResult listTransactions(int count ,int offset) {
        return baseInvoke("listtransactions", new Object[] {"*", count, offset});
    }

    /**
     * 获取一个hash的详细信息
     * @param txId 交易hash
     * @return 详细记录
     */
    public BtcRpcResult getTransaction(String txId) {
        return baseInvoke("gettransaction", new Object[]{txId});
    }

    /**
     * 获取还未花费的UTXO
     * @return 返回全部的未花费的UTXO
     */
    public BtcRpcResult listUnSpent(){
        return baseInvoke("listunspent", new Object[]{});
    }

    /**
     * 获取对应地址还未花费的UTXO
     * @param minConfirm 最小确认数
     * @param maxConfirm 最大确认数
     * @param address 地址list
     * @return
     */
    public BtcRpcResult listUnSpent(int minConfirm, int maxConfirm, String... address){
        return baseInvoke("listunspent", new Object[]{minConfirm, maxConfirm, address});
    }

    /**
     * 钱包备份
     * 把钱包文件备份到../walletBackup/wallet-%s.txt
     */
    public BtcRpcResult dumpWallet(){
        System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String file = String.format("../walletBackup/wallet-%s.txt", time);
        return baseInvoke("dumpwallet", new Object[] {file});
    }

    /**
     * 导入钱包数据，会把对应文件上的地址导入到钱包中
     * 对于影响新添加的密钥的事务，调用可能需要重新扫描整个链，可能需要几分钟。
     * https://bitcoin.org/en/developer-reference#importwallet
     */
    public BtcRpcResult importWallet(String fielName) {
        return baseInvoke("importwallet", new Object[] {fielName});
    }

    //====================================Omni相关api====================================
    /**
     * 从fromAddress发送propertyId的币种给toAddress
     * 要确保发送方有大于0.00000546的btc，交易粉尘，用于标记地址
     * feeAddress 是手续费地址，在feeAddress上消耗手续费，并把多余的手续费回到feeAddress上
     */
    public BtcRpcResult omniFundedSend(String fromAddress, String toAddress, int propertyId, String amount, String feeAddress){
        return baseInvoke("omni_funded_send", new Object[]{fromAddress, toAddress, propertyId, amount, feeAddress});
    }

    /**
     * 获取钱包信息 omni_getinfo
     */
    /*
    {
        "omnicoreversion_int": 30001000,
        "omnicoreversion": "0.3.1",//omni版本
        "mastercoreversion": "0.3.1",
        "bitcoincoreversion": "0.13.2",//btc版本
        "block": 566983,//区块高度
        "blocktime": 1552547557,
        "blocktransactions": 477,
        "totaltrades": 1804,
        "totaltransactions": 5669251,
        "alerts": []
    }
     */
    public BtcRpcResult omniGetInfo(){
        return baseInvoke("omni_getinfo", new Object[]{});
    }

    /**
     * 获取对应propertyId的余额 usdt的propertyid=31
     */
    /*
    {
      "balance": "200.00000000", //可用的地址余额
      "reserved": "0.00000000",//买卖双方保留的金额
      "frozen": "0.00000000"//发行者冻结的金额(仅适用于托管的属性)
    }
     */
    public BtcRpcResult omniGetBalance(String address, Integer propertyId){
        return baseInvoke("omni_getbalance", new Object[] {address, propertyId});
    }

    /**
     * 查询交易事务(用于查询omni协议的-查usdt) 查询普通btc的没有
     */
    public BtcRpcResult omniGetTransaction(String txId) {
        return baseInvoke("omni_gettransaction", new Object[] {txId});
    }

    /**
     * 查询钱包内的USDT总额
     * @return 钱包内的USDT总额
     */
    public BtcRpcResult omniGetWalletBalances(){
        return baseInvoke("omni_getwalletbalances", new Object[] {});
    }

    /**
     * 查询指定区块的所有hash
     * @return 一个区块的所有交易hash
     */
    public BtcRpcResult omniListBlockTransactions(Integer blockIndex) {
        return baseInvoke("omni_listblocktransactions", new Object[] {blockIndex});
    }

    /*
    omni_listtransactions
    omni_listpendingtransactions
    查询钱包内的所有地址的USDT余额列表 omni_getwalletaddressbalances
     */

    //  第三方接口
    /**
     * 感觉不太准
     * 获取预估旷工费 0.00933632
     * @param inputCount
     * @return
     */
    public BigDecimal calculationFee(int inputCount) {
        //计算手续费获取每个字节的手续费
        String url = "https://bitcoinfees.earn.com/api/v1/fees/recommended";
        //计算字节大小和费用
        JSONObject forObject = JSON.parseObject(HttpUtil.get(url));
        //148 * 输入数额 + 34 * 输出数额 + 10
        BigDecimal keyCount = BigDecimal.valueOf((inputCount * 148 + 44) * forObject.getDoubleValue("halfHourFee"));
        BigDecimal transferFee = keyCount.divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);
        return transferFee;
    }

    public BigDecimal getBtcBalanceByAddress(String address) {
        JSONObject forObject  = HttpUtil.get4obj("https://chain.so/api/v2/get_address_balance/BTC/" + address, JSONObject.class);
        String status = forObject.getString("status");
        if (StringUtils.equalsIgnoreCase(status, "success")) {
            String confirmed_balance = forObject.getJSONObject("data").getString("confirmed_balance");
            return new BigDecimal(confirmed_balance);
        } else {
            throw new RuntimeException("address获取BTC余额失败");
        }
    }


    public static void main(String[] args) {
        //f4a27f2f29716ec7b3ad05cc17bd0160a768d59e29da98257f64f1614ee0afa0
        //a18ae2f313b4d07cb2e9e9fec43ea3795261e441598a1e844b4c86530f32a75d
//        log.info("{}", JSON.toJSONString(BtcRpcUtil.getInstance().omniFundedSend("1NWbqeVR6CQ1L57Urtkf3xBY6itp3YiADx","18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ",31, "20", "18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ")));
//        log.info(JSON.toJSONString(BtcRpcUtil.getInstance().omniListBlockTransactions(567168)));
        log.info(JSON.toJSONString(BtcRpcUtil.getInstance().getBtcBalanceByAddress("18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ")));
    }

}
