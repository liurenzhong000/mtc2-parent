package io.mtc.facade.user.entity.wheel;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * 转盘奖项
 *
 * @author Chinhin
 * 2018/12/24
 */
@Getter @Setter
@Entity
public class WheelPrize extends BaseEntity {

    @Column(columnDefinition = "int COMMENT '来源类型 1未中奖，2奖品'")
    private Integer type;

    @Column(columnDefinition = "varchar(100) COMMENT '奖品名字'", nullable = false)
    private String name;

    @Column(columnDefinition = "varchar(200) COMMENT '奖品图片'")
    private String pic = Constants.EMPTY;

    @Column(columnDefinition = "int COMMENT '库存'")
    private Integer stock;

    @Column(columnDefinition = "decimal(4,2) COMMENT '中奖概率'")
    private BigDecimal rate;

}
