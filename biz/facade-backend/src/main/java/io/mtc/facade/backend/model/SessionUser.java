package io.mtc.facade.backend.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * session里存放的管理员信息
 *
 * @author Chinhin
 * 2018/6/11
 */
@Data
public class SessionUser {

    private Long id;

    private String username;

    private Boolean isEnabled = true;

    private String phone;

    private String email;

    private Long version;

    private Date createTime = new Date();

    private Date updateTime;

    // 不能复制的
    private String roleName;

    private Map<String, Boolean> auths;

}
