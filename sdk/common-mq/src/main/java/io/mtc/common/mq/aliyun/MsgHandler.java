package io.mtc.common.mq.aliyun;

import com.aliyun.openservices.ons.api.Action;

/**
 * 消费者处理
 *
 * @author Chinhin
 * 2018/7/28
 */
public interface MsgHandler {

    Action doConsume(String json);

}
