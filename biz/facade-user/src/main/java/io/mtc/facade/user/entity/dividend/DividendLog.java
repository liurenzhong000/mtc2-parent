package io.mtc.facade.user.entity.dividend;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 10:36
 * @Description: 分红详细记录
 */
@Setter @Getter
@Entity
public class DividendLog extends BaseEntity{

    @Column(columnDefinition = "bigint COMMENT '关联用户'")
    private Long userId;

    @Column(columnDefinition = "decimal(18,4) COMMENT '当前用户平均持有量'")
    private BigDecimal currBalance;

    @Column(columnDefinition = "decimal(18,4) COMMENT '总级收益'")
    private BigDecimal allDividend;

    @Column(columnDefinition = "decimal(18,4) COMMENT '自身收益'")
    private BigDecimal myDividend;

    @Column(columnDefinition = "decimal(18,4) COMMENT '一级收益'")
    private BigDecimal oneDividend;

    @Column(columnDefinition = "int COMMENT '一级达标用户个数'")
    private Integer oneCount;

    @Column(columnDefinition = "varchar(200) COMMENT '一级用户id'")
    private String oneUserIds;

    @Column(columnDefinition = "decimal(18,4) COMMENT '二级收益'")
    private BigDecimal twoDividend;

    @Column(columnDefinition = "int COMMENT '二级达标用户个数'")
    private Integer twoCount;

    @Column(columnDefinition = "varchar(500) COMMENT '二级用户id'")
    private String twoUserIds;

}
