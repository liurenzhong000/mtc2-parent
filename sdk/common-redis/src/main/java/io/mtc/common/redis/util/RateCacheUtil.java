package io.mtc.common.redis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.redis.constants.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 币种工具类
 *
 * @author Chinhin
 * 2018/6/17
 */
@Slf4j
@Component
public class RateCacheUtil {

    private static final BigDecimal defaultRate = new BigDecimal("6.4379");

    @Resource
    private RedisUtil redisUtil;

    /**
     * 美元兑人民币汇率
     * @return 汇率
     */
    public BigDecimal getUSD2CNY() {
        Object o = redisUtil.get(RedisKeys.USD2CNY_RATE);
        if (o != null) {
            return new BigDecimal(o.toString());
        } else {
            // 35ebf6ca0b21b29e48589cf7e258fc17
            String string = HttpUtil.get("http://op.juhe.cn/onebox/exchange/currency?from=USD&to=CNY&key=d7238670efa3542fe4d3d94b1619a2c6");
            if (string == null) {
                return defaultRate;
            }
            try {
                JSONObject json = JSON.parseObject(string);
                JSONArray result = json.getJSONArray("result");
                String rate = result.getJSONObject(0).getString("exchange");
                log.info("更新汇率为：{}", rate);
                // 汇率半小时更新一次
                redisUtil.set(RedisKeys.USD2CNY_RATE, rate, 1800);
                return new BigDecimal(rate);
            } catch (Exception e) {
                e.printStackTrace();
                return defaultRate;
            }
        }
    }

}
