package io.mtc.facade.user.constants;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DividendConstant {

    //  ZCD合约地址
    public static final String ZCD_ADDRESS = "0x1284cb7d83195a95f2e59b977547d5293b89cde6";

    //  ZCD最大分红基数
    public static final BigDecimal DIVIDEND_MAX_NUMBER = new BigDecimal("10000");

    //  分成条件(预设持币500参与分成)
    public static final Integer DIVIDEND_CONDITION_NUMBER = 500;

    public static final Integer SAMPLE_COUNTS = 1;

    //  USDT恒定价格(RMB)
    public static final BigDecimal USDT_PRICE = BigDecimal.valueOf(6.93);

    //  ZCD价格(RMB)
    public static final BigDecimal ZCD_PRICE = BigDecimal.valueOf(10.00);

    public static final BigDecimal ZCD_USDT = new BigDecimal("0.8");

    //  静态分红比例
    public static final BigDecimal STATIC_DIVIDEND_RATE = BigDecimal.valueOf(0.01);//TODO 0.5%-2%

    //  动态一级分红比例(一级获得分红的20%)
    public static final BigDecimal DYNAMIC_DIVIDEND_LEVEL_1_RATE = BigDecimal.valueOf(0.2);

    //  动态二级分红比例(二级获得分红的30%)
    public static final BigDecimal DYNAMIC_DIVIDEND_LEVEL_2_RATE = BigDecimal.valueOf(0.3);

    //  动态三级分红比例
    public static final BigDecimal DYNAMIC_DIVIDEND_LEVEL_3_RATE = BigDecimal.valueOf(0.003);

    /**
     * 根据一个时间,得到该时间用于分成计算的有效区间(前一天的20点到当天的19.59.59)
     *
     * @param datetime 时间
     * @return
     */
    public static Map<String, Date> getDateByDateTime(Date datetime) {
        ZoneId zone = ZoneId.systemDefault();
        LocalTime time = LocalDateTime.ofInstant(datetime.toInstant(), zone).toLocalTime();
        LocalDate date = LocalDateTime.ofInstant(datetime.toInstant(), zone).toLocalDate();
        LocalDate startDate = date.plusDays(-2);
        date = date.plusDays(-1);
        if (time.getHour() > 20) {
            startDate = startDate.plusDays(1);
            date = date.plusDays(1);
        }

        String startTime = startDate.getYear() + "-" + startDate.getMonthValue() + "-" + startDate.getDayOfMonth() + " 19:30:00";
        String endTime = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth() + " 20:30:00";
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Date> times = new HashMap<>();
        try {
            times.put("startTime", sim.parse(startTime));
            times.put("endTime", sim.parse(endTime));
            return times;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(getDateByDateTime(new Date())));
    }

    /**
     * 得到对应分成的比率
     *
     * @return
     */
    public static BigDecimal getRate(Integer level) {
        switch (level) {
            case 0:
                return DividendConstant.STATIC_DIVIDEND_RATE;
            case 1:
                return DividendConstant.DYNAMIC_DIVIDEND_LEVEL_1_RATE;
            case 2:
                return DividendConstant.DYNAMIC_DIVIDEND_LEVEL_2_RATE;
            case 3:
                return DividendConstant.DYNAMIC_DIVIDEND_LEVEL_3_RATE;
            default:
                throw new RuntimeException("错误的用户等级" + level);
        }
    }

}
