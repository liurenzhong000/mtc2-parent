package io.mtc.common.constants;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 交易配置常量类
 *
 * @author Chinhin
 * 2018/7/30
 */
public class TransactionConstants {

    /**
     * 实际的gasPrice与公网gasPrice的比率
     */
    private static final BigDecimal GAS_PRICE_RATE = new BigDecimal(1.3);

    /**
     * 预扣的gas数量
     */
    public static final BigInteger GAS_AMOUNT = BigInteger.valueOf(100000);

    /**
     * 获取应该使用的gasPrice
     * @param gasPrice 公网获取的gasPrice
     * @return 应该使用的gasPrice
     */
    public static BigInteger getUseGasPrice(BigInteger gasPrice) {
        return new BigDecimal(gasPrice).multiply(TransactionConstants.GAS_PRICE_RATE).toBigInteger();
    }

}
