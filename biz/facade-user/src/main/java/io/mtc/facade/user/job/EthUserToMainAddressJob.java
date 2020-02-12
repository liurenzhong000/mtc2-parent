package io.mtc.facade.user.job;

/**
 * @Auther: hyp
 * @Date: 2019/3/10 11:34
 * @Description: eth汇总任务
 */

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
public class EthUserToMainAddressJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("#################execute EthUserToMainAddressJob job##################");
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            FundService fundService = applicationContext.getBean(FundService.class);
            fundService.ethUserToMainAddress();
        }
        log.info("#################execute EthUserToMainAddressJob job end##################");
    }

}
