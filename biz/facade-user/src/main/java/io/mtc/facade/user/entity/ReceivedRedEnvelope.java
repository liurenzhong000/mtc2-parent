package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * 收到的红包(可能未打开)
 *
 * @author Chinhin
 * 2018/7/31
 */
@Getter @Setter
@Entity
public class ReceivedRedEnvelope extends BaseEntity {

    @Column(columnDefinition = "bit COMMENT '是否打开了红包'")
    private Boolean isOpened = false;

    @Column(columnDefinition = "decimal(30,0) COMMENT '抢到金额(wei)'")
    private BigInteger amount = BigInteger.ZERO;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '收到红包的用户id'")
    private User user;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "envelope_id", columnDefinition = "bigint COMMENT '红包id'")
    private RedEnvelope redEnvelope;

}