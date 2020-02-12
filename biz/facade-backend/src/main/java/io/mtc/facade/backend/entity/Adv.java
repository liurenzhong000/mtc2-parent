package io.mtc.facade.backend.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * 广告
 *
 * @author Chinhin
 * 2018/8/27
 */
@Setter @Getter
//@Entity
@Deprecated
public class Adv extends BaseEntity {

    @Column(columnDefinition = "varchar(200) COMMENT '点击跳转的网页链接(type为1有效)'")
    String url;

    @Column(columnDefinition = "varchar(200) COMMENT '文件或图片全路径'")
    String file;

    @Column(columnDefinition = "bit COMMENT '是否有效'")
    Boolean isActive = false;

    @Column(columnDefinition = "int COMMENT '点击跳转类型：1网页'")
    Integer type;

    @Column(columnDefinition = "bit COMMENT '是否加载页'")
    Boolean isLaunchScreen = false;

}
