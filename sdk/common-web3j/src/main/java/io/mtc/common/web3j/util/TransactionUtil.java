package io.mtc.common.web3j.util;

import io.mtc.common.constants.Constants;
import io.mtc.common.mongo.dto.TransactionRecord;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Map;

/**
 * 交易记录相关工具
 *
 * @author Chinhin
 * 2018/6/26
 */
public class TransactionUtil {

    public static TransactionRecord makeRecordByTransaction(Transaction transaction) {
        TransactionRecord record = new TransactionRecord();
        record.setHash(transaction.getHash());
        record.setFrom(transaction.getFrom());
        record.setInput(transaction.getInput());
        // 如果是合约的交易
        if (isContractTransaction(transaction)) {
            // 合约的to都是合约地址
            record.setContractAddress(transaction.getTo());
            // 真实交易对象
            String input = transaction.getInput();

            String methodSha3 = input.substring(0, 10);
            // 不是erc20交易的记录
            if (!"0xa9059cbb".equals(methodSha3)) {
                record.setHash(null);
                return record;
            }
            record.setTo("0x" + input.substring(34, 74));
            // 转账金额
            String amountStr = input.substring(74);

            if (amountStr.length() == 0){
                record.setTokenCounts(Constants.ZERO);
            } else {
                // 转账多少代币
                BigInteger value = new BigInteger(amountStr, 16);
                record.setTokenCounts(value.toString());
            }
            record.setValue(Constants.EMPTY);
        } else {
            record.setContractAddress(Constants.ETH_ADDRESS);
            record.setTo(transaction.getTo());
            record.setTokenCounts(Constants.EMPTY);
//            record.setValue(transaction.getValue().toString());
            record.setValue(hexCheck(transaction.getValueRaw()).toString()); // rawOnly
        }

//        record.setGas(transaction.getGas());
//        record.setNonce(transaction.getNonce());
        record.setGasPrice(hexCheck(transaction.getGasPriceRaw())); // rawOnly
        record.setNonce(hexCheck(transaction.getNonceRaw())); // rawOnly

        record.setBlockHash(transaction.getBlockHash());
        try {
            record.setBlockNumber(transaction.getBlockNumber());
        } catch (Exception e) {
            record.setBlockNumber(BigInteger.ZERO);
        }
        record.setCreateTime(System.currentTimeMillis());
        return record;
    }

    /**
     * 是否是平台所包含币种的交易
     * @param transaction 交易记录
     * @param currencyMap 平台包含的币种的集合
     * @return true表示是，false表示否(需要过滤掉)
     */
    public static boolean ifPlatformCurrencyTrans(Transaction transaction, Map<String, String> currencyMap) {
        // 如果是合约的交易
        if (isContractTransaction(transaction)) {
            return currencyMap.get(transaction.getTo()) != null;
        } else {
            return true;
        }
    }

    /**
     * 判断是否是合约的交易
     * @param transaction 交易记录对象
     * @return true表示是合约的交易，false表示是eth的交易
     */
    private static boolean isContractTransaction(Transaction transaction) {
        String input = transaction.getInput();
        // eth的交易的input固定为：'0x'
        return input.length() > 74;
    }

    /**
     * 判断字符串是否合法的hex字符串，若不是则直接当做已经转为正常的来使用
     * 因为通过SpringCloud传递一次后，获取到的raw数据，已经是经过hex处理后的数值字符串
     * @param mayHexStr 可能是hex的字符串，而在交易的时候，由于是本模块调用，raw数据正常
     * @return 结果
     */
    public static BigInteger hexCheck(String mayHexStr) {
        // 不是正常的hex
        if (mayHexStr == null) {
            return BigInteger.ZERO;
        } else if (mayHexStr.length() < 3
                || !mayHexStr.startsWith("0x")) {
            return new BigInteger(mayHexStr);
        } else {
            return Numeric.decodeQuantity(mayHexStr);
        }
    }

}
