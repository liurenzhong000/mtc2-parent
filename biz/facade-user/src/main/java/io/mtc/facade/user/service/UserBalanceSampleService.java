package io.mtc.facade.user.service;

import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserBalanceSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class UserBalanceSampleService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserBalanceSampleRepository userBalanceSampleRepository;

    @Value("${bhbInvestmentDividendEnabled}")
    private boolean bhbInvestmentDividendEnabled;

    /**
     * 定时任务,每小时插入每个BHB用户,托管账户中的金额到记录表中
     */
    public void refreshPrice() {

        if (bhbInvestmentDividendEnabled == false) return;

        try {
            List<UserBalance> userBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType(DividendConstant.BHB_ADDRESS, 5);
            List<UserBalanceSample> userBalanceSamples = new ArrayList<>();
            userBalances.forEach(item -> {
                UserBalanceSample userBalanceSample = new UserBalanceSample(item.getBalance(), item.getFreezingAmount(), item);
                userBalanceSamples.add(userBalanceSample);
            });
            userBalanceSampleRepository.saveAll(userBalanceSamples);
        } catch (Exception e) {
            logger.error("BHB,每小时刷新数据定时任务出错: ", e);
        }
    }

    /**
     * 根据一个时间得到对应时间区间的分成抽样值
     *
     * @param dateTime 时间
     * @return
     */
    public List<UserBalanceSample> getUserBalanceSampleByDate(Date dateTime) {
        Map<String, Date> dates = DividendConstant.getDateByDateTime(dateTime);
        if (dates == null) return null;
        return userBalanceSampleRepository.findByDate(dates.get("startTime"), dates.get("endTime"));
    }


}
