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

    public DividendUser(User user, Integer level, DividendContext context) {
        this.user = user;
        this.level = level;
        this.context = context;
    }

    //  用户的下级用户
    private DividendUserList children = new DividendUserList();

    /**
     * 验证用户余额方法,判断出当前用户是否能参与分成
     * 1    判断当前用户是否拥有bhb账户信息
     * 2    判断当前用户的余额是否符合分成的限定值
     * 3    判断当前用户的每小时抽查记录,是否符合要求(每小时BHB持有额度必须达到指定的值)
     *
     * @return
     */
    private BigDecimal _validateBalance() {
        UserBalance userBalance = context.getBHBBalanceByUserId(user.getId());
        if (userBalance == null) return null;
        BigInteger dividendConditionNumber = CommonUtil.toWei(DividendConstant.DIVIDEND_CONDITION_NUMBER.toString());
        if (userBalance.getBalance().compareTo(dividendConditionNumber) < 0) return null;
        boolean flag = true;
        List<UserBalanceSample> userBalanceSamples = context.getBalanceSampleByUserId(user.getId());
        if (userBalanceSamples == null || userBalanceSamples.size() == 0) return null;
        for (UserBalanceSample item : userBalanceSamples) {
            if (item.getBalance().compareTo(dividendConditionNumber) < 0)
                flag = false;
        }
        if (flag == false) return null;
        return CommonUtil.getFormatAmount(userBalance.getBalance().toString());
    }

    /**
     * 计算得到用户分成的金额(RMB)
     * 1    调用验证用户余额的方法,如果验证成功则返回用户余额,否则返回null
     * 2    未通过验证处理:    如果为树形结构中的顶层用户,则返回0,代表不参与分成,否则返回下级用户的所有分成金额
     * 3    如果通过验证,得到用户余额后,根据公式计算出当前用户的分成金额,再加上下级用户的所有分成金额
     * 公式:  当前用户BHB余额 * BHB单价 * 当前等级的分成比率 = 分成金额(RMB)
     *
     * @return
     */
    @Override
    public BigDecimal getPrice() {
        BigDecimal balance = _validateBalance();
        if (balance == null) {
            if (this.level.compareTo(0) == 0) return BigDecimal.ZERO;
            return this.children.getPrice();
        }
        BigDecimal rate = DividendConstant.getRate(this.level);
        return balance.multiply(context.getBHBCNYPrice()).multiply(rate).add(children.getPrice());
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
        if (this.level >= 3) return null;
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
