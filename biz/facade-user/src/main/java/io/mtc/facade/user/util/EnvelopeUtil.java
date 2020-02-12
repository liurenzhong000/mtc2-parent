package io.mtc.facade.user.util;

import io.mtc.common.constants.Constants;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

/**
 * 红包工具类
 *
 * @author Chinhin
 * 2018/7/31
 */
public class EnvelopeUtil {


//    /**
//     * 拼手气红包
//     * @param amout 总金额
//     * @param num 红包数量
//     * @return 拆分好的红包
//     */
//    public static BigInteger[] luckyDraw(BigInteger amout, int num) {
//        // 得到总份数
//        int total = amout.divide(ENVELOPE_UNIT).intValue();
//
//        // 创建一个长度等于n的红包数组
//        int[] array = new int[num];
//        // 第一步 每个红包先塞1份
//        Arrays.fill(array,1);
//        // 总份数 减去已分配的份数
//        total -= num;
//        // 创建一个随机分配对象
//        Random random = new Random();
//        int index = random.nextInt(num);
//        int times = 0;
//        // 第二步，循环遍历如果剩余金额>0 则一直分配
//        while (total > 1){
//            int temp;
//            if (times > num) {
//                temp  = random.nextInt(total);
//            } else { // 第一轮最多拿一半
//                temp  = random.nextInt(total / 2);
//            }
//            array[index++ % num] +=  temp;
//            total -= temp;
//            times ++;
//        }
//        // 判断剩余未分配的金额是否大于0,如果大于0，可以把剩下未分配金额塞到第一个红包中
//        if (total > 0){
//            array[0] +=  total;
//        }
//        BigInteger[] result = new BigInteger[num];
//        for (int i = 0; i < num; i ++) {
//            result[i] = ENVELOPE_UNIT.multiply(BigInteger.valueOf(array[i]));
//        }
//        return result;
//    }

    /**
     * 拼手气红包
     * @param amount 总金额
     * @param num 红包数量
     * @return 拆分好的红包
     */
    public static BigInteger[] luckyDraw(BigInteger amount, int num) {
        // 得到总份数
        int total = amount.divide(Constants.CURRENCY_UNIT).intValue();
        // 平均数
        BigDecimal avg = new BigDecimal(total).divide(BigDecimal.valueOf(num), 0, RoundingMode.DOWN);
        // 计算出最小金额
        BigDecimal min = avg.multiply(BigDecimal.valueOf(0.8)).setScale(0, RoundingMode.DOWN);

        // 创建一个长度等于n的红包数组
        int[] array = new int[num];
        // 第一步 每个红包先塞最小金额
        Arrays.fill(array, min.intValue());
        // 总份数 减去已分配的份数
        total -= num * min.intValue();

        // 创建一个随机分配对象
        Random random = new Random();
        int index = random.nextInt(100);

        int times = 0;
        // 第二步，循环遍历如果剩余金额>0 则一直分配
        while (total > 1){
            int temp;
            if (times > num) {
                temp  = random.nextInt(total);
            } else { // 第一轮最多拿一半
                temp  = random.nextInt(total / 2);
            }
            array[index++ % num] +=  temp;
            total -= temp;
            times ++;
        }
        // 判断剩余未分配的金额是否大于0,如果大于0，可以把剩下未分配金额塞到第一个红包中
        if (total > 0){
            array[0] +=  total;
        }
        BigInteger[] result = new BigInteger[num];
        for (int i = 0; i < num; i ++) {
            result[i] = Constants.CURRENCY_UNIT.multiply(BigInteger.valueOf(array[i]));
        }
        return result;
    }

}
