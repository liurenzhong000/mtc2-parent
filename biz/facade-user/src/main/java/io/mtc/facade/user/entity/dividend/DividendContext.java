package io.mtc.facade.user.entity.dividend;

import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import io.mtc.facade.user.repository.UserBalanceRepository;
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

    //  保存所有用户对象
    private Iterable<User> users;

    //  所有参与分成的用户集合
    private List<DividendUser> dividendUsers = new ArrayList<>();

    //  用户bhb账户集合
    private List<UserBalance> userBHBBalances;

    //  用户usdt账户集合
    private List<UserBalance> userUSDTBalances;

    //  用户账户抽样数据集合
    private List<UserBalanceSample> userBalanceSamples;

    private UserBalanceRepository userBalanceRepository;

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
     * 得到BHB价格(RMB)
     *
     * @return
     */
    public BigDecimal getBHBCNYPrice() {
        return DividendConstant.BHB_PRICE;
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
     * 根据用户id,得到用户的BHB账户
     *
     * @param userId 用户id
     * @return
     */
    public UserBalance getBHBBalanceByUserId(Long userId) {
        if (userBHBBalances == null) return null;
        for (UserBalance item : userBHBBalances) {
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
        if (userUSDTBalances == null) return null;
        for (UserBalance item : userUSDTBalances) {
            if (item.getUserId().compareTo(userId) == 0) return item;
        }
        return null;
    }

    /**
     * 根据用户id,得到用户采样信息
     *
     * @param userId 用户id
     * @return
     */
    public List<UserBalanceSample> getBalanceSampleByUserId(Long userId) {
        List<UserBalanceSample> findData = new ArrayList<>();
        for (UserBalanceSample item : userBalanceSamples) {
            if (item.getUserId().compareTo(userId) == 0) findData.add(item);
        }
        return findData;
    }

    /**
     * bhb分成逻辑入口函数
     * 1    遍历bhb分成用户
     * 2    得到用户分成金额,如果为0,则跳过
     * 3    计算当前循环用户的usdt分成数量
     * 4    写入数据库(如果没有USDT账户则直接创建)
     */
    public void dividendPrice() {
        for (DividendUser dividendUser : this.dividendUsers) {
            BigDecimal price = dividendUser.getPrice();
            if (price.compareTo(BigDecimal.ZERO) == 0) continue;
            BigInteger dividendUSDT = CommonUtil.toWei(
                    price.divide(getUSDTCNYPrice(), 5, RoundingMode.HALF_UP).stripTrailingZeros().toString()
            );
            UserBalance userUSDTBalance = this.getUSDTBalanceByUserId(dividendUser.getUser().getId());
            logger.info("用户: " + dividendUser.getUser().getId() + "计算分成金额为: " + price.doubleValue() + "分到usdt--->" + dividendUSDT);
            if (userUSDTBalance == null) {
                userUSDTBalance = new UserBalance();
                userUSDTBalance.setBalance(dividendUSDT);
                userUSDTBalance.setCurrencyAddress("USDT");
                userUSDTBalance.setCurrencyType(4);
                userUSDTBalance.setUser(dividendUser.getUser());
            } else {
                userUSDTBalance.setBalance(userUSDTBalance.getBalance().add(dividendUSDT));
                userUSDTBalance.setUser(dividendUser.getUser());
            }
            //  更新前要查询数据库......
            userBalanceRepository.save(userUSDTBalance);
        }
    }

}
