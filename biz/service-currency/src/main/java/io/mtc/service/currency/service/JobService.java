package io.mtc.service.currency.service;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.NumberUtil;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 用于为quartz的job服务
 *
 * @author Chinhin
 * 2018/9/20
 */
@Service
public class JobService {

    @Resource
    private RateCacheUtil rateCacheUtil;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CurrencyRepository currencyRepository;

    /**
     * 刷新币种价格到缓存
     * @param address 代币地址
     * @param price 价格（美元）
     * @param changeHourly 每小时变化
     */
    public void updateTokenPriceCache(String address, BigDecimal price, BigDecimal changeHourly) {
        redisUtil.set(RedisKeys.ETH_TOKEN_PRICE(address.toLowerCase()), price);
        redisUtil.set(RedisKeys.ETH_TOKEN_CNY_PRICE(address.toLowerCase()), NumberUtil.scale(price.multiply(rateCacheUtil.getUSD2CNY()), 7)) ;
        redisUtil.set(RedisKeys.ETH_TOKEN_CHANGE(address.toLowerCase()), changeHourly);
    }

    /**
     * 刷新币种价格到缓存
     * @param address 代币地址
     * @param price 价格（人民币）
     * @param changeHourly 每小时变化
     */
    public void updateTokenPriceCacheCNY(String address, BigDecimal price, BigDecimal changeHourly) {
        BigDecimal rate = rateCacheUtil.getUSD2CNY();
        BigDecimal usdPrice = price.divide(rate, 7, BigDecimal.ROUND_HALF_UP);
        redisUtil.set(RedisKeys.ETH_TOKEN_PRICE(address.toLowerCase()), usdPrice);
        redisUtil.set(RedisKeys.ETH_TOKEN_CNY_PRICE(address.toLowerCase()), price);
        redisUtil.set(RedisKeys.ETH_TOKEN_CHANGE(address.toLowerCase()), changeHourly);
    }

    /**
     * 刷新代币的价格到数据库
     * 注意：与缓存对账
     * @param it 代币实例
     */
    public void updateTokenPriceDB(Currency it) {
        BigDecimal price = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(it.getAddress().toLowerCase()), BigDecimal.class);
        if (price != null) {
            it.setPrice(price);
        }
        BigDecimal cnyPrice = redisUtil.get(RedisKeys.ETH_TOKEN_CNY_PRICE(it.getAddress().toLowerCase()), BigDecimal.class);
        if (cnyPrice != null) {
            it.setCnyPrice(cnyPrice);
        }
        BigDecimal changeHourly = redisUtil.get(RedisKeys.ETH_TOKEN_CHANGE(it.getAddress().toLowerCase()), BigDecimal.class);
        if (changeHourly != null) {
            it.setChangeHourly(changeHourly);
        }
        currencyRepository.save(it);
    }

}
