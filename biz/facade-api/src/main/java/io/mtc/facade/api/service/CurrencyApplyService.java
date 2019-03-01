package io.mtc.facade.api.service;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.facade.api.feign.ServiceEndpointEth;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 申请代币的service
 *
 * @author Chinhin
 * 2018/8/16
 */
@Service
public class CurrencyApplyService {

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private RedisUtil redisUtil;

    public String getNameByAddress(String address) {
        String name = redisUtil.get(RedisKeys.CURRENCY_NAME(address), String.class);
        if (name == null) {
            name = serviceEndpointEth.getNameByAddress(address);
            redisUtil.set(RedisKeys.CURRENCY_NAME(address), name, 300);
        }
        return name;
    }

    public String getSymbolByAddress(String address) {
        String symbol = redisUtil.get(RedisKeys.CURRENCY_SYMBOL(address), String.class);
        if (symbol == null) {
            symbol = serviceEndpointEth.getSymbolByAddress(address);
            redisUtil.set(RedisKeys.CURRENCY_SYMBOL(address), symbol, 300);
        }
        return symbol;
    }

}
