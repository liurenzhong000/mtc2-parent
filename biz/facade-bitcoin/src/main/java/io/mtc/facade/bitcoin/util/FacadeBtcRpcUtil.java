package io.mtc.facade.bitcoin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Auther: hyp
 * @Date: 2019/3/15 18:13
 * @Description: 对BtcRpcUtil进行再一层封装
 */
public class FacadeBtcRpcUtil {

    public static Integer getBlockCount(){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().getBlockCount();
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("获取最新区块高度失败 - "+ btcRpcResult.getMsg());
        }
        return Integer.parseInt(btcRpcResult.getResult());
    }

    public static List<String> omniListBlockTransactions(Integer blockIndex){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().omniListBlockTransactions(blockIndex);
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("获取区块交易hash失败 - "+ btcRpcResult.getMsg());
        }
        List<String> hashList = JSONArray.parseArray(btcRpcResult.getResult(), String.class);
        return hashList;
    }

    public static OmniTransactionBean omniGetTransaction(String txId){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().omniGetTransaction(txId);
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("获取hash详情失败 - "+ btcRpcResult.getMsg());
        }
        OmniTransactionBean result = JSON.parseObject(btcRpcResult.getResult(), OmniTransactionBean.class);
        return result;
    }

    public static String getNewAddress(){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().getNewAddress();
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("获取新钱包地址失败 - "+ btcRpcResult.getMsg());
        }
        String address = btcRpcResult.getResult();
        return address;
    }

    public static String omniFundedSend(String fromAddress, String toAddress, int propertyId, String amount, String feeAddress){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().omniFundedSend(fromAddress, toAddress, propertyId, amount, feeAddress);
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("发送omni交易失败 - "+ btcRpcResult.getMsg());
        }
        String txId = btcRpcResult.getResult();
        return txId;
    }

    public static String sendToAddress(String address, BigDecimal amount){
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().sendToAddress(address, amount);
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("发送BTC交易失败 - "+ btcRpcResult.getMsg());
        }
        String txId = btcRpcResult.getResult();
        return txId;
    }

    public static BigDecimal omniBalance(String address, Integer propertyId) {
        BtcRpcResult btcRpcResult = BtcRpcUtil.getInstance().omniGetBalance(address, propertyId);
        if (btcRpcResult == null || !btcRpcResult.isSuccess()){
            throw new RuntimeException("发送omni令牌余额失败 - "+ btcRpcResult.getMsg());
        }
        OmniBalanceBean omniBalanceBean = JSON.parseObject(btcRpcResult.getResult(), OmniBalanceBean.class);
        BigDecimal balance = new BigDecimal(omniBalanceBean.getBalance());
        return balance;
    }

    public static BigDecimal getBtcBalanceByAddress(String address) {
        BigDecimal balance = BtcRpcUtil.getInstance().getBtcBalanceByAddress(address);
        return balance;
    }

    public static void main(String[] args) {
        System.out.println(omniBalance("18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ", 31));
    }
}
