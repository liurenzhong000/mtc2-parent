package io.mtc.common.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期共同类
 *
 * @author Chinhin
 * 2018/6/6
 */
public final class DateUtil {

    /**----------------------------------------
     *                时间接口
     *-----------------------------------------*/
    public static String formatTime(Date date) {
        return DateFormatUtils.format(date, "HH:mm:ss");
    }

    public static String formatStandardDate(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(millis);
    }

    public static String formatStandardDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**----------------------------------------
     *                日期接口
     *-----------------------------------------*/
    public static String formatDateDay(Date date) {
        return DateFormatUtils.format(date, "yyyyMMdd");
    }

    public static String formatDateTime(Date date) {
        return DateFormatUtils.format(date, "yyyyMMddHHmmss");
    }

    public static String formatDateTimeMillis(Date date) {
        return DateFormatUtils.format(date, "yyyyMMddHHmmssSSS");
    }

    public static String formatDate(Date date, String format) {
        return DateFormatUtils.format(date, format);
    }

    public static String getFormatDateDay() {
        return formatDateDay(new Date());
    }

    public static String getFormatDateTime() {
        return formatDateTime(new Date());
    }

    public static String getFormatDateTimeMillis() {
        return formatDateTimeMillis(new Date());
    }

    public static String getFormatDate(String format) {
        return formatDate(new Date(), format);
    }

    public static Date parseDate(String dateStr, String format) {
        try {
            return !StringUtil.isBlank(dateStr) ? DateUtils.parseDate(dateStr, format) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取指定日期当日0点的时间
     * @param date 日期
     * @return 该日期当日0点的时间
     */
    public static Date getDateWithTimeZero(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(false);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取指定日期第n天后0点的时间
     * @param date 日期
     * @return 该日期第n天后0点的时间
     */
    public static Date getNextDayWithTimeZero(Date date, int day) {
        return getDateWithTimeZero(DateUtils.addDays(date, day));
    }

    /**
     * 获取指定日期下个月的第一天
     * @param date 指定日期
     */
    public static Date getFirstDayOfNextMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 判断一个时间是否早于另一个时间N秒
     * @param targetDate 早的时间
     * @param afterDate 晚的时间
     * @param second N（秒）
     * @return true表示是大于
     */
    public static boolean isBeforeSecond(Date targetDate, Date afterDate, int second) {
        long targetDateTime = targetDate.getTime();
        long afterDateTime = afterDate.getTime();
        return (afterDateTime - targetDateTime) > second * 1000;
    }

    /**
     * 增加秒数
     * @param time 日期
     * @param seconds 要增加的秒数
     * @return 增加后的日期
     */
    public static Date plusSeconds(Date time, Integer seconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

}
