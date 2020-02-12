package io.mtc.service.currency.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.quartz.support.JobSupport;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.repository.CurrencyRepository;
import io.mtc.service.currency.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 币种价格刷新定时任务
 *
 * @author Chinhin
 * 2018/6/20
 */
@Slf4j
@Transactional(readOnly = true)
public class CurrencyPriceJob extends QuartzJobBean {

    private CurrencyRepository currencyRepository;

    private RedisUtil redisUtil;

    private JobService jobService;

    /**
     * 每10秒执行一次币种价格刷新
     */
    private void refreshPrice() {
        // 获得来源类型非本地的币种
        Iterable<Currency> all = currencyRepository.findAll();

        Map<String, String> platformCurrencyMap = new HashMap<>();
        all.forEach(it -> {
            // 来源为block.cc的币种
            if (it.getSourceType() == 2) {
                updateBCCurrencyCache(it);
            } else if (it.getSourceType() == 3) {
                updateAipTradeCache(it);
            }
            platformCurrencyMap.put(it.getAddress(), it.getShortName());
        });
        redisUtil.set(RedisKeys.PLATFORM_CURRENCY_COLLECTION, platformCurrencyMap);
    }

    /**
     * 从AIP交易所获取数据
     * @param currency 代币
     */
    private void updateAipTradeCache(Currency currency) {
        if (StringUtil.isEmpty(currency.getSourceSystemId())) {
            log.error("请设置【{}】币种的交易所对应id", currency.getShortName());
            return;
        }
        String url = "http://47.88.237.228:8080/v2/tradeInfo.html?id=" + currency.getSourceSystemId();
        String string = HttpUtil.get(url);
        if (string == null) {
            return;
        }
        try {
            JSONObject json = JSON.parseObject(string);
            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.size(); i ++) {
                JSONObject temp = data.getJSONObject(i);
                if (currency.getShortName().equals(temp.getString("sellSymbol"))) {
                    BigDecimal nowPrice = temp.getBigDecimal("p_new");
                    // 开盘价
                    BigDecimal openPrice = temp.getBigDecimal("p_open");
                    // 涨跌幅
                    BigDecimal rate = nowPrice.subtract(openPrice).divide(openPrice, 4, RoundingMode.CEILING);

                    log.info("AIP {}:{}", currency.getShortName(), nowPrice);

                    jobService.updateTokenPriceCacheCNY(currency.getAddress(), nowPrice, rate);
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            log.info("BLOCKCC Parsing Json error occurred {}", e);
            return;
        }
        log.error("BLOCKCC {}", currency.getShortName());
    }

    /**
     * 通过blockcc获取单个币种的最新情报，并更新缓存
     * @param currency 要更新币种的缓存
     */
    private void updateBCCurrencyCache(Currency currency) {
        String url = "https://data.block.cc/api/v1/price?symbol=" + currency.getShortName();
        String string = HttpUtil.get(url);
        if (string == null) {
            return;
        }
        try {
            JSONObject json = JSON.parseObject(string);
            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.size(); i ++) {
                JSONObject temp = data.getJSONObject(i);
                if (currency.getName().equals(temp.getString("name"))) {
                    log.info("BLOCKCC {}:{}", currency.getShortName(), temp.getDoubleValue("price"));
                    jobService.updateTokenPriceCache(currency.getAddress(),
                            temp.getBigDecimal("price"), temp.getBigDecimal("change_hourly"));
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            log.info("BLOCKCC Parsing Json error occurred {}", e);
            return;
        }
        log.error("BLOCKCC {}", currency.getShortName());
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        currencyRepository = applicationContext.getBean(CurrencyRepository.class);
        jobService = applicationContext.getBean(JobService.class);
        redisUtil = applicationContext.getBean("redisUtil", RedisUtil.class);
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])
                || "test".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            refreshPrice();
        } else {
            log.info("更新币种价格计划任务mock");
        }
    }
}
