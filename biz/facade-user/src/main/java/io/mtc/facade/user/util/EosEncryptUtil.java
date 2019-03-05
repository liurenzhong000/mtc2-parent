package io.mtc.facade.user.util;

import io.mtc.common.util.CodecUtil;

/**
 * 调用eos传的签名
 *
 * @author Chinhin
 * 2018/7/17
 */
public class EosEncryptUtil {

    // 密钥
    private static final String secret = "mtc";

    /**
     * 请求加密
     * @return 加密后的签名字符串
     */
    public static String getSignature(String requestTimeMills, String accountName, String ownerKey, String activeKey) {
        StringBuilder stringBuilder = new StringBuilder(requestTimeMills);
        stringBuilder.append("&").append(accountName);
        stringBuilder.append("&").append(ownerKey);
        stringBuilder.append("&").append(secret);
        stringBuilder.append("&").append(activeKey);

        String linkStr = stringBuilder.toString();
        return CodecUtil.getMD5Code(String.valueOf(linkStr.hashCode()));
    }

}
