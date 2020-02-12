package io.mtc.facade.user.entity.dividend;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 16:39
 * @Description: 用户余额快照平均值
 */
@Setter
@Getter
public class UserBalanceSampleAvg {

    private Long userId;

    private Long counts;

    private BigDecimal avgBalance;

    public UserBalanceSampleAvg(Long userId, Long counts, Double avgBalance) {
        this.userId = userId;
        this.counts = counts;
        this.avgBalance = new BigDecimal(avgBalance.toString());
    }
}
