package io.mtc.facade.backend.config;

import io.mtc.facade.backend.config.security.SecurityPwdEncode;
import io.mtc.facade.backend.entity.AdminPermission;
import io.mtc.facade.backend.entity.AdminRole;
import io.mtc.facade.backend.entity.AdminUser;
import io.mtc.facade.backend.repository.AdminPermissionRepository;
import io.mtc.facade.backend.repository.AdminRoleRepository;
import io.mtc.facade.backend.repository.AdminUserRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统启动初始化
 *
 * @author Chinhin
 * 2018/6/11
 */
@Component
public class Bootstrap {

    @Resource
    private AdminPermissionRepository adminPermissionRepository;

    @Resource
    private AdminRoleRepository adminRoleRepository;

    @Resource
    private AdminUserRepository adminUserRepository;

    @Resource
    private SecurityPwdEncode securityPwdEncode;

    @PostConstruct
    public void initAdminData() {
        // 没有权限，则建一个所有权限的权限
        if (adminPermissionRepository.count() == 0) {

            Set<AdminPermission> permissions = new HashSet<>();

            permissions.add(new AdminPermission("权限-检索", "permission:select"));
            permissions.add(new AdminPermission("权限-新增", "permission:insert"));
            permissions.add(new AdminPermission("权限-修改", "permission:update"));
            permissions.add(new AdminPermission("权限-删除", "permission:delete"));

            permissions.add(new AdminPermission("角色-检索", "role:select"));
            permissions.add(new AdminPermission("角色-新增", "role:insert"));
            permissions.add(new AdminPermission("角色-修改", "role:update"));
            permissions.add(new AdminPermission("角色-删除", "role:delete"));

            permissions.add(new AdminPermission("管理员-检索", "admin:select"));
            permissions.add(new AdminPermission("管理员-新增", "admin:insert"));
            permissions.add(new AdminPermission("管理员-修改", "admin:update"));
            permissions.add(new AdminPermission("管理员-删除", "admin:delete"));

            permissions.add(new AdminPermission("币种-检索", "currency:select"));
            permissions.add(new AdminPermission("币种-新增", "currency:insert"));
            permissions.add(new AdminPermission("币种-修改", "currency:update"));
            permissions.add(new AdminPermission("币种-删除", "currency:delete"));

            permissions.add(new AdminPermission("交易记录-检索", "trans:select"));
            permissions.add(new AdminPermission("交易记录-新增", "trans:insert"));
            permissions.add(new AdminPermission("交易记录-删除", "trans:delete"));
            permissions.add(new AdminPermission("交易记录-修改", "trans:update"));

            permissions.add(new AdminPermission("自定义网页-检索", "custom:select"));
            permissions.add(new AdminPermission("自定义网页-新增", "custom:insert"));
            permissions.add(new AdminPermission("自定义网页-修改", "custom:update"));
            permissions.add(new AdminPermission("自定义网页-删除", "custom:delete"));

            permissions.add(new AdminPermission("App版本管理-检索", "appVersion:select"));
            permissions.add(new AdminPermission("App版本管理-新增", "appVersion:insert"));
            permissions.add(new AdminPermission("App版本管理-修改", "appVersion:update"));
            permissions.add(new AdminPermission("App版本管理-删除", "appVersion:delete"));

            permissions.add(new AdminPermission("推送记录-检索", "notifyRecord:select"));
            permissions.add(new AdminPermission("推送记录-删除", "notifyRecord:delete"));

            permissions.add(new AdminPermission("推送模板-检索", "notifyTemplate:select"));
            permissions.add(new AdminPermission("推送模板-新增", "notifyTemplate:insert"));
            permissions.add(new AdminPermission("推送模板-修改", "notifyTemplate:update"));
            permissions.add(new AdminPermission("推送模板-删除", "notifyTemplate:delete"));

            permissions.add(new AdminPermission("APP加载页-设置", "launchScreen:update"));

            permissions.add(new AdminPermission("托管用户-查询", "user:select"));
            permissions.add(new AdminPermission("托管用户-更新推荐人", "user:updatePromoter"));

            permissions.add(new AdminPermission("账单-查询", "bill:select"));

            permissions.add(new AdminPermission("借款配置-更新", "loanConfig:update"));
            permissions.add(new AdminPermission("借款记录-查看", "loanRecord:select"));
            permissions.add(new AdminPermission("借款记录-修改", "loanRecord:update"));
            permissions.add(new AdminPermission("借款奖励-查看", "loanBonus:select"));
            permissions.add(new AdminPermission("借款奖励-修改", "loanBonus:update"));

            permissions.add(new AdminPermission("转盘-配置", "wheel:update"));
            permissions.add(new AdminPermission("转盘-抽奖记录", "wheel:record"));

            adminPermissionRepository.saveAll(permissions);

            AdminRole adminRole = new AdminRole();
            adminRole.setRolename("超级管理员");
            adminRole.setPermissions(permissions);
            adminRoleRepository.save(adminRole);

            AdminUser adminUser = new AdminUser();
            adminUser.setUsername("admin");
            adminUser.setPassword(securityPwdEncode.encode("123456"));
            adminUser.setRole(adminRole);

            adminUserRepository.save(adminUser);
        }
    }

}
