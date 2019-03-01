package io.mtc.common.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 */
public class AesCBC {

    private AesCBC(){}
    private static class SingletonHolder {
        private static AesCBC instance = new AesCBC();
    }
    public static AesCBC getInstance() {
        return SingletonHolder.instance;
    }

    public static String makeKey(String signature) {
        String md5Code = CodecUtil.getMD5Code(String.valueOf(signature.hashCode()));
        return md5Code.substring(0, 16);
    }

    public String simpleEncrypt(String content, String key) {
        String result = null;
        try {
            result = encrypt(content, "utf-8", key, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String simpleDecrypt(String content, String key) {
        return decrypt(content, "utf-8", key, key);
    }

    /**
     * 加密
     * @param sSrc 原文
     * @param encodingFormat 编码
     * @param sKey 密码
     * @param ivParameter 偏移量
     * @return 密文
     * @throws Exception 异常
     */
    public String encrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(encodingFormat));
        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码。
    }

    /**
     * 解密
     * @param sSrc 密文
     * @param encodingFormat 编码
     * @param sKey 密码
     * @param ivParameter 偏移量
     * @return 原文
     */
    public String decrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) {
        try {
            byte[] raw = sKey.getBytes(StandardCharsets.US_ASCII);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, encodingFormat);
        } catch (Exception ex) {
            return null;
        }
    }

}