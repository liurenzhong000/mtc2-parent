package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 账户托管余额的每小时平均金额
 */
@Getter
@Setter
@Entity
@Table(
        indexes = {@Index(name = "time", columnList = "createTime")}
)
public class UserBalanceSample extends BaseEntity {

    public UserBalanceSample() {
    }

    public UserBalanceSample(BigDecimal balance, BigDecimal freezingAmount, BigDecimal freezingDividendAmount,
                             BigDecimal dividendAmount, UserBalance userBalance) {
        this.balance = balance;
        this.freezingAmount = freezingAmount;
        this.dividendAmount = dividendAmount;
        this.freezingDividendAmount = freezingDividendAmount;
        this.userId = userBalance.getUserId();
        this.currencyAddress = userBalance.getCurrencyAddress();
        this.currencyType = userBalance.getCurrencyType();
        this.userBalance = userBalance;
    }

    public UserBalanceSample(Long id, BigDecimal balance, BigDecimal freezingAmount, BigDecimal dividendAmount, Long userId, Long userBalanceId) {
        this.id = id;
        this.balance = balance;
        this.freezingAmount = freezingAmount;
        this.dividendAmount = dividendAmount;
        this.userId = userId;
        this.userBalanceId = userBalanceId;
    }

    @Version
    protected Long version = 0L;

    @Column(columnDefinition = "datetime COMMENT '钱包抽样时间'", nullable = false)
    private Date createTime = new Date();

    @Column(columnDefinition = "decimal(18,4) COMMENT '余额'")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(18,4) COMMENT '冻结金额'")
    private BigDecimal freezingAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(18,4) COMMENT '可获得分红的冻结数'")
    private BigDecimal freezingDividendAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(18,4) COMMENT '用于分红的个数 = 余额 - 冻结金额 + 可获得分红的冻结数'")
    private BigDecimal dividendAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "bigint COMMENT '关联托管用户id'")
    private Long userId;

    @Column(columnDefinition = "int COMMENT '代币基链类型 基链类型 1:eth, 2:bch, 3:eos，4:btc'")
    private Integer currencyType = 1;

    @Column(columnDefinition = "varchar(100) COMMENT '代币地址'", nullable = false)
    private String currencyAddress;

    @JSONField(serialize = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_balance_id", columnDefinition = "bigint COMMENT '关联托管账户余额'")
    private UserBalance userBalance;

    @Transient
    private Long userBalanceId;

}
