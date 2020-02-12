package io.mtc.common.util;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

/**
 * 加密工具类
 *
 * @author Chinhin
 * 2018/6/8
 */
public final class CodecUtil {

    /**
     * 生成摘要
     * @param data 源数据
     * @param algorithm 摘要算法：MD5|SHA1|SHA256|SHA512...
     * @return 摘要数据
     */
    public static byte[] digest(byte[] data, String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * base64编码，生成编码字符串
     * @param data 要编码的数据
     * @return Base64字符串
     */
    public static String base64EncodeString(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * 生成摘要，返回base64Encode后的字符串
     * @param data 源数据
     * @param algorithm 摘要算法：MD5|SHA1|SHA256|SHA512...
     * @return 摘要数据base64Encode后的字符串
     */
    public static String digestStr(byte[] data, String algorithm) {
        return CodecUtil.base64EncodeString(digest(data, algorithm));
    }

    public static String digestStrSHA1(String string) {
        return digestStr(string.getBytes(), "SHA1");
    }

    // md5加密
    public static String getMD5Code(String message) {
        String md5Str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(message.getBytes());
            md5Str = bytes2Hex(md5Bytes);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return md5Str;
    }

    // 2进制转16进制
    public static String bytes2Hex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        int temp;
        try {
            for (int i = 0; i < bytes.length; i++) {
                temp = bytes[i];
                if(temp < 0) {
                    temp += 256;
                }
                if (temp < 16) {
                    result.append("0");
                }
                result.append(Integer.toHexString(temp));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
