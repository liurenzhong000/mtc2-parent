package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;

/**
 * 红包
 *
 * @author Chinhin
 * 2018/7/31
 */
@Getter @Setter
@Entity
public class RedEnvelope extends BaseEntity {

    @Column(columnDefinition = "varchar(50) COMMENT '内容'", nullable = false)
    private String content;

    @Column(columnDefinition = "varchar(100) COMMENT '代币图片地址'", nullable = false)
    private String currencyImage;

    @Column(columnDefinition = "varchar(100) COMMENT '代币地址'", nullable = false)
    private String currencyAddress;

    @Column(columnDefinition = "int COMMENT '代币基链类型'")
    private Integer currencyType = 1;

    @Column(columnDefinition = "varchar(50) COMMENT '代币简称'")
    private String currencyShortName;

    @Column(columnDefinition = "int COMMENT '红包类型 1:拼手气, 2:等额'")
    private Integer type;

    @Column(columnDefinition = "decimal(30,0) COMMENT '总额(wei)'")
    private BigInteger amount;

    @Column(columnDefinition = "int COMMENT '红包个数'")
    private Integer num;

    @Column(columnDefinition = "int COMMENT '状态：1进行中，2暂停，3结束'")
    private Integer status = 1;

    @Column(columnDefinition = "bit COMMENT '是否抢光了'")
    private Boolean isGrabbedOut = false;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '发红包的用户'")
    private User user;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "redEnvelope", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceivedRedEnvelope> receivedRedEnvelopes;

    @Column(columnDefinition = "bigint COMMENT '账单id'")
    private Long billId;

}