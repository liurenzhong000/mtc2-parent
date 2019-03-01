package io.mtc.service.currency.entity;


import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * 数据
 *
 * @author Chinhin
 * 2018/6/16
 */
@Getter @Setter
@Entity
public class Currency extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '地址'", nullable = false, unique = true)
    private String address;

    @Column(columnDefinition = "varchar(100) COMMENT '名称'", nullable = false)
    private String name;

    @Column(columnDefinition = "varchar(200) COMMENT '图片地址'", nullable = false)
    private String image;

    @Column(columnDefinition = "varchar(20) COMMENT '简称'", nullable = false)
    private String shortName;

    @Column(columnDefinition = "decimal(25,7) COMMENT '市场价（美元）'", nullable = false)
    private BigDecimal price;

    @Column(columnDefinition = "decimal(25,7) COMMENT '市场价（人民币）'")
    private BigDecimal cnyPrice;

    @Column(columnDefinition = "varchar(100) COMMENT '提现手续费'", nullable = false)
    private String fee;

    @Column(columnDefinition = "int COMMENT '来源类型 1:本地，2:block.cc，3:交易所'")
    private Integer sourceType;

    @Column(columnDefinition = "varchar(100) COMMENT '来源Key'")
    private String sourceSystemId;

    @Column(columnDefinition = "int COMMENT '基链类型 1:eth, 2:bch, 3:eos，4:btc'")
    private Integer baseType = 1;

    @Column(columnDefinition = "bit COMMENT '是否默认显示'")
    private Boolean isDefaultVisible = false;

    @Column(columnDefinition = "bit COMMENT '是否有效'")
    private Boolean isEnabled = true;

    @Column(columnDefinition = "bit COMMENT '是否支持发红包'")
    private Boolean redPacketEnabled = false;

    @Column(columnDefinition = "bit COMMENT '是否支持托管'")
    private Boolean hostEnabled = false;

    @Column(columnDefinition = "varchar(200) COMMENT '备注说明'")
    private String note;

    @Column(columnDefinition = "decimal(10,4) COMMENT '涨跌(小时)'")
    private BigDecimal changeHourly = BigDecimal.ZERO;

}
