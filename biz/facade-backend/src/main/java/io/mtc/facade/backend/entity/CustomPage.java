package io.mtc.facade.backend.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 自定义页面的表
 *
 * @author Chinhin
 * 2018/6/20
 */
@Getter @Setter
@Entity
public class CustomPage extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '标题'", nullable = false)
    private String title;

    @Column(columnDefinition = "varchar(20) COMMENT '链接标签'", nullable = false)
    private String linkTag;

    @Column(columnDefinition = "longtext COMMENT '内容'", nullable = false)
    private String content;

}
