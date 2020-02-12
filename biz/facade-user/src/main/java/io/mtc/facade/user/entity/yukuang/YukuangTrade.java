package io.mtc.facade.user.entity.yukuang;

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

/**
 * 云矿交易
 *
 * @author Chinhin
 * 2018/12/6
 */
@Getter @Setter
@Entity
public class YukuangTrade extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "bill_id", columnDefinition = "bigint COMMENT '账单id'")
    private Bill bill;

    @Column(columnDefinition = "datetime COMMENT '交易时间'")
    private Date tradeTime;

    @Column(columnDefinition = "decimal(25,7) COMMENT '支付金额(人民币)'")
    private BigDecimal cnyMoney;

    @Column(columnDefinition = "varchar(100) COMMENT '支付币种'", nullable = false)
    private String payCurrency;

    @Column(columnDefinition = "decimal(25,7) COMMENT '币价(人民币)'")
    private BigDecimal currencyCnyPrice;

    @Column(columnDefinition = "decimal(30,0) COMMENT '实际支付数量(wei)'")
    private BigInteger actCost = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT 'imc数量(wei)'")
    private BigInteger imc = BigInteger.ZERO;

    @Column(columnDefinition = "varchar(200) COMMENT '云矿订单ID'")
    private String orderNo;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "pay_user_id", columnDefinition = "bigint COMMENT '付款帐号'")
    private User payUser;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "receive_user_id", columnDefinition = "bigint COMMENT '收款帐号'")
    private User receiveUser;

}
