package io.mtc.common.mq.aliyun;

import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 阿里云mq消费者
 *
 * @author Chinhin
 * 2018/7/28
 */
@Slf4j
public class Consumer {

    /**
     * 创建消费者
     *
     * @param topic 枚举，订阅的topic (mtc_notify) 和消费者id：（CID_mtc_notify_consumer）
     * @param tag 订阅的tag ("TagA||TagB"[多个tag], *[全部tag])
     */
    public Consumer(Constants.Topic topic, Constants.Tag tag, MsgHandler handler) {
        Properties properties = new Properties();
        // 控制台创建的 Consumer ID
        properties.put(PropertyKeyConst.ConsumerId, tag.getConsumerId());
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, Constants.AccessKey);
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, Constants.SecretKey);
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr, Constants.getONSAddr());
        com.aliyun.openservices.ons.api.Consumer consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(topic.getName(), tag.name(), (message, context) -> {
            String result = new String(message.getBody());
            return handler.doConsume(result);
        });
        consumer.start();
        log.info("Consumer:{}  bindTopic:{} Tag:{} started", tag.getConsumerId(), topic, tag);
    }

}
