package io.mtc.facade.user.entity.wheel;

import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 转盘中奖记录
 *
 * @author Chinhin
 * 2018/12/24
 */
@Getter @Setter
@Entity
public class WheelRecord extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '奖品名字'", nullable = false)
    private String name;

    @Column(columnDefinition = "varchar(200) COMMENT '奖品图片'")
    private String pic = Constants.EMPTY;

    @Column(columnDefinition = "int COMMENT '1表示已发放，2表示未发放'")
    private Integer status = 2;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '获得奖品的用户'")
    private User user;

}
