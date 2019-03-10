package io.mtc.facade.user.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import io.mtc.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云邮件推送
 *
 * @author Chinhin
 * 2018/7/25
 */
@Slf4j
public class AliyunMailUtil {

    private static final String regionId = "cn-hangzhou";
    private static final String accessKeyId = "LTAIQDLWQ1yGUcrS";
    private static final String accessKeySecret = "mqUFcmwTpuhchzrsiUlpHK1dXu2SBY";

    // 控制台创建的发信地址
    private static final String accountName = "zcdcoin@email.zcdcoin.club";
    // 发信人昵称
    private static final String fromAlias = "ZCD";
    // 控制台创建的标签
    private static final String tagName = "zcdcoinWallet";

    public static void sendMail(String target, String subject, String content) {
        // 如果是除杭州region外的其它region（如新加坡、澳洲Region），需要将下面的"cn-hangzhou"替换为"ap-southeast-1"、或"ap-southeast-2"。
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        // 如果是除杭州region外的其它region（如新加坡region）， 需要做如下处理
        try {
            DefaultProfile.addEndpoint("dm.ap-southeast-1.aliyuncs.com", "ap-southeast-1", "Dm",  "dm.ap-southeast-1.aliyuncs.com");
        } catch (ClientException e) {
            e.printStackTrace();
        }
        IAcsClient client = new DefaultAcsClient(profile);
        SingleSendMailRequest request = new SingleSendMailRequest();
        try {
            //request.setVersion("2017-06-22");// 如果是除杭州region外的其它region（如新加坡region）,必须指定为2017-06-22
            request.setAccountName(accountName);
            request.setFromAlias(fromAlias);
            request.setAddressType(1);
            request.setTagName(tagName);
            request.setReplyToAddress(true);
            request.setToAddress(target);
            request.setSubject(subject);
            request.setHtmlBody(getHtml(content));
            SingleSendMailResponse httpResponse = client.getAcsResponse(request);
            log.info(CommonUtil.toJson(httpResponse));
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private static String getHtml(String content){
        StringBuilder stringBuilder = new StringBuilder("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">");
        stringBuilder.append("<tbody><tr><td>");
        stringBuilder.append("<table width=\"560\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        stringBuilder.append("<tbody><tr><td style=\"-webkit-border-radius:8px;background-color:#ffffff\" bgcolor=\"#FFFFFF\">");
        stringBuilder.append("<table width=\"520\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        stringBuilder.append("<tbody><tr valign=\"top\"><td width=\"520\" bgcolor=\"#FFFFFF\" align=\"left\">");
        stringBuilder.append("<div style=\"margin:40px 0px 20px 15px;color:#000000;font-size:18px;line-height:1.3em;\"><strong>");
        stringBuilder.append(content);
        stringBuilder.append("</strong></div><div style=\"margin:0px 0px 20px 15px;border-top: 1px solid #eeeeee; font-size:12px;line-height:1.3em;\"></div>");
        stringBuilder.append("<div style=\"margin:0px 10px 20px 15px;color:#333333;font-size:12px;line-height:1.3em;\">");
        stringBuilder.append("© 2019 ZCD-WALLET");
        stringBuilder.append("</div><br /></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table>");
        return stringBuilder.toString();
    }

}
