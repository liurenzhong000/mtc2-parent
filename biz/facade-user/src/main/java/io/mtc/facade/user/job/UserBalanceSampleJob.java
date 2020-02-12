package io.mtc.facade.user.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.user.service.UserBalanceSampleService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 用户BHB持有金额分成
 * DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail
 * PersistJobDataAfterExecution 等待任务执行完毕以后,不执行了
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class UserBalanceSampleJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        UserBalanceSampleService userBalanceSampleService = applicationContext.getBean(UserBalanceSampleService.class);
        userBalanceSampleService.refreshPrice();
    }

}
