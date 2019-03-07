package io.mtc.facade.user.service;

import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.entity.dividend.DividendContext;
import io.mtc.facade.user.entity.dividend.DividendUser;
import io.mtc.facade.user.repository.UserBalanceRepository;
import io.mtc.facade.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Service
public class DividendService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceSampleService userBalanceSampleService;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private DividendContext dividendContext;

    @Value("${bhbInvestmentDividendEnabled}")
    private boolean bhbInvestmentDividendEnabled;


    public void executeJob() {

        if (bhbInvestmentDividendEnabled == false) return;

        //  得到所有用户的信息
        dividendContext.setUsers(userRepository.findAll());
        //  设置用户账户的repository
        dividendContext.setUserBalanceRepository(userBalanceRepository);
        //  得到用户BHB账户信息
        List<UserBalance> serBHBBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType(DividendConstant.BHB_ADDRESS, 1);
        if (serBHBBalances == null || serBHBBalances.size() == 0) return;
        dividendContext.setUserBHBBalances(serBHBBalances);
        //  设置已存在的用户USDT账户
        List<UserBalance> userUSDTBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType("USDT", 4);
        dividendContext.setUserUSDTBalances(userUSDTBalances);
        //  设置所有参与分成的用户,并生成分成树形结构
        for (UserBalance item : serBHBBalances) {
            User user = dividendContext.findUserById(item.getUserId());
            if (user != null) {
                dividendContext.getDividendUsers().add(new DividendUser(user, 0, dividendContext).buildTree());
            }
        }
        //  得到BHB用户每小时时段的抽样信息
        List<UserBalanceSample> userBalanceSamples = userBalanceSampleService.getUserBalanceSampleByDate(new Date());
        dividendContext.setUserBalanceSamples(userBalanceSamples);
        //  计算分成
        dividendContext.dividendPrice();
    }

}
