package io.mtc.service.endpoint.eth.config;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.endpoint.eth.job.LastestBlockJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;

/**
 * 计划任务
 *
 * @author Chinhin
 * 2018/6/26
 */
@Configuration
public class JobConfig {

    @Resource
    private JobSupport jobSupport;

    /**
     * 最新区块高度
     */
    @Bean
    public SchedulerFactoryBean lastBlockNumScheduler() {
        return jobSupport.makeScheduler("lastBlockNumScheduler", LastestBlockJob.class, "*/5 * * * * ?");
    }

}
