package io.mtc.facade.user.entity.dividend;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.constants.DividendConstant;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.UserBalanceSample;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Setter
@Getter
public class DividendUser implements Dividend {

    //  上下文对象
    @JSONField(serialize = false)
    private DividendContext context;

    private User user;

    //  当前用户所处动态分成等级(0:静态,1:动态一级,2:动态二级,3:动态三级)
    private Integer level;

    //  用户的下级用户
    private DividendUserList children = new DividendUserList();

    public DividendUser(User user, Integer level, DividendContext context) {
        this.user = user;
        this.level = level;
        this.context = context;
    }

    /**
     * 验证用户余额方法,判断出当前用户是否能参与分成
     * 1    判断当前用户是否拥有zcd账户信息
     * 2    判断当前用户的余额是否符合分成的限定值
     * 3    判断当前用户的每小时抽查记录,是否符合要求(每小时ZCD持有额度必须达到指定的值)
     *
     * @return 分红基数 -- 25次的平均值
     */
    private BigDecimal _validateBalance() {
        UserBalance userBalance = context.getZCDBalanceByUserId(user.getId());
        if (userBalance == null) return null;
        BigInteger dividendConditionNumber = CommonUtil.toWei(DividendConstant.DIVIDEND_CONDITION_NUMBER.toString());
        if (userBalance.getBalance().compareTo(dividendConditionNumber) < 0) return null;
//        List<UserBalanceSample> userBalanceSamples = context.getBalanceSampleByUserId(user.getId());
        UserBalanceSampleAvg userBalanceSampleAvg = context.getBalanceSampleAvgByUserId(user.getId());
        //取到的都是满足的记录，只要统计一下在区间是否达到25次
        if (userBalanceSampleAvg == null) return null;
//        BigDecimal allBalance = userBalanceSamples.stream().map(UserBalanceSample::getDividendAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgBalance = userBalanceSampleAvg.getAvgBalance();
        return avgBalance;
    }

    /**
     * 计算得到用户分成的金额(RMB)
     * 1    调用验证用户余额的方法,如果验证成功则返回用户余额,否则返回null
     * 2    未通过验证处理:    如果为树形结构中的顶层用户,则返回0,代表不参与分成,否则返回下级用户的所有分成金额
     * 3    如果通过验证,得到用户余额后,根据公式计算出当前用户的分成金额,再加上下级用户的所有分成金额
     * 公式:  当前用户BHB余额 * BHB单价 * 当前等级的分成比率 = 分成金额(RMB)
     *
     * @return 返回的是RMB金额
     */
    @Override
    public List<DividendData> getPrice(List<DividendData> dividendDataList) {
        //验证并返回分红基数
        BigDecimal levelBalance = _validateBalance();
        if (levelBalance == null) {
            //自身不达标，返回0
            if (this.level.compareTo(0) == 0) {
                DividendData dividendData = new DividendData(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, this.getUser().getId());
                dividendDataList.add(dividendData);
                return dividendDataList;
            }
            return this.children.getPrice(dividendDataList);
        }
        BigDecimal rate = DividendConstant.getRate(this.level);
        if (this.level.compareTo(0) == 0) {
            //如果该用户的一级达标的不足3个，不构建二级
            this.children.notGetFromTwoChildes = notGetFromTwoChildes();
            this.children.masterBalance = levelBalance;
            levelBalance = levelBalance.compareTo(DividendConstant.DIVIDEND_MAX_NUMBER) >= 0 ? DividendConstant.DIVIDEND_MAX_NUMBER : levelBalance;
            DividendData dividendData = new DividendData(0, levelBalance, levelBalance, levelBalance.multiply(context.getZCDUSDT()).multiply(rate), this.getUser().getId());
            dividendDataList.add(dividendData);
            return this.children.getPrice(dividendDataList);
//            return levelBalance.multiply(context.getZCDCNYPrice()).multiply(rate).add(children.getPrice());
        } else {
            //获得下级所获得分红的多少
            BigDecimal masterBalance = this.children.masterBalance;
            //两者取最小
            BigDecimal balance = levelBalance.compareTo(masterBalance) >= 0 ? masterBalance : levelBalance;
            //收益最大值限制
            balance = balance.compareTo(DividendConstant.DIVIDEND_MAX_NUMBER) >= 0 ? DividendConstant.DIVIDEND_MAX_NUMBER : balance;
            DividendData dividendData = new DividendData(this.level, balance, levelBalance, balance.multiply(context.getZCDUSDT()).multiply(rate).multiply(DividendConstant.getRate(0)), this.getUser().getId());
            dividendDataList.add(dividendData);
            return this.children.getPrice(dividendDataList);
        }
     }

    //看当前用户达标的满不满足3个
    private boolean notGetFromTwoChildes(){
        String email = this.user.getEmail();
        String phone = this.user.getPhone();
        List<User> promoterUsers = this.context.findPromoterByEmailOrPhone(email, phone);
        int flag = 0;
        if (promoterUsers != null && promoterUsers.size() > 0) {
            for (User userItem : promoterUsers) {
//                UserBalance userBalance = this.context.getZCDBalanceByUserId(userItem.getId());
                UserBalanceSampleAvg userBalanceSampleAvg = this.context.getBalanceSampleAvgByUserId(userItem.getId());
                if (userBalanceSampleAvg != null) {
                    ++flag;
                }
            }
        }
        return flag < 3;
    }

    /**
     * 生成用户的树形结构(分成层级)
     * 1    判断当前用户等级,是否为最后一层,如果为最后一层,则不进行递归
     * 2    得到当前用户的email,phone
     * 3    从上下文对象中取得下级用户的信息
     * 4    设置当前用户的下级用户,并进行递归创建下级用户
     *
     * @return DividendUser
     */
    public DividendUser buildTree() {
        if (this.level >= 2) return null;
        String email = this.user.getEmail();
        String phone = this.user.getPhone();
        List<User> promoterUsers = this.context.findPromoterByEmailOrPhone(email, phone);
        if (promoterUsers != null && promoterUsers.size() > 0) {
            for (User userItem : promoterUsers) {
                this.children.addUser(new DividendUser(userItem, this.level + 1, context)).buildTree();
            }
        }
        return this;
    }
}
