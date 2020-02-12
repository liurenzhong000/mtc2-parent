package io.mtc.facade.user.entity;

import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigInteger;

/**
 * 后台管理员更改托管账户余额的记录
 *
 * @author Chinhin
 * 2018/12/13
 */
@Getter @Setter
@Entity
public class BalanceUpdateRecord extends BaseEntity {

    @Column(columnDefinition = "decimal(30,0) COMMENT '增减前多少代币(wei) '")
    private BigInteger beforeNum = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '增减后多少代币(wei) '")
    private BigInteger afterNum = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '增加多少代币，负数为减少(wei) '")
    private BigInteger addNum = BigInteger.ZERO;

    @Column(columnDefinition = "varchar(200) COMMENT '备注'", nullable = false)
    private String note;

    @Column(columnDefinition = "varchar(20) COMMENT '操作人名'", nullable = false)
    private String adminName;

    @Column(columnDefinition = "bigint COMMENT '修改的用户id'")
    private Long uid;

    @Column(columnDefinition = "varchar(50) COMMENT '修改的用户名'")
    private String username = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '修改的用户合约地址'", nullable = false)
    private String currencyAddress;

}
