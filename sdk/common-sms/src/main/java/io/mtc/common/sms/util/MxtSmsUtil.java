package io.mtc.common.sms.util;

import io.mtc.common.constants.MTCError;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hyp
 * @Date: 2019/3/5 10:38
 * @Description: 麦讯通短信工具类
 */
@Slf4j
public class MxtSmsUtil {
    //国内账户
    static final String ACCOUNT_CHINA = "MXT800691";
    static final String PWD_CHINA = "Mxt800691";
    //国际账户
    static final String ACCOUNT_INTERNATIONAL ="MXT800692";
    static final String PWD_INTERNATIONAL = "Mxt800692";

    static final String MSG_TAG = "【ZCD】";
    static final String BASE_URL = "http://116.62.212.142/msg/HttpSendSM";

    /**
     * 发送短信
     * @param phoneNum 以国际区号+号码的形式
     * @param code 验证码
     * @param langCode 语言编号(1:英文, 2:中文, 3:韩文)
     * @return 发送结果
     */
    public static Object simpleSend(String phoneNum, String code, int langCode) {
        //TODO 发送限制
//        return ResultUtil.errorObj(MTCError.SEND_VERIFY_OVER_LIMIT);
        String templateMsg;
        // 中文
        if (langCode == 2) {
            templateMsg = "您的验证码 ${code}，该验证码5分钟内有效。".replace("${code}", code);
        } else {
            // 英文
            templateMsg = "Your verify code ${code}, the code is valid within 5 minutes.".replace("${code}", code);
        }
        boolean sendSuccess = sendMsg(phoneNum, templateMsg);
        if(sendSuccess) {
            return null;//返回空表示发送成功
        } else {
            return ResultUtil.errorObj(MTCError.SEND_VERIFY_ERROR);
        }
    }


    public static Boolean sendMsg(String phone, String templateMsg){
        String account;
        String pwd;
        if (phone.startsWith("86") || phone.length() == 11){
            account = ACCOUNT_CHINA;
            pwd = PWD_CHINA;
        } else {
            account = ACCOUNT_INTERNATIONAL;
            pwd = PWD_INTERNATIONAL;
        }
        Map<String, String> params = new HashMap<>();
        params.put("account", account);
        params.put("pswd", pwd);
        params.put("mobile", phone);
        params.put("needstatus", "false");
        params.put("msg", templateMsg + MSG_TAG);
        String resultStr = HttpUtil.post(BASE_URL, params);
        try {
            String[] strArray = resultStr.split(",");
            if (strArray[1].equals("0")) {
                return true;
            }
        } catch (Exception e) {
            log.error("send msg error- result={}", resultStr);
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(simpleSend("18296799192", "154784", 2));
    }

}
