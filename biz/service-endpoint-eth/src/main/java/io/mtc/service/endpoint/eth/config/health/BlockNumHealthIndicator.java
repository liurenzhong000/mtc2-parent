package io.mtc.service.endpoint.eth.config.health;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import io.mtc.service.endpoint.eth.util.remoteLinux.RemoteLinuxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 区块高度健康指示器
 *
 * @author Chinhin
 * 2018/6/26
 */
@Slf4j
@Component
public class BlockNumHealthIndicator implements HealthIndicator {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private Web3jPool web3JPool;

    // 获取区块高度失败次数
    private static AtomicInteger nullTimes = new AtomicInteger(0);

    @Override
    public Health health() {
        // 获取公网最新区块高度
        Object o = redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM);
        if (o == null) {
            return new Health.Builder(Status.UP).build();
        }
        int pubBlockNum = (int) o;

        // 获取本节点的区块高度
        Integer blockNumber = redisUtil.get(RedisKeys.ETH_ENDPOINT_BLOCK_NUMBER(web3JPool.getEndpointIndex()), Integer.class);
        rebootEndpointHandler(pubBlockNum, blockNumber);

        if (blockNumber == null) {
            unHealthHandler();
            log.error("健康监测请求节点失败");
            return new Health.Builder(Status.DOWN).build();
        }
        nullTimes.set(0);

        // 节点区块高度 与 公网区块高度相差10，为节点不健康
        if (blockNumber < (pubBlockNum - 10)) {
            unHealthHandler();
            log.info("健康监测[Down] 节点高度:{}, 公网高度:{}", blockNumber, pubBlockNum);
            return new Health.Builder(Status.DOWN).build();
        } else {
            healthHandler();
            log.info("健康监测， 节点高度:{}, 公网高度:{}", blockNumber, pubBlockNum);
            return new Health.Builder(Status.UP).build();
        }
    }

    /**
     * 判断是否需要重启geth节点
     * @param pubBlockNum 公网区块高度
     * @param blockNumber 本节点区块高度
     */
    private void rebootEndpointHandler(int pubBlockNum, Integer blockNumber) {
        String rebootTimeKey = RedisKeys.ETH_ENDPOINT_PRE_REBOOT_TIME(web3JPool.getEndpointIndex());

        if (blockNumber == null) {
            if (nullTimes.intValue() < 3) {
                log.error("获取节点区块高度失败: 第{}次", nullTimes.get() + 1);
                nullTimes.incrementAndGet();
                return;
            } else { // 连续3次未获得区块高度，直接重启
                log.error("获取节点区块高度失败3次，重启");
                nullTimes.set(0);
                redisUtil.set(rebootTimeKey, new Date());
                RemoteLinuxUtil.rebootGeth(web3JPool.getEndpointIndex());
                return;
            }
        }
        // 节点区块高度获取失败 或 节点区块高度落后公网100以内, 不用考虑重启
        if (!(blockNumber == 0) && (pubBlockNum - blockNumber) <= 100) {
            return;
        }
        Object rebootTimeObj = redisUtil.get(rebootTimeKey);
        // 2小时内未重启过
        if (rebootTimeObj == null || DateUtil.isBeforeSecond((Date)rebootTimeObj, new Date(), 60 * 60 * 2)) {
            log.error("重启节点【{}】的GETH应用, 公网区块高度:{}, 节点区块高度:{}, 高度差{}",
                    web3JPool.getEndpointIndex(),
                    pubBlockNum,
                    blockNumber,
                    pubBlockNum - blockNumber
            );
            redisUtil.set(rebootTimeKey, new Date());
            RemoteLinuxUtil.rebootGeth(web3JPool.getEndpointIndex());
        }
    }

    /**
     * 当此节点不健康时的处理
     */
    private void unHealthHandler() {
        // 释放连接池连接
        web3JPool.rest();
    }

    /**
     * 当此节点健康时的处理
     */
    private void healthHandler() {
        // 重新加载web3j连接池, 如果上次监测的时候，连接池正常，则不会新建连接
        web3JPool.init();
    }
}
