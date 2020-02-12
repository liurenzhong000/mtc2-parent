package io.mtc.server.zuul.util;

/**
 * 请求验证
 *
 * @author Chinhin
 * 2018/7/17
 */
public class RequestCheckUtil {

    /**
     * 请求是否正确
     * @param requestTimeMills 请求毫秒
     * @param url 请求url
     * @param signature 签名
     * @return true表示通过验证
     */
    public static boolean validateRequest(long requestTimeMills, String url, String signature) {
        long now = System.currentTimeMillis();
        if (requestTimeMills > (now + 1000 * 60 * 10)) {
            return false;
        }
        // 时间差(秒)
        long diff = (now - requestTimeMills) / 1000;
        // 5分钟前的请求直接返回
        if (diff > 5 * 60) {
            return false;
        }
        String encrypt = EncryptUtil.encrypt(requestTimeMills, url);
        return encrypt.equals(signature);
    }

}
