package io.mtc.facade.user.service;

import io.mtc.common.constants.Constants;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.sms.util.MxtSmsUtil;
import io.mtc.common.sms.util.SmsUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.entity.dividend.UserBalanceSampleAvg;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserBalanceSampleRepository;
import io.mtc.facade.user.util.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserBalanceSampleService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserBalanceSampleRepository userBalanceSampleRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private EnvUtil envUtil;


    @Value("${zcdSampleEnabled}")
    private boolean zcdSampleEnabled;

    /**
     * 定时任务,每小时插入每个ZCD用户,把满足分红数量500的托管账户中的金额到记录表中
     */
    public void refreshPrice() {

        if (zcdSampleEnabled == false) return;

        try {
//            List<UserBalance> userBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType(DividendConstant.ZCD_ADDRESS, 1);//修改 5 -> 1 (20190306)
            //  得到用户ZCD账户信息(TODO 只获取有充值记录的用户)
            List<UserBalance> userBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyTypeAndBalanceGreaterThanEqual(
                    DividendConstant.ZCD_ADDRESS, 1, CommonUtil.toWei(DividendConstant.DIVIDEND_CONDITION_NUMBER.toString()));

            List<UserBalanceSample> userBalanceSamples = new ArrayList<>();
            //判断当前时间是否在19:30-20:30之间，如果是，保存余额记录到redis
            boolean saveBalanceToRedis = isSaveBalanceToRedis();
            userBalances.forEach(userBalance -> {
                BigDecimal freezingDividendAmount = getFreezingDividendAmountByUserId(userBalance.getUserId());
                BigDecimal balance = CommonUtil.getFormatAmount(userBalance.getBalance().toString());
                BigDecimal freezingAmount = CommonUtil.getFormatAmount(userBalance.getFreezingAmount().toString());
                BigDecimal dividendAmount = balance.subtract(freezingAmount).add(freezingDividendAmount);
                UserBalanceSample userBalanceSample = new UserBalanceSample(balance, freezingAmount, freezingDividendAmount,
                        getDividendAmount(dividendAmount, userBalance.getUserId()), userBalance);
                if (saveBalanceToRedis) {
                    saveBalanceToRedis(userBalance.getUserId(), freezingDividendAmount);
                }
                userBalanceSamples.add(userBalanceSample);
            });
            logger.info("ZCD定时任务扫描 - userBalances={}", userBalances.size());
            userBalanceSampleRepository.saveAll(userBalanceSamples);
        } catch (Exception e) {
            logger.error("ZCD,每小时刷新数据定时任务出错: ", e);
            //  如果是正式环境发送邮件或短信通知
            if (envUtil.isProd()) {
                MxtSmsUtil.sendMsg(Constants.DEV_PHONE_HYP, "ZCD,每小时刷新数据定时任务出错,请及时查看");
            }
        }
    }

    //  获取当前用户的冻结可分红金额
    private BigDecimal getFreezingDividendAmountByUserId(Long userId){
        //TODO 如果有冻结但需要分红的需求下进行处理
        return BigDecimal.ZERO;
    }

    //  获取用户分红的余额计算数
    private BigDecimal getDividendAmount(BigDecimal dividendAmount, Long userId){
        BigDecimal logBalance = redisUtil.get(RedisKeys.DIVIDEND_SAMPLE_LOG(userId), BigDecimal.class);
        if (logBalance != null && logBalance.compareTo(dividendAmount) < 0){
            return logBalance;
        }
        return dividendAmount;
    }

    /**
     * 根据一个时间得到对应时间区间的分成抽样值
     * @param dateTime 时间
     * @return
     */
    public List<UserBalanceSample> getUserBalanceSampleByDate(Date dateTime) {
        Map<String, Date> dates = DividendConstant.getDateByDateTime(dateTime);
        if (dates == null) return null;
        return userBalanceSampleRepository.findByDate(dates.get("startTime"), dates.get("endTime"));
    }

    public List<UserBalanceSampleAvg> getUserBalanceSampleAvgByDate(Date dateTime) {
        Map<String, Date> dates = DividendConstant.getDateByDateTime(dateTime);
        if (dates == null) return null;
        return userBalanceSampleRepository.findAvgByDate(dates.get("startTime"), dates.get("endTime"));
    }

    private void saveBalanceToRedis(Long userId, BigDecimal balance){
        redisUtil.set(RedisKeys.DIVIDEND_SAMPLE_LOG(userId), balance, 84600);//23小时30分钟
    }

    private boolean isSaveBalanceToRedis(){
        long timestamp = System.currentTimeMillis();
        if (timestamp > getStartTime().getTime() && timestamp < getEndTime().getTime()) {
            return true;
        }
        return false;
    }

    /**
     * 今天晚上19:30
     */
    public static Date getStartTime(){
        ZoneId zoneId = ZoneId.systemDefault();
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now();
        LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
        LocalDateTime yesterdayMidnight = todayMidnight.plusHours(19).plusMinutes(30);
        ZonedDateTime zdt = yesterdayMidnight.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    /**
     * 今天晚上20:30
     */
    public static Date getEndTime(){
        ZoneId zoneId = ZoneId.systemDefault();
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now();
        LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
        LocalDateTime yesterdayMidnight = todayMidnight.plusHours(20).plusMinutes(30);
        ZonedDateTime zdt = yesterdayMidnight.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }


}
