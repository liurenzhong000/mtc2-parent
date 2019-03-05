package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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

    public UserBalanceSample(BigInteger balance, BigInteger freezingAmount, UserBalance userBalance) {
        this.balance = balance;
        this.freezingAmount = freezingAmount;
        this.userBalance = userBalance;
    }

    public UserBalanceSample(Long id, BigInteger balance, BigInteger freezingAmount, Long userId, Long userBalanceId) {
        this.id = id;
        this.balance = balance;
        this.freezingAmount = freezingAmount;
        this.userId = userId;
        this.userBalanceId = userBalanceId;
    }

    @Version
    protected Long version = 0L;

    @Column(columnDefinition = "datetime COMMENT '钱包抽样时间'", nullable = false)
    private Date createTime = new Date();

    @Column(columnDefinition = "decimal(30,0) COMMENT '余额(wei)'")
    private BigInteger balance = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '冻结金额(wei)'")
    private BigInteger freezingAmount = BigInteger.ZERO;

    @JSONField(serialize = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_balance_id", columnDefinition = "bigint COMMENT '关联托管账户余额'")
    private UserBalance userBalance;

    @Transient
    private Long userId;
    @Transient
    private Long userBalanceId;

}
