package io.mtc.service.endpoint.eth.config;

import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.Consumer;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.service.endpoint.eth.service.TransactionService;
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

    @Resource
    private TransactionService transactionService;

    @Bean
    public Producer ethTransPendingProducer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        return new Producer(Constants.Tag.ETH_BIZ_TRANS_COMPLETE);
    }

    @Bean
    public Consumer ethTransCompleteConsumer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        return new Consumer(Constants.Topic.MTC_BIZ_TRANS, Constants.Tag.ETH_BIZ_TRANS_PENDING, transactionService);
    }

}
