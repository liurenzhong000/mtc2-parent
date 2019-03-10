package io.mtc.common.sms.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import io.mtc.common.constants.MTCError;
import io.mtc.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里短信工具类
 *
 * @author Chinhin
 * 2018/7/24
 */
@Slf4j
public class SmsUtil {

    private static final String regionId = "ap-southeast-1";
    private static final String accessKeyId = "LTAIQDLWQ1yGUcrS";
    private static final String accessKeySecret = "mqUFcmwTpuhchzrsiUlpHK1dXu2SBY";
    private static final String signName = "ZCD-WALLET";

    public static void main(String[] args) {
        simpleSend("8618296799192", "666666", 1);
    }

    /**
     * 发送短信
     * @param phoneNum 以国际区号+号码的形式
     * @param code 验证码
     * @param langCode 语言编号(1:英文, 2:中文, 3:韩文)
     * @return 发送结果
     */
    public static Object simpleSend(String phoneNum, String code, int langCode) {
        String templateCode;
        // 国内
        if (phoneNum.startsWith("86")) {
            // 中文
            if (langCode == 2) {
                templateCode = "SMS_158943753";
                // 英文
            } else {
                templateCode = "SMS_158943757";
            }
        } else {
            // 中文
            if (langCode == 2) {
                templateCode = "SMS_158943755";
                // 英文
            } else {
                templateCode = "SMS_158948455";
            }
        }
        try {
            return send("00" + phoneNum, "{\"code\":\"" + code + "\"}", templateCode);
        } catch (ClientException e) {
            e.printStackTrace();
            return ResultUtil.errorObj(MTCError.SEND_VERIFY_ERROR);
        }
    }

    /**
     * 发送短信
     * @param phoneNums 支持以逗号分隔的形式进行批量调用。
     *                 发送国际/港澳台消息时，接收号码格式为00+国际区号+号码，如“0085200000000”。
     *                 国内用：1500000000
     * @param params "{\"name\":\"Tom\", \"code\":\"123\"}"
     * @param templateCode "SMS_1000000"
     * @throws ClientException 失败异常
     */
    public static Object send(String phoneNums, String params, String templateCode) throws ClientException {
        //设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        //初始化ascClient需要的几个参数
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改
        //初始化ascClient,暂时不支持多region（请勿修改）
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId,
                accessKeySecret);
        DefaultProfile.addEndpoint(regionId, regionId, product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        //组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        //使用post提交
        request.setMethod(MethodType.POST);
        //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为00+国际区号+号码，如“0085200000000”
        request.setPhoneNumbers(phoneNums);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        request.setTemplateParam(params);
        //可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");
        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
//        request.setOutId("yourOutId");
        //请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        //请求成功
        if(sendSmsResponse != null && sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
            return null;
        } else {
            if (sendSmsResponse != null) {
                log.error(sendSmsResponse.getMessage());
                // 触发流量控制
                if ("isv.BUSINESS_LIMIT_CONTROL".equals(sendSmsResponse.getCode())) {
                    return ResultUtil.errorObj(MTCError.SEND_VERIFY_OVER_LIMIT);
                }
            }
            return ResultUtil.errorObj(MTCError.SEND_VERIFY_ERROR);
        }
    }

}