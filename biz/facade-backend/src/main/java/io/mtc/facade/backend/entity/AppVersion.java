package io.mtc.facade.backend.entity;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * App版本管理
 *
 * @author Chinhin
 * 2018/7/5
 */
@Setter @Getter
@Entity
public class AppVersion extends BaseEntity {

    @Column(columnDefinition = "varchar(20) COMMENT '版本号'", nullable = false)
    String versionNumber;

    @Column(columnDefinition = "varchar(20) COMMENT '版本名'", nullable = false)
    String versionName;

    @Column(columnDefinition = "varchar(1000) COMMENT '版本描述'")
    String description;

    @Column(columnDefinition = "varchar(1000) COMMENT '版本描述(英文)'")
    String descriptionEn;

    @Column(columnDefinition = "varchar(200) COMMENT '下载地址'")
    String url;

    @Column(columnDefinition = "bit COMMENT '是否有效'")
    Boolean isActive = false;

    @Column(columnDefinition = "bit COMMENT '是否安卓，false为苹果'")
    Boolean isAndroid;

}
