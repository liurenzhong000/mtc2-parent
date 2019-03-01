package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * 用户分配的钱包地址
 *
 * @author Chinhin
 * 2019-01-18
 */
@Getter @Setter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"walletAddress", "currencyType"})
})
public class UserWallet extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '托管钱包地址'", nullable = false)
    private String walletAddress;

    @Column(columnDefinition = "int COMMENT '代币基链类型'")
    private Integer currencyType = 1;

    @Column(columnDefinition = "varchar(200) COMMENT '加密后的私钥'", nullable = false)
    private String secret;

    @Column(columnDefinition = "decimal(30,0) COMMENT '手续费余额(wei)'")
    private BigInteger feeBalance = BigInteger.ZERO;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

}
