package io.mtc.service.trans.eth.config;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.trans.eth.job.AllTransScanJob;
import io.mtc.service.trans.eth.job.CleanMongoDataJob;
import io.mtc.service.trans.eth.job.RetryAcquireTxInfoJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;

@Configuration
public class JobConfig {

    @Resource
    private JobSupport jobSupport;

    /**
     * 全网交易扫描
     */
    @Bean
    public SchedulerFactoryBean allTransScanScheduler() {
        return jobSupport.makeScheduler("allTransScanScheduler", AllTransScanJob.class, "*/1 * * * * ?");
    }


    /**
     * 重试获取未确认的交易记录
     */
    @Bean
    public SchedulerFactoryBean retryAcquireTxInfo() {
        return jobSupport.makeScheduler("retryAcquireTxInfo", RetryAcquireTxInfoJob.class, "0 */1 * * * ?");
    }

    /**
     * 定时清理mongodb的数据
     */
    @Bean
    public SchedulerFactoryBean removeMongoDataJobScheduler() {
        // 每晚两点执行
        return jobSupport.makeScheduler("removeMongoDataJobScheduler", CleanMongoDataJob.class, "0 0 2 * * ?");
    }

}
