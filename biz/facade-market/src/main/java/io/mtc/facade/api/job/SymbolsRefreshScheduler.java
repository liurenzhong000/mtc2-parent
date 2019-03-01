package io.mtc.facade.api.job;

import io.mtc.facade.api.common.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by hooger on 2018/8/10.
 */
@Slf4j
@Component
@EnableScheduling
public class SymbolsRefreshScheduler {

    @Resource
    private CacheUtil cacheUtil;

    @Scheduled(cron = "${cache.symbol.refreshcron}")
    public void refresh(){

        long startTime = System.currentTimeMillis();
        log.info("开始刷新缓存symbol...");
        cacheUtil.refreshSymbolList();
        log.info("结束刷新缓存symbol，耗时=[{}]ms",System.currentTimeMillis()-startTime);
    }

}
