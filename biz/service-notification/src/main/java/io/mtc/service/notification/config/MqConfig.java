package io.mtc.service.notification.config;

import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.Consumer;
import io.mtc.service.notification.service.NotificationService;
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
    private NotificationService notificationService;

    @Bean
    public Consumer ethTransCompleteConsumer() {
        Constants.setEnv(applicationContext.getEnvironment().getActiveProfiles()[0]);
        return new Consumer(Constants.Topic.MTC_BIZ_TRANS, Constants.Tag.ETH_TRANS_NOTIFI, notificationService);
    }

}
