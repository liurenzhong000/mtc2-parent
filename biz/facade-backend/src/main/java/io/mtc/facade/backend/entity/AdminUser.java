package io.mtc.facade.backend.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

/**
 * 系统管理员
 *
 * @author Chinhin
 * 2018/6/8
 */
@Setter @Getter
@Entity
public class AdminUser extends BaseEntity implements UserDetails  {

    @Column(columnDefinition = "varchar(20) COMMENT '用户登录名'", nullable = false, unique = true)
    private String username;

    @Column(columnDefinition = "varchar(50) COMMENT '用户登录密码'", nullable = false)
    private String password;

    @Column(columnDefinition = "bit COMMENT '是否可用'")
    private Boolean isEnabled = true;

    @Column(columnDefinition = "varchar(20) COMMENT '手机号'")
    private String phone;

    @Column(columnDefinition = "varchar(59) COMMENT '邮件地址'")
    private String email;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)// optional=false,表示role不能为空
    @JoinColumn(name = "role_id", columnDefinition = "bigint COMMENT '关联角色ID'")
    private AdminRole role;

    @JSONField(serialize = false)
    @Transient
    private List<GrantedAuthority> auths;

    @JSONField(serialize = false)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return auths;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
