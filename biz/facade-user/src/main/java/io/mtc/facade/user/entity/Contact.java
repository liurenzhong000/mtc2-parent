package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 联系人
 *
 * @author Chinhin
 * 2018/9/11
 */
@Getter @Setter
@Entity
public class Contact extends BaseEntity {

    @Column(columnDefinition = "varchar(200) COMMENT '头像'")
    private String photo = Constants.EMPTY;

    @Column(columnDefinition = "varchar(200) COMMENT '姓名'")
    private String name;

    @Column(columnDefinition = "varchar(200) COMMENT '托管账户'")
    private String hostName;

    @Column(columnDefinition = "varchar(100) COMMENT 'eth钱包地址'")
    private String ethAddress;

    @Column(columnDefinition = "varchar(100) COMMENT 'eos钱包地址'")
    private String eosAddress;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

}
