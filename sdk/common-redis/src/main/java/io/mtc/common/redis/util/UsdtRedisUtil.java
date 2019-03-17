package io.mtc.common.redis.util;

import io.mtc.common.redis.constants.RedisKeys;
import org.springframework.stereotype.Component;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 10:44
 * @Description:
 */
@Component
public class UsdtRedisUtil extends RedisUtil{

    /**
     * 监控用户的USDT钱包地址是否完成
     * @return true表示完成
     */
    public boolean monitorUserHostWalletInitFinish() {
        Object o = get(RedisKeys.USDT_HOST_WALLET_ADDRESS_INIT_FINISH);
        return o != null;
    }

    /**
     * 增加托管用户的个人钱包地址于监控缓存里面
     * @param walletAddress 钱包地址
     * @param userId 用户ID
     */
    public void monitorUserHostWallet(String walletAddress, Long userId) {
        hsetString(RedisKeys.USDT_HOST_WALLET_ADDRESS, walletAddress, String.valueOf(userId));
    }

    /**
     * 完成了需要监控用户的钱包地址的初始化
     */
    public void finishInitMonitorUserHostWallet() {
        set(RedisKeys.USDT_HOST_WALLET_ADDRESS_INIT_FINISH, 1);
    }

}
