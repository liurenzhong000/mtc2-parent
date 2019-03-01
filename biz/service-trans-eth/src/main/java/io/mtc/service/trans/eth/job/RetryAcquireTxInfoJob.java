package io.mtc.service.trans.eth.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.trans.eth.service.TransactionService;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 每分钟获取次没有确认的交易记录（通过本平台的转账）
 *
 * @author Chinhin
 * 2018/7/11
 */
public class RetryAcquireTxInfoJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext context = JobSupport.getContext(jobExecutionContext);
        if (context == null) {
            return;
        }
        TransactionService transactionService = context.getBean("transactionService", TransactionService.class);

        long currentTimeMillis = System.currentTimeMillis();
        // 重试前1分钟至前3分钟的
        transactionService.retryTxBetweenTimes(currentTimeMillis - 60 * 1000 * 3, currentTimeMillis - 60 * 1000);

        long currentTimeMin = currentTimeMillis / 1000 / 60;

        // 整5分钟
        if (currentTimeMin % 5 == 0) {
            // 重试前10分钟至前5分钟的
            transactionService.retryTxBetweenTimes(currentTimeMillis - 60 * 1000 * 10, currentTimeMillis - 60 * 1000 * 5);
            // 每小时
            if (currentTimeMin % 60 == 0) {
                // 重试前120分钟至前60分钟的
                transactionService.retryTxBetweenTimes(currentTimeMillis - 60 * 1000 * 120, currentTimeMillis - 60 * 1000 * 60);

                // 每天0点
                if (currentTimeMin % 1440 == 0) {
                    // 重试近一周的
                    transactionService.retryTxBetweenTimes(currentTimeMillis - 60 * 1000 * 60 * 24 * 7, currentTimeMillis);
                }
            }
        }
    }

}
