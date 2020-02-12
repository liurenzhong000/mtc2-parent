package io.mtc.service.endpoint.eth.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.quartz.support.JobSupport;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.service.endpoint.eth.util.EndpointUrlFactory;
import io.mtc.service.endpoint.eth.util.GethUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.math.BigInteger;

/**
 * 获取最新blockNum的hash
 *
 * @author Chinhin
 * 2018/6/26
 */
@Slf4j
public class LastestBlockJob extends QuartzJobBean {


    private RedisUtil redisUtil;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        ApplicationContext context = JobSupport.getContext(jobExecutionContext);
        if (context == null) {
            return;
        }
        if (redisUtil == null) {
            redisUtil = context.getBean("redisUtil", RedisUtil.class);
        }

        String result = GethUtil.request(EndpointUrlFactory.getEndpointAtIndex(-1), "eth_blockNumber");
        if (result == null) {
            log.error("获取infura最新区块高度失败");
            return;
        }
        JSONObject json = JSON.parseObject(result);
        String lastBlockNumStr = json.getString("result");
        int lastBlockNum = Integer.parseInt(lastBlockNumStr.substring(2), 16);

        Object beforeBlockNumObj = redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM);
        if (beforeBlockNumObj != null) {
            int beforeBlockNum = (int) beforeBlockNumObj;
            if (beforeBlockNum >= lastBlockNum) {
                return;
            } else {
                log.info("获取infura最新区块高度成功, {} → {}", beforeBlockNum, lastBlockNum);
            }
        }
        redisUtil.set(RedisKeys.ETH_LAST_BLOCK_NUM_HEX, lastBlockNumStr);
        redisUtil.set(RedisKeys.ETH_LAST_BLOCK_NUM, lastBlockNum);
        redisUtil.set(RedisKeys.GAS_PRICE, getGasPrice());
    }

    private BigInteger getGasPrice() {
        String eth_gasPrice = GethUtil.request("https://mainnet.infura.io/DwsTJXLCQR2aMhi7QTLR", "eth_gasPrice");
        JSONObject gasPriceJson = JSON.parseObject(eth_gasPrice);
        String gasPriceStr = gasPriceJson.getString("result");
        return new BigInteger(gasPriceStr.substring(2), 16);
    }

}
