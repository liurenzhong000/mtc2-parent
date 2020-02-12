package io.mtc.common.redis.constants;

/**
 * btc的缓存key值
 * 所有key必须以 BTC_ 开头
 *
 * @author Chinhin
 * 2018/12/10
 */
public class BtcRedisKeys {

    // 节点区块高度
    public static final String ENDPOINT_BLOCK_HEIGHT = "BTC_ENDPOINT_BLOCK_HEIGHT";

    // 余额
    public static String BALANCE(String address) {
        return BALANCE_PREFIX + ":" + address;
    }
    private static final String BALANCE_PREFIX = "BTC_BALANCE:";

    /**
     * 平台用户钱包地址
     */
    public static final String PLATFORM_USER_PREFIX = "BTC_PLATFORM_USER:";
    public static String PLATFORM_USER(String address) {
        return PLATFORM_USER_PREFIX + address;
    }

    /*-------------扫描用------------*/
    // 扫描完成的连续区块的最小值
    public static final String SCANNED_CONTINUITY_MAX = "BTC_SCANNED_CONTINUITY_MAX";

    // 扫描完成的分散的blockNum
    public static final String SCANNED_PIECE_PREFIX = "BTC_SCANNED_PIECE:";
    public static String SCANNED_PIECE(int blockNum) {
        return SCANNED_PIECE_PREFIX + blockNum;
    }

    // 正在扫描的交易的锁定
    public static final String LOCK_SCAN_TX_PREFIX = "BTC_LOCK_SCAN_TX:";
    public static String LOCK_SCAN_TX(String txHash) {
        return LOCK_SCAN_TX_PREFIX + txHash;
    }

    public static final String BTC_SCANNING_BLOCK_TXS_PREFIX = "BTC_SCANNING_BLOCK_TXS:";
    public static String BTC_SCANNING_BLOCK_TXS(int blockNum) {
        return BTC_SCANNING_BLOCK_TXS_PREFIX + blockNum;
    }

    /**
     * 扫描某个区块的锁
     * @param blockNum 区块高度
     * @return 锁名字
     */
    public static String BTC_SCAN_LOCK(int blockNum) {
        return BTC_SCAN_LOCK_PREFIX + blockNum;
    }
    public static final String BTC_SCAN_LOCK_PREFIX = "BTC_SCAN_LOCK:";

}
