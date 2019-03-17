package io.mtc.facade.bitcoin.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.bitcoin.service.RechargeService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Auther: hyp
 * @Date: 2019/3/15 16:38
 * @Description: USDT区块扫描
 */
@Slf4j
public class OmniScanRechargeJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("#################USDT区块扫描 job##################");
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            RechargeService rechargeService = applicationContext.getBean(RechargeService.class);
            rechargeService.omniScanRecharge();
        }
        log.info("#################USDT区块扫描 完成##################");
    }
}
