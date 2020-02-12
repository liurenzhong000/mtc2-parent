package io.mtc.service.currency.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.repository.CurrencyRepository;
import io.mtc.service.currency.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将缓存中的价格更新到数据库里
 *
 * @author Chinhin
 * 2018/9/20
 */
@Slf4j
public class CurrencyPrice2DBJob extends QuartzJobBean {

    private CurrencyRepository currencyRepository;

    private JobService jobService;

    /**
     * 每1小时把缓存的价格信息更新到数据库
     */
    @Transactional
    public void refreshPrice() {
        // 获得来源类型非本地的币种
        Iterable<Currency> all = currencyRepository.findAll();

        log.info("刷新价格缓存到数据库");
        all.forEach(it -> {
            // 需要更新的币种
            if (it.getSourceType() != 1) {
                jobService.updateTokenPriceDB(it);
            }
        });
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext applicationContext = JobSupport.getContext(jobExecutionContext);
        if (applicationContext == null) {
            return;
        }
        currencyRepository = applicationContext.getBean(CurrencyRepository.class);
        jobService = applicationContext.getBean(JobService.class);
        refreshPrice();
    }
}
