package io.mtc.facade.backend.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * 角色
 *
 * @author Chinhin
 * 2018/6/8
 */
@Setter @Getter
@Entity
public class AdminRole extends BaseEntity {

    @Column(columnDefinition = "varchar(20) COMMENT '角色名'", nullable = false, unique = true)
    private String rolename;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdminUser> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<AdminPermission> permissions;

}
