package io.mtc.common.mq.aliyun;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 阿里云mq生产者
 *
 * @author Chinhin
 * 2018/7/28
 */
@Slf4j
public class Producer {

    private com.aliyun.openservices.ons.api.Producer producer;

    /**
     * 创建消费者
     *
     * @param tag 枚举，主要提供producerId（控制台创建的 Producer ID）
     */
    public Producer(Constants.Tag tag) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.GROUP_ID, tag.getGroupId());
        properties.put(PropertyKeyConst.AccessKey, Constants.AccessKey);
        properties.put(PropertyKeyConst.SecretKey, Constants.SecretKey);
        //设置发送超时时间，单位毫秒
        properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, "3000");
        properties.put(PropertyKeyConst.NAMESRV_ADDR, Constants.getONSAddr());
        producer = ONSFactory.createProducer(properties);
        producer.start();
    }

    /**
     * 发送消息
     * @param topic 发送的topic
     * @param tag 发送的tag
     * @param obj 可以是任何二进制形式的数据，需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
     * @param key 设置代表消息的业务关键属性，请尽可能全局唯一
     *            以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
     *            注意：不设置也不会影响消息正常收发
     * @return true表示发送成功，反之失败
     */
    public boolean send(Constants.Topic topic, Constants.Tag tag, Object obj, String key) {
        boolean result = false;
        Message msg = new Message(topic.getName(), tag.name(), CommonUtil.toJson(obj).getBytes());
        if (StringUtil.isNotBlank(key)) {
            msg.setKey(key);
        }
        try {
            SendResult sendResult = producer.send(msg);
            // 同步发送消息，只要不抛异常就是成功
            if (sendResult != null) {
                result = true;
                log.info("Send mq message success. Topic is: {}, msgId is: {}", msg.getTopic(), sendResult.getMessageId());
            }
        }
        catch (Exception e) {
            result = false;
            log.error("Send mq message failed. Topic is: {}", msg.getTopic());
            e.printStackTrace();
        }
        return result;
    }

}
