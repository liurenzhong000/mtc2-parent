package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;

/**
 * 账户托管的余额
 *
 * @author Chinhin
 * 2018/7/23
 */
@Getter
@Setter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "balance_unique", columnNames = {"currencyAddress", "currencyType", "user_id"})
})
public class UserBalance extends BaseEntity {

    public UserBalance() {
    }

    public UserBalance(Long id, Long version, Integer currencyType, String currencyAddress, BigInteger balance, BigInteger freezingAmount, Long userId) {
        this.id = id;
        this.version = version;
        this.currencyType = currencyType;
        this.currencyAddress = currencyAddress;
        this.balance = balance;
        this.freezingAmount = freezingAmount;
        this.userId = userId;
    }

    @Version
    protected Long version = 0L;

    @Column(columnDefinition = "varchar(100) COMMENT '托管钱包地址(暂时都是平台的同一个，后期扩展备用)'", nullable = false)
    private String walletAddress;

    @Column(columnDefinition = "int COMMENT '代币基链类型' 基链类型 1:eth, 2:bch, 3:eos，4:btc")
    private Integer currencyType = 1;

    @Column(columnDefinition = "varchar(100) COMMENT '代币地址'", nullable = false)
    private String currencyAddress;

    @Column(columnDefinition = "decimal(30,0) COMMENT '余额(wei)'")
    private BigInteger balance = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '冻结金额(wei)'")
    private BigInteger freezingAmount = BigInteger.ZERO;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "balance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "userBalance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserBalanceSample> userBalanceSamples;

    @Transient
    private Long userId;

}
