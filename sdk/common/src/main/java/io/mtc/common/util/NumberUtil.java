package io.mtc.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 计算工具类
 *
 * @author Chinhin
 * 2018/6/6
 */
public final class NumberUtil {

    /**
     * 设置精度（四舍五入）
     */
    public static BigDecimal scale(BigDecimal decimal, int scale) {
        return decimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal scale2(BigDecimal decimal) {
        return scale(decimal, 2);
    }

    /**
     * 除法:d1/d2,保留2位小数,四舍五入  - 除法需要设置精度,否则若出现无限循环的小数会抛异常
     */
    public static BigDecimal divide(BigDecimal decimal, BigDecimal divisor) {
        return decimal.divide(divisor, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 乘法:d1*d2，四舍五入保留两位小数
     */
    public static BigDecimal multiply(BigDecimal d1, BigDecimal d2) {
        return scale2(d1.multiply(d2));
    }

    /** 100 */
    private static final BigDecimal DEC100 = new BigDecimal(100);

    /**
     * 分转换为元
     * @param cent
     * @return
     */
    public static BigDecimal cent2Yuan(long cent) {
        return divide(new BigDecimal(cent), DEC100);
    }

    /**
     * 元转换为分
     * @param yuan
     * @return
     */
    public static int yuan2Cent(BigDecimal yuan) {
        return yuan.multiply(DEC100).intValue();
    }

    /**
     * 获得biginteger，主要是带小数点的也可以转换
     * @param amountStr 金额
     * @return 结果
     */
    public static BigInteger toBigInteger(String amountStr) {
        BigDecimal amountDecimal = new BigDecimal(amountStr);
        return amountDecimal.toBigInteger();
    }

}
