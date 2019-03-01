package io.mtc.facade.user.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

/**
 * 手机验证码
 *
 * @author Chinhin
 * 2018/7/23
 */
@Getter @Setter
@Entity
public class CodeRecord extends BaseEntity {

    @Column(columnDefinition = "varchar(50) COMMENT '手机号'", nullable = false, unique = true)
    private String phone;

    @Column(columnDefinition = "varchar(10) COMMENT '验证码'", nullable = false)
    private String code;

    // 发送时间
    private Date sendTime;

}