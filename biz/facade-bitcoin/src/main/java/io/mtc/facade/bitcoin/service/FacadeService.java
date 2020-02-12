package io.mtc.facade.bitcoin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.dto.CurrencyBean;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.bitcoin.feign.ServiceCurrency;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: hyp
 * @Date: 2019/3/17 10:42
 * @Description: 从其他服务获取一些通用信息
 */
@Service
public class FacadeService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ServiceCurrency serviceCurrency;

    /**
     * 获取支持托管的币种
     * @return 币种一览
     */
    public JSONArray getCurrencyList() {
        Map result;
        // 币种一览
        Object listObj = redisUtil.get(RedisKeys.ENABLE_HOST_CURRENCY);
        if (listObj != null) {
            result = (Map) listObj;
        } else {
            String s = serviceCurrency.hostEnableCurrency();
            result = CommonUtil.fromJson(s, HashMap.class);
            // 60秒钟刷新一次缓存
            redisUtil.set(RedisKeys.ENABLE_HOST_CURRENCY, result, 60);
        }
        return (JSONArray) result.get("result");
    }

    //  获取currencyType=5的币种，当前只有usdt
    public List<CurrencyBean> getOmniCurrencyList(){
        JSONArray hostEnableCurrencyArray = getCurrencyList();
        if (hostEnableCurrencyArray == null || hostEnableCurrencyArray.size() <= 0) {
            return null;
        }
        // 获取支持托管的omni币种
        List<CurrencyBean> currencyList = hostEnableCurrencyArray
                .stream()
                .map(obj -> JSON.toJavaObject(((JSONObject) obj), CurrencyBean.class))
                .filter(item -> item.getBaseType().equals(5))
                .collect(Collectors.toList());
        return currencyList;
    }

    //  获取本地托管的USDT用户地址
    public Map<String, String> getOmniHostUserWalletMap(){
        Map<String, String> hostUserWalletMap = redisUtil.hget(RedisKeys.USDT_HOST_WALLET_ADDRESS);
        return hostUserWalletMap;
    }

}
