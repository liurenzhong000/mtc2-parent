package io.mtc.facade.backend.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * 权限
 *
 * @author Chinhin
 * 2018/6/9
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Entity
public class AdminPermission extends BaseEntity {

    public AdminPermission(String name, String permission) {
        this.name = name;
        this.permission = permission;
    }

    @Column(columnDefinition = "varchar(32) COMMENT '权限值'", nullable = false, unique = true)
    private String permission;

    @Column(columnDefinition = "varchar(50) COMMENT '权限名'", nullable = false, unique = true)
    private String name;

    @JSONField(serialize = false)
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<AdminRole> roles;

}
