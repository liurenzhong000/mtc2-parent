package io.mtc.service.currency.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.currency.service.CreateCurrencyService;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.validation.constraints.NotNull;

/**
 * 创建合约的计划任务
 *
 * @author Chinhin
 * 2018/8/16
 */
public class CreateCurrencyJob extends QuartzJobBean {

    @Override
    protected void executeInternal(@NotNull JobExecutionContext jobExecutionContext) {
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        CreateCurrencyService createCurrencyService = applicationContext.getBean(CreateCurrencyService.class);
        createCurrencyService.doCreate();
    }

}
