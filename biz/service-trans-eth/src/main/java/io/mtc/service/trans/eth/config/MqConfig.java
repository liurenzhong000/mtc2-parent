package io.mtc.service.trans.eth.config;

import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.Producer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 消息队列
 *
 * @author Chinhin
 * 2018/7/28
 */
@Configuration
public class MqConfig {

    @Resource
    private ApplicationContext applicationContext;

    @Bean
    public Producer ethTransPendingProducer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        return new Producer(Constants.Tag.ETH_TRANS_NOTIFI);
    }

    @Bean
    public Producer ethHostWalletTransProducer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            return new Producer(Constants.Tag.ETH_BIZ_HOST_WALLET_TRANS_PROD);
        } else {
            return new Producer(Constants.Tag.ETH_BIZ_HOST_WALLET_TRANS);
        }
    }

}