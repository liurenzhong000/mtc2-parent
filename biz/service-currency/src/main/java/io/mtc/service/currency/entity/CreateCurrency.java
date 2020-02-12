package io.mtc.service.currency.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

/**
 * 发币
 *
 * @author Chinhin
 * 2018/8/15
 */
@Getter @Setter
@Entity
public class CreateCurrency extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '代币名称'", nullable = false)
    private String name;

    @Column(columnDefinition = "varchar(100) COMMENT '缩写符号'", nullable = false)
    private String symbol;

    @Column(columnDefinition = "varchar(200) COMMENT '图片地址'")
    private String image;

    @Column(columnDefinition = "decimal(30,0) COMMENT '发行数量'")
    private BigInteger supply;

    @Column(columnDefinition = "varchar(200) COMMENT 'Token官网'", nullable = false)
    private String website;

    @Column(columnDefinition = "varchar(200) COMMENT '描述'")
    private String description;

    @Column(columnDefinition = "varchar(100) COMMENT '收币地址'", nullable = false)
    private String ownerAddress;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "category_id", columnDefinition = "bigint COMMENT '关联分类ID'")
    private Category category;

    @JSONField(serialize = false)
    @Column(columnDefinition = "bigint COMMENT '创建用户id'")
    private Long uid;

    @JSONField(serialize = false)
    @Column(columnDefinition = "bigint COMMENT '本次提现过期时间'")
    private Long expireTime;

    @Column(columnDefinition = "int COMMENT '状态：1排队中，2创建中，3创建成功，4创建失败'")
    private Integer status = 1;

    @Column(columnDefinition = "varchar(100) COMMENT '代币地址'")
    private String tokenAddress;

    @Column(columnDefinition = "varchar(100) COMMENT '交易hash'")
    private String txHash;

    @JSONField(serialize = false)
    @Column(columnDefinition = "decimal(30,0) COMMENT '交易的nonce'")
    private BigInteger txNonce = BigInteger.ZERO;

    private Date successTime;

}