package io.mtc.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * 共通工具类
 *
 * @author Chinhin
 * 2018/6/6
 */
@Slf4j
public final class CommonUtil {

    public static boolean isEmpty(Collection<?> c) {
        return null == c || 0 == c.size();
    }

    public static boolean isEmpty(Map<?, ?> m) {
        return null == m || 0 == m.size();
    }

    public static <T> boolean isEmpty(T[] arr) {
        return null == arr || 0 == arr.length;
    }

    public static <T> boolean isEmpty(byte[] arr) {
        return null == arr || 0 == arr.length;
    }

    /**
     * 对象转json
     * @param obj 要转换的对象
     * @return
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect); // 避免循环引用
    }

    /**
     * json转对象
     * @param json  json文本
     * @param clazz 解析的类
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * json字符串转map
     * @param json
     * @return
     */
    public static Map<String, Object> jsonToMap(String json) {
        return JSON.parseObject(json);
    }

    /**
     * 对象转Map<String, Object>
     * <br/>忽略为null的属性
     * @param obj
     * @return
     */
    public static Map<String, Object> toMap(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<Object, Object> entry : new BeanMap(obj).entrySet()) {
            if ("class".equals(entry.getKey()) || null == entry.getValue()) {
                continue;
            }

            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return map;
    }

    /**
     * 从map导出对象
     * @param map
     * @param clazz
     * @return
     */
    public <T> T fromMap(Map<String, ?> map, Class<T> clazz) {
        try {
            T bean = clazz.newInstance();
            BeanUtils.populate(bean, map);
            return bean;
        } catch (Exception e) {
            log.error("map转obj失败");
            return null;
        }
    }

    /**
     * 是否需要忽略本次推送，暂停10分钟前的就不推送了
     * @param dealTimeMils 交易成交时间(毫秒)
     * @return true表示不用推送，false表示可以推送
     */
    public static boolean ignorePush(long dealTimeMils) {
        long nowTimes = System.currentTimeMillis() / 1000;
        long dealTimes = dealTimeMils / 1000;
        // 10分钟前的交易不推送
        return (nowTimes - dealTimes) > 60 * 10;
    }

    /**
     * 对数字除以10的18次方， 并保留5位小数，去掉小数点后多余的0
     * @param amount 数字
     * @return 处理好的数字
     */
    public static BigDecimal getFormatAmount(String amount) {
        return new BigDecimal(
                amount).divide(new BigDecimal(Math.pow(10, 18)),
                5, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /**
     * 对数字乘以10的18次方
     * @param amount 乘数
     */
    public static BigInteger toWei(String amount) {
        return new BigDecimal(amount).multiply(new BigDecimal(Math.pow(10, 18))).toBigInteger();
    }

    /**
     * 对于余额返回的精度进行统一为10的18次方
     * 有些代币的精度没有18次方，没有的情况补满18次方
     * @param balance 通过geth获取到的余额
     * @param decimals 代币的余额精度
     * @return 结果
     */
    public static BigInteger balanceCorrect(BigInteger balance, int decimals) {
        // 差多少个0就补多少个
        int pow = 18 - decimals;
        return balance.multiply(new BigDecimal(Math.pow(10, pow)).toBigInteger());
    }

    /**
     * 对于余额返回的精度进行统一为10的18次方
     * 返回真实的次方，主要用于提现
     * @param balance 通过geth获取到的余额
     * @param decimals 代币的余额精度
     * @return 结果
     */
    public static BigInteger balanceUnCorrect(BigInteger balance, int decimals) {
        int pow = 18 - decimals;
        return balance.divide(new BigDecimal(Math.pow(10, pow)).toBigInteger());
    }

    /**
     * 比特币聪转位的换算
     * 目的是为了托管账户的统一管理
     * @param valueOfBtc 多少个比特币
     * @return 以wei为单位的数字，比如1个比特币是10的18次方个wei
     */
    public static BigInteger btc2wei(String valueOfBtc) {
        return new BigDecimal(String.valueOf(Coin.parseCoin(valueOfBtc).getValue()))
                .multiply(new BigDecimal(String.valueOf(Math.pow(10, 10)))).toBigInteger();
    }

    /**
     * wei转为比特币
     * 将托管账户以wei为最小单位的保存方式转换为比特币的个数
     * @param valueOfWei 多少wei
     * @return 比特币
     */
    public static Coin wei2btc(BigInteger valueOfWei) {
        BigDecimal satoshi = new BigDecimal(valueOfWei).divide(new BigDecimal(String.valueOf(Math.pow(10, 10))), RoundingMode.CEILING);
        return Coin.valueOf(satoshi.longValue());
    }

}
