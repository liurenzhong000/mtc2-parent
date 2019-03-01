package io.mtc.common.util;

import io.mtc.common.constants.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author Chinhin
 * 2018/6/6
 */
public final class StringUtil extends StringUtils {

    /** random */
    private static final Random random = new Random();

    /**
     * 随机数字
     * @param length 字符串长度
     * @return 生成的随机数字
     */
    public static String randomNumber(int length) {
        char[] numbers = "0123456789".toCharArray();
        if (length < 1) {
            return null;
        }
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbers[random.nextInt(10)];
        }
        return new String(randBuffer);
    }

    /**
     * 生成指定长度的随机字符串
     */
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        char[] values = new char[length];
        for (int i = 0; i < length; i++) {
            values[i] = base.charAt(random.nextInt(base.length()));
        }
        return new String(values);
    }

    /**
     * 将字符串按照一定长度分割
     * @param inputString 字符串
     * @param length 需要的长度
     * @return 切割好的数组
     */
    public static List<String> stringSpilt(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length 指定长度
     * @param size 指定列表大小
     * @return 结果
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 验证是否正常邮箱地址
     *
     * @param email 邮箱地址
     * @return true表示正确
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception ignored) {

        }
        return flag;
    }

    /**
     * 对字符串加密
     * @param content 字符串
     * @return 结果
     */
    public static String snow(String content) {
        String userNameAfterReplaced;
        if (content == null) {
            return Constants.EMPTY;
        }
        int nameLength = content.length();
        if(nameLength<3 && nameLength>0){
            if(nameLength==1){
                userNameAfterReplaced = "*";
            }else{
                userNameAfterReplaced = content.replaceAll(content, "^.{1,2}");
            }
        }else{
            Integer num1,num2,num3;
            num2=(new Double(Math.ceil((double) nameLength /3))).intValue();
            num1=(new Double(Math.floor((double) nameLength /3))).intValue();
            num3=nameLength-num1-num2;
            String star= StringUtils.repeat("*",num2);
            userNameAfterReplaced = content.replaceAll("(.{"+num1+"})(.{"+num2+"})(.{"+num3+"})","$1"+star+"$3");
        }
        return userNameAfterReplaced;
    }

    /**
     * set转str，会去掉两边的括号
     * @param temp set
     * @param maxLength 最多多少位，后面会加...
     * @return 结果
     */
    public static String set2str(Set<String> temp, int maxLength) {
        StringBuilder sb = new StringBuilder(temp.toString());
        boolean over = (sb.length() - 1) > maxLength;
        if (over) {
            sb.delete(maxLength - 2, sb.length());
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.deleteCharAt(0);
        if (over) {
            sb.append("...");
        }
        return sb.toString();
    }

}
