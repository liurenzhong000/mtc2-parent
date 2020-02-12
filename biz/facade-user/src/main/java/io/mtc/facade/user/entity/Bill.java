package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillStatusConverter;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.constants.BillTypeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * 账单表
 *
 * @author Chinhin
 * 2018/7/27
 */
@Getter @Setter
@Entity
public class Bill extends BaseEntity {

    @Column(columnDefinition = "decimal(30,0) COMMENT '支出(wei) '")
    private BigInteger outcome = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '收入(wei)'")
    private BigInteger income = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '支出时候的手续费(wei)'")
    private BigInteger outComeFee = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '余额(wei)'")
    private BigInteger currentBalance = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '退款(wei)'")
    private BigInteger refund = BigInteger.ZERO;

    @Column(columnDefinition = "int COMMENT '状态'")
    @Convert(converter = BillStatusConverter.class)
    private BillStatus status;

    @Column(columnDefinition = "int COMMENT '账单类型'")
    @Convert(converter = BillTypeConverter.class)
    private BillType type;

    @Column(columnDefinition = "int COMMENT '代币基链类型，用于充值提现的时候方便取'")
    private Integer currencyType;

    @Column(columnDefinition = "varchar(200) COMMENT '备注:发红包的人'")
    private String note;

    @Column(columnDefinition = "varchar(200) COMMENT '备注2'")
    private String note2;

    @Column(columnDefinition = "varchar(200) COMMENT '备注2'")
    private String memo;

    @Column(columnDefinition = "bigint COMMENT '关联id'")
    private Long relativeId;

    @Column(columnDefinition = "varchar(200) COMMENT '关联地址: 红包发送人、充值的地址、提现的地址'")
    private String relatedAddress;

    @Column(columnDefinition = "varchar(100) COMMENT '交易hash'")
    private String txHash;

    @JSONField(serialize = false)
    @Column(columnDefinition = "decimal(30,0) COMMENT '交易的nonce'")
    private BigInteger txNonce = BigInteger.ZERO;

    @JSONField(serialize = false)
    @Column(columnDefinition = "bigint COMMENT '本次提现过期时间'")
    private Long withdrawExpireTime;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.ALL}, optional = false)
    @JoinColumn(name = "balance_id", columnDefinition = "bigint COMMENT '关联余额'")
    private UserBalance balance;

}
