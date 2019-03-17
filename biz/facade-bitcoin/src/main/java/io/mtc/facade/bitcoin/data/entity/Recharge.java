package io.mtc.facade.bitcoin.data.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.bitcoin.data.enums.RechargeStatus;
import io.mtc.facade.bitcoin.data.enums.RechargeStatusConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 10:05
 * @Description: Omni钱包扫描到的充值记录
 */
@Getter
@Setter
@Entity
@Table(name="recharge",uniqueConstraints ={@UniqueConstraint(columnNames="txId")})
public class Recharge extends BaseEntity {

    @Column(columnDefinition = "bigint COMMENT '币种id'")
    private Long currencyId;

    @Column(columnDefinition = "bigint COMMENT '用户id'")
    private Long userId;

    @Column(columnDefinition = "varchar(100) COMMENT '转出地址'")
    private String fromAddress;

    @Column(columnDefinition = "varchar(100) COMMENT '转入地址'")
    private String toAddress;

    @Column(columnDefinition = "decimal(18,18) COMMENT '充值数量 '")
    private BigDecimal qty;

    @Column(columnDefinition = "varchar(100) COMMENT '交易hash'")
    private String txId;

    @Column(columnDefinition = "varchar(100) COMMENT '备注'")
    private String remark;

    @Column(columnDefinition = "int COMMENT '状态'")
    @Convert(converter = RechargeStatusConverter.class)
    private RechargeStatus status;
}
