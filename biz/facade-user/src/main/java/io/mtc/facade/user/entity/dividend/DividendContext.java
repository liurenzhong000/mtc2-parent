package io.mtc.facade.user.entity.dividend;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.constants.BillStatus;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.repository.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class DividendContext {

    @Autowired
    private RateCacheUtil rateCacheUtil;

    //  保存所有用户对象（用户量不多的情况下可行）
    private Iterable<User> users;

    //  所有参与分成的用户集合 (给含有ZCD的用户构建上下级关系)
    private List<DividendUser> dividendUsers = new ArrayList<>();

    //  用户ZCD账户集合
    private List<UserBalance> userZCDBalances;

    //  用户usdt账户集合(这个实时性差，可能并发导致失败)
//    private List<UserBalance> userUSDTBalances;

    //  用户账户抽样数据集合
//    private List<UserBalanceSample> userBalanceSamples;

    //  用户账户抽样数据合格集合
    private List<UserBalanceSampleAvg> userBalanceSampleAvgs;

    private UserBalanceRepository userBalanceRepository;

    private UserRepository userRepository;

    private DividendLogRepository dividendLogRepository;

    private DividendDetailRepository dividendDetailRepository;

    private BillRepository billRepository;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 得到USD->RMB比率
     *
     * @return
     */
    public BigDecimal getUSD2CNYRate() {
        return rateCacheUtil.getUSD2CNY();
    }

    /**
     * 得到USDT价格(USD)
     *
     * @return
     */
    public BigDecimal getUSDTCNYPrice() {
        return DividendConstant.USDT_PRICE;
    }

    /**
     * 得到ZCD价格(RMB)
     *
     * @return
     */
//    public BigDecimal getZCDCNYPrice() {
//        return DividendConstant.ZCD_PRICE;
//    }

    public BigDecimal getZCDUSDT(){
        return DividendConstant.ZCD_USDT;
    }

    public User findUserById(Long id) {
        for (User item : users) {
            if (item.getId().longValue() == id) return item;
        }
        return null;
    }

    /**
     * 根据手机号,email,得到当前用户下一级用户(推荐人)
     * 1    判断参数是否为空,如果为空则直接返回null
     * 2    从所有用户中筛选出匹配的用户,并返回
     *
     * @param email 用户email
     * @param phone 用户手机号码
     * @return
     */
    public List<User> findPromoterByEmailOrPhone(String email, String phone) {
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) return null;
        List<User> findUser = new ArrayList<>();
        for (User item : this.users) {
            if (StringUtils.isNotBlank(item.getPromoter()) &&
                    (item.getPromoter().equalsIgnoreCase(email) || item.getPromoter().equalsIgnoreCase(phone))) {
                findUser.add(item);
            }
        }
        return findUser;
    }

    /**
     * 根据用户id,得到用户的ZCD账户
     *
     * @param userId 用户id
     * @return
     */
    public UserBalance getZCDBalanceByUserId(Long userId) {
        if (userZCDBalances == null) return null;
        for (UserBalance item : userZCDBalances) {
            if (item.getUserId().compareTo(userId) == 0) return item;
        }
        return null;
    }

    /**
     * 根据用户id,得到用户的USDT账户
     *
     * @param userId 用户id
     * @return
     */
    public UserBalance getUSDTBalanceByUserId(Long userId) {
        User user = userRepository.findById(userId).get();
        return userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(user, "USDT", 5);
//        if (userUSDTBalances == null) return null;
//        for (UserBalance item : userUSDTBalances) {
//            if (item.getUserId().compareTo(userId) == 0) return item;
//        }
//        return null;
    }

    /**
     * 根据用户id,得到用户采样信息
     *
     * @param userId 用户id
     * @return
     */
