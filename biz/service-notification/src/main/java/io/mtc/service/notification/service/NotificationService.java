package io.mtc.service.notification.service;

import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.constants.MessageConstants;
import io.mtc.common.dto.TransInfo;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.notification.entity.Notification;
import io.mtc.service.notification.entity.NotificationTemplet;
import io.mtc.service.notification.repository.NotificationRepository;
import io.mtc.service.notification.repository.NotificationTempletRepository;
import io.mtc.service.notification.util.MessageUtil;
import io.mtc.service.notification.util.NotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 通知服务类
 *
 * @author Chinhin
 * 2018/7/10
 */
@Slf4j
@Service
public class NotificationService implements MsgHandler {

    @Resource
    private NotificationRepository notificationRepository;

    @Resource
    private NotificationTempletRepository notificationTempletRepository;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private MessageUtil messageUtil;

    /**
     * 全局推送通知
     * @param templateId 模板ID
     */
    public void push(long templateId) {
        log.info("推送模板 {}", templateId);
        NotificationTemplet template = notificationTempletRepository.findById(templateId).get();

        Set<String> platformUserKeys = ethRedisUtil.getKeysBeginWith(RedisKeys.PLATFORM_USER_PREFIX);
        // 所有钱包地址集合
        for (String temp : platformUserKeys) {
            String tempAddress = temp.substring(14);
            // 获取该用户的语言
            int langCode = (int) ethRedisUtil.get(RedisKeys.PLATFORM_USER(tempAddress));

            Notification notification = new Notification();
            notification.setAddress(tempAddress);

            if (langCode == MessageConstants.LANG_CH) {
                notification.setTitle(template.getTitle());
                notification.setContent(template.getContent());
                notification.setUrl(template.getUrl());
            } else if (langCode == MessageConstants.LANG_EN) {
                notification.setTitle(template.getTitleEN());
                notification.setContent(template.getContentEN());
                notification.setUrl(template.getUrlEN());
            } else if (langCode == MessageConstants.LANG_KO) {
                notification.setTitle(template.getTitleKO());
                notification.setContent(template.getContentKO());
                notification.setUrl(template.getUrlKO());
            }
            notification.setType(2);
            notificationRepository.save(notification);
        }

        new Thread(() -> {
            Notification notificationZh = new Notification();
            notificationZh.setAddress("custom_notify_zh");
            notificationZh.setTitle(template.getTitle());
            notificationZh.setContent(template.getContent());
            notificationZh.setUrl(template.getUrl());
            notificationZh.setType(2);
            NotificationUtil.pushTagNotification(notificationZh, "custom_notify_zh");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Notification notificationEn = new Notification();
            notificationEn.setAddress("custom_notify_en");
            notificationEn.setTitle(template.getTitleEN());
            notificationEn.setContent(template.getContentEN());
            notificationEn.setUrl(template.getUrlEN());
            notificationEn.setType(2);
            NotificationUtil.pushTagNotification(notificationEn, "custom_notify_en");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Notification notificationKo = new Notification();
            notificationKo.setAddress("custom_notify_ko");
            notificationKo.setTitle(template.getTitleKO());
            notificationKo.setContent(template.getContentKO());
            notificationKo.setUrl(template.getUrlKO());
            notificationKo.setType(2);
            if (StringUtil.isNotBlank(template.getTitleKO()) && StringUtil.isNotBlank(template.getContentKO())) {
                NotificationUtil.pushTagNotification(notificationKo, "custom_notify_ko");
            }
        }).start();
    }

    /**
     * 交易消息消费
     */
    @Override
    public Action doConsume(String json) {

        log.info("将要推送：{}", json);
        TransInfo transInfo = CommonUtil.fromJson(json, TransInfo.class);

        Notification txRecord = notificationRepository.findByTxHash(transInfo.getTxHash());
        // 已经推过这笔交易记录
        if (txRecord != null) {
            return Action.CommitMessage;
        }

        Object[] transContentArgs = {CommonUtil.getFormatAmount(transInfo.getAmount()).toPlainString(), transInfo.getShotName()};
        // 发币方是平台用户，要给他推送
        if (ethRedisUtil.isPlatformUser(transInfo.getFrom())) {
            // 获取该用户的语言
            int langCode = (int) ethRedisUtil.get(RedisKeys.PLATFORM_USER(transInfo.getFrom()));
            Notification notification = new Notification();
            notification.setAddress(transInfo.getFrom());
            notification.setTitle(messageUtil.getMessage(MessageConstants.trans_title, null, langCode));
            notification.setTxHash(transInfo.getTxHash());
            notification.setIsSender(true);
            notification.setOtherAddress(transInfo.getTo());
            notification.setType(1);
            String msgKey = MessageConstants.trans_from_success_content;
            // 失败的情况
            if (!transInfo.getIsSuccess()) {
                msgKey = MessageConstants.trans_from_failure_content;
            }
            notification.setContent(messageUtil.getMessage(msgKey, transContentArgs, langCode));
            notificationRepository.save(notification);
            log.info("转出方信息： {}", CommonUtil.toJson(notification));
            NotificationUtil.pushNotification(notification);
        }
        // 收币方是平台用户，要给他推送
        if (ethRedisUtil.isPlatformUser(transInfo.getTo())) {
            if (!transInfo.getIsSuccess()) {
                return Action.CommitMessage;
            }
            // 获取该用户的语言
            int langCode = (int) ethRedisUtil.get(RedisKeys.PLATFORM_USER(transInfo.getTo()));
            Notification notification = new Notification();
            notification.setAddress(transInfo.getTo());
            notification.setTitle(messageUtil.getMessage(MessageConstants.trans_title, null, langCode));
            notification.setTxHash(transInfo.getTxHash());
            notification.setIsSender(false);
            notification.setOtherAddress(transInfo.getFrom());
            notification.setType(1);
            String content = messageUtil.getMessage(MessageConstants.trans_to_success_content, transContentArgs, langCode);
            notification.setContent(content);
            notificationRepository.save(notification);
            log.info("收款方信息： {}", CommonUtil.toJson(notification));
            NotificationUtil.pushNotification(notification);
        }
        return Action.CommitMessage;
    }
}
