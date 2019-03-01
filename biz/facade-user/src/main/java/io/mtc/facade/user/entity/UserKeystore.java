package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 账户托管的keystore
 *
 * @author Chinhin
 * 2018/7/23
 */
@Getter @Setter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"walletAddress", "currencyType", "user_id"})
})
public class UserKeystore extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '钱包名称'", nullable = false)
    private String walletName;

    @Column(columnDefinition = "varchar(100) COMMENT '钱包地址'", nullable = false)
    private String walletAddress;

    @Column(columnDefinition = "int COMMENT '代币基链类型'")
    private Integer currencyType = 1;

    @JSONField(serialize = false)
    @Column(columnDefinition = "longtext COMMENT 'keystore'", nullable = false)
    private String keystore;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

}
