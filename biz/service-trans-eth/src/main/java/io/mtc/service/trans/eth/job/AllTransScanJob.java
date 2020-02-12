package io.mtc.service.trans.eth.job;

import io.mtc.common.quartz.support.JobSupport;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.service.trans.eth.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 全网交易的扫描定时任务
 *
 * @author Chinhin
 * 2018/6/25
 */
@Slf4j
public class AllTransScanJob extends QuartzJobBean {

    private TransactionService transactionService;
    private RedisUtil redisUtil;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext context = JobSupport.getContext(jobExecutionContext);
        if (context == null) {
            return;
        }

        transactionService = context.getBean("transactionService", TransactionService.class);
        redisUtil = context.getBean("redisUtil", RedisUtil.class);

        // 最新区块高度
        Integer newBN = redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM, Integer.class);
        // 还没有最新区块高度，就返回，等待下次轮询
        if (newBN == null){
            return;
        }

        int scannedMax;
        int scannedMin;
        int lowerLimit;

        // 扫描过的最大区块高度
        Object scannedMaxBNObj = redisUtil.get(RedisKeys.SCANNED_CONTINUITY_MAX);
        // 第一次扫描
        if (scannedMaxBNObj == null) {
            scannedMax = newBN - 1;
            scannedMin = newBN;
            lowerLimit = 0;
            redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MAX, newBN - 1);
            redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MIN, newBN);
            redisUtil.set(RedisKeys.SCAN_LOWER_LIMIT, 0);
        } else {
            scannedMax = (int) scannedMaxBNObj;
            scannedMin = (int) redisUtil.get(RedisKeys.SCANNED_CONTINUITY_MIN);
            lowerLimit = (int) redisUtil.get(RedisKeys.SCAN_LOWER_LIMIT);

            if (lowerLimit > scannedMax) {
                log.warn("扫描的最高区块，低于需要扫描的最早区块，重置...扫描");
                redisUtil.delete(RedisKeys.SCANNED_CONTINUITY_MAX);
                redisUtil.delete(RedisKeys.SCANNED_CONTINUITY_MIN);
                redisUtil.delete(RedisKeys.SCAN_LOWER_LIMIT);
                return;
            }
        }
        for (int i = newBN; i >= scannedMax + 1; i --) {
            if (getLock(i)) {
                log.info("准备扫描区块, 已经扫描范围：[{}]←→[{}]; 最新区块高度:[{}]", scannedMin, scannedMax, newBN);
                doScan(i);
//                if (newBN != redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM, Integer.class).intValue()) {
//                    newBN = redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM, Integer.class);
//                    i = newBN;
//                }
                return;
            }
        }
        for (int i = scannedMin - 1; i > lowerLimit; i --) {
            if (getLock(i)) {
                log.info("准备扫描区块, 已经扫描范围：[{}]←→[{}]; 最新区块高度:[{}]", scannedMin, scannedMax, newBN);
                doScan(i);
                return;
            }
        }
    }

    /**
     * 先判断该区块是否已经被扫描过，如果被扫描过，则返回false，如果未被扫描过，
     * 则尝试获取扫描该区块的分布式锁（因为同一个区块只能有同一个线程扫描）
     *
     * @param blockNum 要扫描的区块高度
     * @return true表示获取成功
     */
    private boolean getLock(int blockNum) {
        String inPieceKey = RedisKeys.SCANNED_PIECE(blockNum);
        Object inPiece = redisUtil.get(inPieceKey);
        if (inPiece != null) {
            return false;
        }
        String scanKey = RedisKeys.ETH_SCAN_LOCK(blockNum);
        return redisUtil.distributeLock(scanKey, 30);
    }

    /**
     * 开始扫描某个区块
     * @param blockNum 区块高度
     */
    private void doScan(int blockNum) {
        try {
            transactionService.scan(blockNum);
            log.info("扫描区块完毕:[{}]", blockNum);
            // 扫描完成后的处理
            afterScan(blockNum);
        } catch (IllegalMonitorStateException e) {
            log.info(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redisUtil.delete(RedisKeys.ETH_SCAN_LOCK(blockNum));
        }
    }

    /**
     * 扫描完某个区块后的处理
     * @param blockNum 区块
     */
    private void afterScan(int blockNum) {
        int scannedMax = (int) redisUtil.get(RedisKeys.SCANNED_CONTINUITY_MAX);
        if (blockNum > scannedMax) {
            // 如果正好比连续的Max大1，则扩大连续范围，否则存入散列中
            if (blockNum == ++ scannedMax) {
                redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MAX, scannedMax);
                // 散列中如果有刚好比扩大后的大一，则继续扩大
                while (redisUtil.get(RedisKeys.SCANNED_PIECE(++scannedMax)) != null) {
                    redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MAX, scannedMax);
                    redisUtil.delete(RedisKeys.SCANNED_PIECE(scannedMax));
                }
            } else {
                // 散列中的一个小时还未被合并，则移除
                redisUtil.set(RedisKeys.SCANNED_PIECE(blockNum), 1, 3600);
            }
        } else {
            int scannedMin = (int) redisUtil.get(RedisKeys.SCANNED_CONTINUITY_MIN);
            // 如果正好比连续的Min小1，则扩大连续范围，否则存入散列中
            if (blockNum == --scannedMin) {
                redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MIN, scannedMin);
                // 散列中如果有刚好比扩大后的小一，则继续扩大
                while (redisUtil.get(RedisKeys.SCANNED_PIECE(--scannedMin)) != null) {
                    redisUtil.set(RedisKeys.SCANNED_CONTINUITY_MIN, scannedMin);
                    redisUtil.delete(RedisKeys.SCANNED_PIECE(scannedMin));
                }
            } else {
                // 散列中的一个小时还未被合并，则移除
                redisUtil.set(RedisKeys.SCANNED_PIECE(blockNum), 1, 3600);
            }
        }
    }

}
