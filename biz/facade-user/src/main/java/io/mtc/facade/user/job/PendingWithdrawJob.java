package io.mtc.facade.user.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.user.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 打包交易队列消费
 *
 * @author Chinhin
 * 2018/7/30
 */
public class PendingWithdrawJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])
                || "test".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            FundService fundService = applicationContext.getBean(FundService.class);
            fundService.pendingTransConsume();
        }
    }

}
