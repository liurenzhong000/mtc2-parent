package io.mtc.common.redis.constants;

/**
 * bch的缓存key值
 * 所有key必须以 BCH_ 开头
 *
 * @author Chinhin
 * 2018/12/10
 */
public class BchRedisKeys {

    /**
     * 平台用户钱包地址
     */
    public static final String PLATFORM_USER_PREFIX = "BCH_PLATFORM_USER:";
    public static String PLATFORM_USER(String address) {
        return PLATFORM_USER_PREFIX + address;
    }
}
