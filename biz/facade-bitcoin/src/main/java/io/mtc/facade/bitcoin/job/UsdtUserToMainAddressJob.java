package io.mtc.facade.bitcoin.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.bitcoin.service.OmniService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Auther: hyp
 * @Date: 2019/3/17 10:20
 * @Description: usdt汇总任务
 */
@Slf4j
public class UsdtUserToMainAddressJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("#################usdt汇总任务 START##################");
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            OmniService omniService = applicationContext.getBean(OmniService.class);
            omniService.usdtUserToMainAddress();
        }
        log.info("#################usdt汇总任务 END##################");
    }

}
