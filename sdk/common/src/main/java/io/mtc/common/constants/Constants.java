package io.mtc.common.constants;

import java.math.BigInteger;

/**
 * 常量
 *
 * @author Chinhin
 * 2018/6/6
 */
public class Constants {

    public static final String EMPTY   = "";
    public static final String ZERO    = "0";

    /** 编码格式 **/
    public static final String UTF8    = "UTF-8";
    public static final String GBK     = "GBK";

    /** 换行符 */
    public static final String CRLF    = "\r\n";   // 0D0A
    public static final String CR      = "\r";     // 0D
    public static final String LF      = "\n";     // 0A

    public static final String SPRING_CONTEXT = "SPRING_CONTEXT"; // Spring context

    /** 以太坊地址 */
    public static final String ETH_ADDRESS = "0";
    /** MTC代币地址 */
    public static final String MTC_ADDRESS = "0xdfdc0d82d96f8fd40ca0cfb4a288955becec2088";
    /** EOS地址 */
    public static final String EOS_ADDRESS = "EOS";

    /** 阿里云oss的全路径 */
    public static final String ALI_OSS_URI = "https://zcd-wallet.oss-ap-southeast-1.aliyuncs.com";

    /** 代币计算最小精度：红包、手续费计算使用 **/
    public static final BigInteger CURRENCY_UNIT = new BigInteger("10000000000000");

    /** baseType = 4的时候 */
    public static final String BTC_ADDRESS = "btc";

    /** baseType = 2的时候 */
    public static final String BCH_ADDRESS = "bch";

    public static final String EOS_SERVER_SECRET = "xxxxxxxxxx";

    /** 比特币托管账户提现手续费 */
    public static final String BTC_WITHDRAW_FEE = "0.0001";

}
