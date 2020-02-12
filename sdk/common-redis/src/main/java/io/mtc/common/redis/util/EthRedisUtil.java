package io.mtc.common.redis.util;

import io.mtc.common.constants.Constants;
import io.mtc.common.redis.constants.RedisKeys;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * eth的redis工具类
 * 在redis基本功能上提取了些跨服务通用的基于缓存的共通功能
 *
 * @author Chinhin
 * 2018/7/31
 */
@Component
public class EthRedisUtil extends RedisUtil {

    /**
     * 监控用户的钱包地址是否完成
     * @return true表示完成
     */
    public boolean monitorUserHostWalletInitFinish() {
        Object o = get(RedisKeys.ETH_HOST_WALLET_ADDRESS_INIT_FINISH);
        return o != null;
    }

    /**
     * 完成了需要监控用户的钱包地址的初始化
     */
    public void finishInitMonitorUserHostWallet() {
        set(RedisKeys.ETH_HOST_WALLET_ADDRESS_INIT_FINISH, 1);
    }

    /**
     * 增加托管用户的个人钱包地址于监控缓存里面
     * @param walletAddress 钱包地址
     * @param userId 用户ID
     */
    public void monitorUserHostWallet(String walletAddress, Long userId) {
        hsetString(RedisKeys.ETH_HOST_WALLET_ADDRESS, walletAddress, String.valueOf(userId));
    }

    /**
     * 删除监控的托管用户的钱包地址
     * @param walletAddress 钱包地址
     */
    public void removeMOnitorUserHostWallet(String walletAddress) {
        hdel(RedisKeys.ETH_HOST_WALLET_ADDRESS, walletAddress);
    }

    /**
     * 判断是否为平台的托管用户的钱包地址，
     * @param walletAddress 钱包地址
     * @return 不是返回0，是返回对应用户ID
     */
    public Long isUserHostWalletAddress(String walletAddress) {
        Object result = hgetString(RedisKeys.ETH_HOST_WALLET_ADDRESS, walletAddress);
        if (result == null) {
            return 0L;
        }
        return Long.valueOf((String) result);
    }

    /**
     * 根据缓存判断是否是平台用户，对应存的位置是 facade-api:service:EthService:setLanguage(Method)
     * @param walletAddress 钱包地址
     * @return true表示是，反之亦然
     */
    public boolean isPlatformUser(String walletAddress) {
        Object store = get(RedisKeys.PLATFORM_USER(walletAddress));
        return store != null;
    }

    /**
     * 获取平台的代币集合 （代币地址:单位）
     *
     * @return 代币集合
     */
    public Map<String, String> platformCurrencyCollection() {
        Object platformCurrencyCollectionObj = get(RedisKeys.PLATFORM_CURRENCY_COLLECTION);
        if (platformCurrencyCollectionObj == null) {
            return null;
        } else {
            return  (Map<String, String>) platformCurrencyCollectionObj;
        }
    }

    /**
     * 根据代币地址获取代币简称
     * @param currencyAddress 代币地址
     * @return 简称
     */
    public String getCurrencyShortName(String currencyAddress) {
        Map<String, String> platformCurrencyCollection = platformCurrencyCollection();
        if (platformCurrencyCollection == null) {
            return Constants.EMPTY;
        } else {
            if (platformCurrencyCollection.get(currencyAddress) == null) {
                return Constants.EMPTY;
            } else {
                return platformCurrencyCollection.get(currencyAddress);
            }
        }
    }

    /**
     * 设置创建合约信息
     */
    public void setCreateContractInfo(String txHash, Long createContractId) {
        set(RedisKeys.CREATING_TOKEN_TXHASH, txHash);
        set(RedisKeys.CREATING_TOKEN_ID, createContractId);
    }

    /**
     * 获得创建中合约的交易hash
     */
    public String getCreateContractTxHash() {
        return get(RedisKeys.CREATING_TOKEN_TXHASH, String.class);
    }

    /**
     * 获得创建中合约的交易Id
     */
    public Long getCreateContractId() {
        return get(RedisKeys.CREATING_TOKEN_ID, Long.class);
    }
}
