package io.mtc.facade.bitcoin.config;

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

    //  发送usdt充值提醒给facade-user
    @Bean
    public Producer usdtRechargeProducer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        return new Producer(Constants.Tag.OMNI_BIZ_HOST_WALLET_TRANS);
    }

}