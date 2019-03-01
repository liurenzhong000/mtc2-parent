package io.mtc.service.notification.entity;

import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 管理员发送通知表
 *
 * @author Chinhin
 * 2018/7/9
 */
@Getter @Setter
@Entity
public class NotificationTemplet extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '标题'")
    private String title = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '内容'")
    private String content = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '链接地址'")
    private String url = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '标题EN'")
    private String titleEN = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '内容EN'")
    private String contentEN = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '链接地址EN'")
    private String urlEN = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '标题KO'")
    private String titleKO = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '内容KO'")
    private String contentKO = Constants.EMPTY;

    @Column(columnDefinition = "varchar(100) COMMENT '链接地址KO'")
    private String urlKO = Constants.EMPTY;
}