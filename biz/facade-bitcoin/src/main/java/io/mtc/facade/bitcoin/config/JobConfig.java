package io.mtc.facade.bitcoin.config;

/**
 * @Auther: hyp
 * @Date: 2019/3/15 16:31
 * @Description: 定时任务配置类
 */

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.bitcoin.job.OmniScanRechargeJob;
import io.mtc.facade.bitcoin.job.UsdtUserToMainAddressJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;

@Configuration
public class JobConfig {

    @Resource
    private JobSupport jobSupport;

    /**
     * 区块扫描，保存数据到recharge上 5分钟一次
     */
    @Bean
    public SchedulerFactoryBean omniScanRechargeJob() {
        return jobSupport.makeScheduler("OmniScanRechargeJob", OmniScanRechargeJob.class, "0 0/5 * * * ?");
    }

    /**
     * USDT 汇总 2小时一次
     */
    @Bean
    public SchedulerFactoryBean usdtUserToMainAddressJob() {
        return jobSupport.makeScheduler("UsdtUserToMainAddressJob", UsdtUserToMainAddressJob.class, "0 0 0/2 * * ?");
    }

}
