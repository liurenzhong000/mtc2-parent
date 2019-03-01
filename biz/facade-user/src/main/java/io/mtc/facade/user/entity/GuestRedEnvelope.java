package io.mtc.facade.user.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * 游客红包
 *
 * @author Chinhin
 * 2018/8/29
 */
@Getter @Setter
@Entity
public class GuestRedEnvelope extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "envelope_id", columnDefinition = "bigint COMMENT '红包id'")
    private RedEnvelope redEnvelope;

    @Column(columnDefinition = "varchar(200) COMMENT '设备唯一编号'", unique = true)
    private String deviceId;

    @Column(columnDefinition = "decimal(30,0) COMMENT '抢到金额(wei)'")
    private BigInteger amount = BigInteger.ZERO;

}
