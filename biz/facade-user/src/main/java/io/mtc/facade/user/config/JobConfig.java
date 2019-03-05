package io.mtc.facade.user.config;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.user.job.BtcPendingWithdrawJob;
import io.mtc.facade.user.job.CleanRedEnvelopJob;
import io.mtc.facade.user.job.PendingWithdrawJob;
import io.mtc.facade.user.job.ResetUserWheelNumJob;
import io.mtc.facade.user.job.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.Resource;

@Configuration
public class JobConfig {

    @Resource
    private JobSupport jobSupport;

    /**
     * 提现打包
     */
    @Bean
    public SchedulerFactoryBean pendingWithdrawJob() {
        return jobSupport.makeScheduler("PendingWithdrawJob", PendingWithdrawJob.class, "*/10 * * * * ?");
    }

    /**
     * BTC提现打包
     */
    @Bean
    public SchedulerFactoryBean btcPendingWithdrawJob() {
        return jobSupport.makeScheduler("BtcPendingWithdrawJob", BtcPendingWithdrawJob.class, "*/10 * * * * ?");
    }

    /**
     * 清理红包(每天4点)
     */
    @Bean
    public SchedulerFactoryBean cleanRedEnvelopJob() {
        return jobSupport.makeScheduler("CleanRedEnvelopJob", CleanRedEnvelopJob.class, "0 0 4 * * ?");
    }

    /**
     * 重置用户转盘抽奖次数(每天0点)
     */
    @Bean
    public SchedulerFactoryBean resetUserWheelNumJob() {
        return jobSupport.makeScheduler("ResetUserWheelNumJob", ResetUserWheelNumJob.class, "0 0 0 * * ?");
    }


    /**
     * 每小时刷新用户BHB,持有金额
     */
    @Bean
    public SchedulerFactoryBean userBalanceSampleJob() {
        return jobSupport.makeScheduler("UserBalanceSampleJob", UserBalanceSampleJob.class, "0 0 * * * ?");
    }

    /**
     * 每天计算用户BHB分成
     */
    @Bean
    public SchedulerFactoryBean dividendJob() {
        return jobSupport.makeScheduler("DividendJob", DividendJob.class, "0 0 0 * * ?");
    }


}
