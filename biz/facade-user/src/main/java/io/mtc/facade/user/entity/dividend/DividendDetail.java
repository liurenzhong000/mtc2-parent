package io.mtc.facade.user.entity.dividend;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 11:06
 * @Description: 分红详情
 */
@Setter
@Getter
@Entity
public class DividendDetail extends BaseEntity {

    @Column(columnDefinition = "bigint COMMENT '分红甲方'")
    private Long fromUserId;

    @Column(columnDefinition = "bigint COMMENT '分红乙方, 获得分红的一方'")
    private Long toUserId;

    @Column(columnDefinition = "bigint COMMENT '左币，持有币种'")
    private Long leftCurrencyId;

    @Column(columnDefinition = "bigint COMMENT '右币，能获得分红的币种'")
    private Long rightCurrencyId;

    @Column(columnDefinition = "decimal(18,4) COMMENT '得到的分红'")
    private BigDecimal dividend;

    @Column(columnDefinition = "decimal(18,4) COMMENT '计算基数'")
    private BigDecimal threshold;

    @Column(columnDefinition = "int COMMENT '用户级别'")
    private Integer level;

    @Column(columnDefinition = "decimal(18,4) COMMENT '我的平均持仓'")
    private BigDecimal myAverageThreshold;

    @Column(columnDefinition = "decimal(18,4) COMMENT '下级的平均持仓'")
    private BigDecimal levelAverageThreshold;

    @Column(columnDefinition = "varchar(500) COMMENT '相关说明，计算比例，币种价格等'")
    private String remark;

}
