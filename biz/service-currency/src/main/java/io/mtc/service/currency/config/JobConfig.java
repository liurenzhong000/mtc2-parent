package io.mtc.service.currency.config;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.currency.job.CreateCurrencyJob;
import io.mtc.service.currency.job.CurrencyPrice2DBJob;
import io.mtc.service.currency.job.CurrencyPriceJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;

@Configuration
public class JobConfig {

    @Resource
    private JobSupport jobSupport;

    /**
     * 币种价格更新
     */
    @Bean
    public SchedulerFactoryBean updateCurrencyScheduler() {
        return jobSupport.makeScheduler("updateCurrencyScheduler", CurrencyPriceJob.class, "*/15 * * * * ?");
    }

    /**
     * 币种价格更新到数据库, 每小时
     */
    @Bean
    public SchedulerFactoryBean currencyPrice2DBJob() {
        return jobSupport.makeScheduler("currencyPrice2DBJob", CurrencyPrice2DBJob.class, "0 0 * * * ?");
    }

    /**
     * 发币
     */
    @Bean
    public SchedulerFactoryBean createCurrencyJob() {
        return jobSupport.makeScheduler("createCurrencyJob", CreateCurrencyJob.class, "*/5 * * * * ?");
    }

}
