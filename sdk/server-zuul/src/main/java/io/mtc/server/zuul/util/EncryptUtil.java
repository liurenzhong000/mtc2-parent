package io.mtc.server.zuul.util;

import io.mtc.common.util.CodecUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 加密工具类
 *
 * @author Chinhin
 * 2018/7/17
 */
public class EncryptUtil {

    /**
     * 请求加密
     * @param requestTimeMills 请求时间毫秒数
     * @param url 请求url
     * @return 加密后的签名字符串
     */
    public static String encrypt(long requestTimeMills, String url) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ssdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String encryptDate = simpleDateFormat.format(new Date(requestTimeMills));

        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("&").append(requestTimeMills);
        stringBuilder.append("&").append(encryptDate);

        String linkStr = stringBuilder.toString();
        return CodecUtil.getMD5Code(String.valueOf(linkStr.hashCode()));
    }

    public static String makeKey(Long requestTimeMills, String signature) {
        String temp = requestTimeMills - 2 + signature;
        String md5Code = CodecUtil.getMD5Code(String.valueOf(temp.hashCode()));
        return md5Code.substring(0, 16);
    }

}
