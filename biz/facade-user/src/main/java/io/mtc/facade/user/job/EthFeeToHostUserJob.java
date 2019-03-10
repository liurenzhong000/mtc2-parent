package io.mtc.facade.user.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.facade.user.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 09:12
 * @Description: 给本地托管用户钱包地址转入ETH手续费
 */
@Slf4j
public class EthFeeToHostUserJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("#################execute FeeToHostUserJob job##################");
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            FundService fundService = applicationContext.getBean(FundService.class);
            fundService.ethFeeToHostUser();
        }
        log.info("#################execute FeeToHostUserJob job end##################");
    }

}
