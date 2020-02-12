package io.mtc.service.endpoint.eth.job;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;

/**
 * 获取连接节点的区块高度
 *
 * @author Chinhin
 * 2018/7/24
 */
@Slf4j
@Service
public class EndpointBlockNumber {

    @Resource
    private Web3jPool web3JPool;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 每5秒获取次节点区块高度
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void refreshBlockNumber() {
        BigInteger blockNumber = getBlockNumber();
        if (blockNumber != null) {
            redisUtil.set(RedisKeys.ETH_ENDPOINT_BLOCK_NUMBER(web3JPool.getEndpointIndex()), blockNumber.intValue(), 60);
        }
    }

    /**
     * 获取节点区块高度
     * @return 区块高度
     */
    private BigInteger getBlockNumber() {
        BigInteger blockNumber = null;
        Web3j connection = web3JPool.getNewConnection();
        try {
            blockNumber = connection.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
            connection.shutdown();
        }
        return blockNumber;
    }

}
