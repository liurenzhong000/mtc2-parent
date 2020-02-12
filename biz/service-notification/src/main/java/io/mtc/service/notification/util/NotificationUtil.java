package io.mtc.service.notification.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.push.model.v20160801.PushRequest;
import com.aliyuncs.push.model.v20160801.PushResponse;
import com.aliyuncs.utils.ParameterHelper;
import io.mtc.service.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 推送工具类
 *
 * @author Chinhin
 * 2018/7/11
 */
@Slf4j
public class NotificationUtil {

    private static final String accessKeyId = "LTAI6Nz6H3G1BgkD";
    private static final String accessKeySecret = "N3OAqHxZb1MGMclZaGLjrCneb4zHtT";

    private static final long androidAppKey = 24963209;
    private static final long iosAppKey = 24962428;
    // 国外
    private static final long iosAppKey2 = 25058378;

    public synchronized static void pushNotification(Notification notification) {

        log.info("推送信息给 {}", notification.getAddress());

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        PushRequest pushRequest = makeCommonRequest(notification);

        new Thread(() -> {
            try {
                push2android(client, pushRequest);
            } catch (ClientException e) {
                log.error("Push Android error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                push2ios(iosAppKey, client, "DEV", pushRequest);
            } catch (ClientException e) {
                log.error("Push IOS DEV error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                push2ios(iosAppKey2, client, "DEV", pushRequest);
            } catch (ClientException e) {
                log.error("Push IOS DEV error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                push2ios(iosAppKey, client, "PRODUCT", pushRequest);
            } catch (ClientException e) {
                log.error("Push IOS Product error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                push2ios(iosAppKey2, client, "PRODUCT", pushRequest);
            } catch (ClientException e) {
                log.error("Push IOS Product error {}", e.getMessage());
            }
        }).start();
    }

    /**
     * 群推tag消息
     * @param notification 通知
     * @param tag tag
     */
    public synchronized static void pushTagNotification(Notification notification, String tag) {

        log.info("推送Tag信息给 {}", tag);

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        PushRequest pushRequest = makeTagCommonRequest(notification, tag);

        new Thread(() -> {
            try {
                push2android(client, pushRequest);
            } catch (ClientException e) {
                log.error("Push Tag Android error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(250);
                push2ios(iosAppKey, client, "DEV", pushRequest);
            } catch (Exception e) {
                log.error("Push Tag IOS DEV error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(250);
                push2ios(iosAppKey2, client, "DEV", pushRequest);
            } catch (Exception e) {
                log.error("Push Tag IOS DEV error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                push2ios(iosAppKey, client, "PRODUCT", pushRequest);
            } catch (Exception e) {
                log.error("Push IOS Product error {}", e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                push2ios(iosAppKey2, client, "PRODUCT", pushRequest);
            } catch (Exception e) {
                log.error("Push IOS Product error {}", e.getMessage());
            }
        }).start();
    }

    private static void push2android(DefaultAcsClient client, PushRequest pushRequest) throws ClientException {
        // 推送目标
        pushRequest.setAppKey(androidAppKey);
        pushRequest.setDeviceType("ANDROID"); // 设备类型 ANDROID iOS ALL.
        // 推送配置: Android
        pushRequest.setAndroidNotifyType("SOUND");//通知的提醒方式 "VIBRATE" : 震动 "SOUND" : 声音 "BOTH" : 声音和震动 NONE : 静音
//        pushRequest.setAndroidNotificationBarType(1);//通知栏自定义样式0-100
//        pushRequest.setAndroidNotificationBarPriority(1);//通知栏自定义样式0-100
        pushRequest.setAndroidOpenType("ACTIVITY"); //点击通知后动作 "APPLICATION" : 打开应用 "ACTIVITY" : 打开AndroidActivity "URL" : 打开URL "NONE" : 无跳转
//        pushRequest.setAndroidOpenUrl("http://www.aliyun.com"); //Android收到推送后打开对应的url,仅当AndroidOpenType="URL"有效
//        pushRequest.setAndroidActivity("com.alibaba.push2.demo.XiaoMiPushActivity"); // 设定通知打开的activity，仅当AndroidOpenType="Activity"有效
        pushRequest.setAndroidMusic("default"); // Android通知音乐
//        pushRequest.setAndroidPopupActivity("com.ali.demo.PopupActivity");//设置该参数后启动辅助弹窗功能, 此处指定通知点击后跳转的Activity（辅助弹窗的前提条件：1. 集成第三方辅助通道；2. StoreOffline参数设为true）
//        pushRequest.setAndroidPopupTitle("Popup Title");
//        pushRequest.setAndroidPopupBody("Popup Body");
        PushResponse pushResponse = client.getAcsResponse(pushRequest);
        log.info("Push Android RequestId:{}, MessageID:{}", pushResponse.getRequestId(), pushResponse.getMessageId());
    }


    private static void push2ios(long key, DefaultAcsClient client, String env, PushRequest pushRequest) throws ClientException {

        // 推送目标
        pushRequest.setAppKey(key);
        pushRequest.setDeviceType("iOS"); // 设备类型 ANDROID iOS ALL.
        // 推送配置: iOS
//        pushRequest.setIOSBadge(5); // iOS应用图标右上角角标
        pushRequest.setIOSMusic("default"); // iOS通知声音
//        pushRequest.setIOSSubtitle("iOS10 subtitle");//iOS10通知副标题的内容
//        pushRequest.setIOSNotificationCategory("iOS10 Notification Category");//指定iOS10通知Category
        pushRequest.setIOSMutableContent(true);//是否允许扩展iOS通知内容
        pushRequest.setIOSApnsEnv(env);//iOS的通知是通过APNs中心来发送的，需要填写对应的环境信息。"DEV" : 表示开发环境 "PRODUCT" : 表示生产环境
//        pushRequest.setIOSRemind(true); // 消息推送时设备不在线（既与移动推送的服务端的长连接通道不通），则这条推送会做为通知，通过苹果的APNs通道送达一次。注意：离线消息转通知仅适用于生产环境
//        pushRequest.setIOSRemindBody("iOSRemindBody");//iOS消息转通知时使用的iOS通知内容，仅当iOSApnsEnv=PRODUCT && iOSRemind为true时有效

        PushResponse pushResponse = client.getAcsResponse(pushRequest);
        log.info("Push IOS RequestId:{}, MessageID:{}", pushResponse.getRequestId(), pushResponse.getMessageId());
    }

    private static PushRequest makeBaseRequest(Notification notification) {
        PushRequest pushRequest = new PushRequest();
        pushRequest.setPushType("NOTICE"); // 消息类型 MESSAGE NOTICE
        // 推送配置
        pushRequest.setTitle(notification.getTitle()); // 消息的标题
        pushRequest.setBody(notification.getContent()); // 消息的内容

        String paramsStr = "{" +
                "'type':'" + notification.getType() +
                "','target':'" + notification.getAddress() +
                "','txHash':'" + notification.getTxHash() +
                "','url':'" + notification.getUrl() +
                "'}";

        pushRequest.setIOSExtParameters(paramsStr); //通知的扩展属性(注意 : 该参数要以json map的格式传入,否则会解析出错)
        pushRequest.setAndroidExtParameters(paramsStr); //设定通知的扩展属性。(注意 : 该参数要以 json map 的格式传入,否则会解析出错)

        // 推送控制
        String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + 12 * 3600 * 1000)); // 12小时后消息失效, 不会再发送
        pushRequest.setExpireTime(expireTime);
        pushRequest.setStoreOffline(true); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
        return pushRequest;
    }

    private static PushRequest makeCommonRequest(Notification notification) {
        PushRequest pushRequest = makeBaseRequest(notification);
        pushRequest.setTarget("ALIAS"); //推送目标: DEVICE:按设备推送 ALIAS : 按别名推送 ACCOUNT:按帐号推送  TAG:按标签推送; ALL: 广播推送
        pushRequest.setTargetValue(notification.getAddress()); //根据Target来设定，如Target=DEVICE, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
        return pushRequest;
    }

    private static PushRequest makeTagCommonRequest(Notification notification, String tag) {
        PushRequest pushRequest = makeBaseRequest(notification);
        pushRequest.setTarget("TAG"); //推送目标: DEVICE:按设备推送 ALIAS : 按别名推送 ACCOUNT:按帐号推送  TAG:按标签推送; ALL: 广播推送
        pushRequest.setTargetValue(tag); //根据Target来设定，如Target=DEVICE, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
        return pushRequest;
    }

}
