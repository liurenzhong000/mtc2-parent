package io.mtc.service.trans.eth.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.common.util.DateUtil;
import io.mtc.service.trans.eth.service.CleanMongoService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * 定时清理mongodb的数据
 *
 * @author Chinhin
 * 2018/6/25
 */
@Slf4j
public class CleanMongoDataJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext context = JobSupport.getContext(jobExecutionContext);
        if (context == null) {
            return;
        }
        String scanMaxMinutesBeforeNowStr = context.getEnvironment().getProperty("scanMaxMinutesBeforeNow");
        if (scanMaxMinutesBeforeNowStr == null) {
            log.error("清楚全网交易记录遇到错误，配置不存在");
            return;
        }
        // 这么之前的区块数就够了
        int scanMaxMinutesBeforeNow = Integer.parseInt(scanMaxMinutesBeforeNowStr);

        long willCleanTimestamp = System.currentTimeMillis() - (long) scanMaxMinutesBeforeNow * 60 * 1000;
        String dateStr = DateUtil.formatDate(new Date(willCleanTimestamp), "yyyy-MM-dd HH:mm:ss");
        log.info("将要清楚数据，[{}]前的数据会被清除", dateStr);
        CleanMongoService cleanMongoService = context.getBean("cleanMongoService", CleanMongoService.class);
        cleanMongoService.cleanBeforeTimes(willCleanTimestamp);
    }

}