//    public List<UserBalanceSample> getBalanceSampleByUserId(Long userId) {
//        List<UserBalanceSample> findData = new ArrayList<>();
//        for (UserBalanceSample item : userBalanceSamples) {
//            if (item.getUserId().compareTo(userId) == 0) findData.add(item);
//        }
//        return findData;
//    }

    public UserBalanceSampleAvg getBalanceSampleAvgByUserId(Long userId) {
        for (UserBalanceSampleAvg item : userBalanceSampleAvgs) {
            if (item.getUserId().compareTo(userId) == 0)
                return item;
        }
        return null;
    }

    /**
     * bhb分成逻辑入口函数
     * 1    遍历ZCD分成用户
     * 2    得到用户分成金额,如果为0,则跳过
     * 3    计算当前循环用户的usdt分成数量
     * 4    写入数据库(如果没有USDT账户则直接创建)
     */
    public void dividendPrice() {
        for (DividendUser dividendUser : this.dividendUsers) {
            List<DividendData> dividendDataList = Lists.newArrayList();
            dividendUser.getPrice(dividendDataList);
            BigDecimal price = dividendDataList.stream().map(item -> item.getPrice()).reduce(BigDecimal::add).get();
            if (price.compareTo(BigDecimal.ZERO) == 0) continue;
            BigInteger dividendUSDT = CommonUtil.toWei(price.stripTrailingZeros().toString());
            UserBalance userUSDTBalance = this.getUSDTBalanceByUserId(dividendUser.getUser().getId());
            filterToSaveLog(dividendUser, dividendDataList, userUSDTBalance, price);
            logger.info("用户: " + dividendUser.getUser().getId() + "计算分成金额为: " + price.doubleValue() + "分到usdt--->" + CommonUtil.getFormatAmount(dividendUSDT.toString()));
            if (userUSDTBalance == null) {
                userUSDTBalance = new UserBalance();
                userUSDTBalance.setBalance(dividendUSDT);
                userUSDTBalance.setCurrencyAddress("USDT");
                userUSDTBalance.setCurrencyType(5);
                userUSDTBalance.setUser(dividendUser.getUser());
                userUSDTBalance.setWalletAddress("Deprecated");
            } else {
                userUSDTBalance.setBalance(userUSDTBalance.getBalance().add(dividendUSDT));
                userUSDTBalance.setUser(dividendUser.getUser());
            }
            //  更新前要查询数据库......
            userBalanceRepository.save(userUSDTBalance);
        }
    }

     /**
     * 保存分红日志和分红记录
     * @param dividendUser
     * @param dividendDataList
     * @param userUSDTBalance
     */
    private void filterToSaveLog(DividendUser dividendUser, List<DividendData> dividendDataList, UserBalance userUSDTBalance, BigDecimal allDividend){
        //保存总记录
        Long userId = dividendUser.getUser().getId();
        DividendData myDividendData = DividendData.listByLevel(dividendDataList, 0).get(0);
        List<DividendData> oneDividendDataList = DividendData.listByLevel(dividendDataList, 1);
        List<DividendData> twoDividendDataList = DividendData.listByLevel(dividendDataList, 2);
        BigDecimal oneDividend = DividendData.countPriceFromList(oneDividendDataList);
        BigDecimal twoDividend = DividendData.countPriceFromList(twoDividendDataList);
        String oneUserIds = DividendData.joinUserIdsFromList(oneDividendDataList);
        String twoUserIds = DividendData.joinUserIdsFromList(twoDividendDataList);

        DividendLog dividendLog = new DividendLog();
        dividendLog.setUserId(userId);
        dividendLog.setCurrBalance(myDividendData.getBalance());
        dividendLog.setAllDividend(allDividend);
        dividendLog.setMyDividend(myDividendData.getPrice());
        dividendLog.setOneDividend(oneDividend);
        dividendLog.setOneCount(oneDividendDataList.size());
        dividendLog.setOneUserIds(oneUserIds);
        dividendLog.setTwoDividend(twoDividend);
        dividendLog.setTwoCount(twoDividendDataList.size());
        dividendLog.setTwoUserIds(twoUserIds);
        dividendLogRepository.save(dividendLog);

        //保存分红来源的详细记录
        List<DividendDetail> dividendDetails = Lists.newArrayList();
        String remark = new StringBuilder()
                .append("静态分红比例：").append(DividendConstant.STATIC_DIVIDEND_RATE)
                .append("一级分红比例：").append(DividendConstant.DYNAMIC_DIVIDEND_LEVEL_1_RATE)
                .append("二级分红比例：").append(DividendConstant.DYNAMIC_DIVIDEND_LEVEL_2_RATE).toString();
        dividendDataList.forEach(item ->{
            DividendDetail dividendDetail = new DividendDetail();
            dividendDetail.setFromUserId(item.getUserId());
            dividendDetail.setToUserId(userId);
            //TODO 记得改币种
            dividendDetail.setLeftCurrencyId(0L);
            dividendDetail.setRightCurrencyId(0L);
            dividendDetail.setDividend(item.getPrice());
            dividendDetail.setThreshold(item.getBalance());
            dividendDetail.setLevel(item.getLevel());
            dividendDetail.setMyAverageThreshold(myDividendData.getBalance());
            dividendDetail.setLevelAverageThreshold(item.getLevelBalance());
            dividendDetail.setRemark(remark);
            dividendDetails.add(dividendDetail);
        });
        dividendDetailRepository.saveAll(dividendDetails);

        //保存账单记录，用户可以再记录里看到
        Bill bill = new Bill();
        bill.setIncome(CommonUtil.toWei(allDividend.toPlainString()));
        bill.setCurrentBalance(userUSDTBalance.getBalance());
        bill.setStatus(BillStatus.SUCCESS);
        bill.setType(BillType.DIVIDEND);
        bill.setCurrencyType(4);
        bill.setRelativeId(dividendLog.getId());
        bill.setBalance(userUSDTBalance);
        billRepository.save(bill);
    }

}
