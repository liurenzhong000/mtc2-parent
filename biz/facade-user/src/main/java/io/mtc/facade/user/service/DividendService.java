package io.mtc.facade.user.service;

import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.entity.dividend.DividendContext;
import io.mtc.facade.user.entity.dividend.DividendUser;
import io.mtc.facade.user.entity.dividend.UserBalanceSampleAvg;
import io.mtc.facade.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private DividendLogRepository dividendLogRepository;

    @Autowired
    private DividendDetailRepository dividendDetailRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private DividendContext dividendContext;

    @Value("${zcdInvestmentDividendEnabled}")
    private boolean zcdInvestmentDividendEnabled;


    public void executeJob() {

        if (zcdInvestmentDividendEnabled == false) return;

        //  得到所有用户的信息
        dividendContext.setUsers(userRepository.findAll());
        //  设置用户账户的repository
        dividendContext.setUserBalanceRepository(userBalanceRepository);
        dividendContext.setUserRepository(userRepository);
        dividendContext.setDividendLogRepository(dividendLogRepository);
        dividendContext.setDividendDetailRepository(dividendDetailRepository);
        dividendContext.setBillRepository(billRepository);
        //  得到用户ZCD账户信息
        //  TODO 减少数据量-（改成从快照里取/自取能获得分红的数据）
        List<UserBalance> serZCDBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType(DividendConstant.ZCD_ADDRESS, 1);
        if (serZCDBalances == null || serZCDBalances.size() == 0) return;
        dividendContext.setUserZCDBalances(serZCDBalances);

        //  得到ZCD用户每小时时段的抽样信息
//        List<UserBalanceSample> userBalanceSamples = userBalanceSampleService.getUserBalanceSampleByDate(new Date());
        List<UserBalanceSampleAvg> userBalanceSampleAvgs = userBalanceSampleService.getUserBalanceSampleAvgByDate(new Date());
        userBalanceSampleAvgs = userBalanceSampleAvgs.stream().filter(item->item.getCounts()>=DividendConstant.SAMPLE_COUNTS).collect(Collectors.toList());
//        dividendContext.setUserBalanceSamples(userBalanceSamples);
        dividendContext.setUserBalanceSampleAvgs(userBalanceSampleAvgs);
        //  设置已存在的用户USDT账户
//        List<UserBalance> userUSDTBalances = userBalanceRepository.findByCurrencyAddressAndCurrencyType("USDT", 4);
//        dividendContext.setUserUSDTBalances(userUSDTBalances);
        //  设置所有参与分成的用户,并生成分成树形结构
        for (UserBalance item : serZCDBalances) {
            User user = dividendContext.findUserById(item.getUserId());
            if (user != null) {
                dividendContext.getDividendUsers().add(new DividendUser(user, 0, dividendContext).buildTree());
            }
        }

        //  计算分成
        dividendContext.dividendPrice();
    }

}
