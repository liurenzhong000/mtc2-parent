package io.mtc.service.notification.entity;

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
public class Notification extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '钱包地址'")
    private String address;

    @Column(columnDefinition = "varchar(100) COMMENT '标题'")
    private String title;

    @Column(columnDefinition = "varchar(100) COMMENT '内容'")
    private String content;

    @Column(columnDefinition = "int COMMENT '通知类型 1:交易通知, 2:后台推送通知'")
    private Integer type;

    @Column(columnDefinition = "varchar(100) COMMENT '链接地址(type为2有可能有值)'")
    private String url;

    @Column(columnDefinition = "varchar(100) COMMENT '交易hash(type为1有值)'")
    private String txHash;

    @Column(columnDefinition = "bit COMMENT '是否付款方'")
    private Boolean isSender = false;

    @Column(columnDefinition = "varchar(100) COMMENT '交易的另一方钱包地址'")
    private String otherAddress;

}