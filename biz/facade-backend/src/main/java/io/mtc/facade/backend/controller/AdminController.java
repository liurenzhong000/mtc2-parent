package io.mtc.facade.backend.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.config.security.SecurityPwdEncode;
import io.mtc.facade.backend.entity.AdminRole;
import io.mtc.facade.backend.entity.AdminUser;
import io.mtc.facade.backend.repository.AdminRoleRepository;
import io.mtc.facade.backend.repository.AdminUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员管理类
 *
 * @author Chinhin
 * 2018/6/14
 */
@Slf4j
@RestController
@Transactional(readOnly = true)
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminUserRepository userRepository;

    @Resource
    private AdminRoleRepository roleRepository;

    @Resource
    private SecurityPwdEncode securityPwdEncode;

    @PreAuthorize("hasAuthority('admin:select')")
    @GetMapping
    public String select(String username, @ModelAttribute PagingModel pageModel) {
        Specification<AdminUser> specification = (Specification<AdminUser>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(username)) {
                list.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(userRepository.findAll(specification, pageModel.make()));
    }

    @PreAuthorize("hasAuthority('admin:select')")
    @GetMapping("/allRole")
    public String roles() {
        Iterable<AdminRole> all = roleRepository.findAll();
        Map<String, Long> allRole = new HashMap<>();
        all.forEach(it -> allRole.put(it.getRolename(), it.getId()));
        return ResultUtil.success(allRole);
    }

    @PreAuthorize("hasAuthority('admin:insert')")
    @Transactional
    @PutMapping
    public String insert(@ModelAttribute AdminUser adminUser, Long roleId) {
        adminUser.setRole(roleRepository.findById(roleId).get());
        adminUser.setPassword(securityPwdEncode.encode(adminUser.getPassword()));
        userRepository.save(adminUser);
        return ResultUtil.success(adminUser);
    }

    @PreAuthorize("hasAuthority('admin:delete')")
    @Transactional
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    @PostMapping
    public String update(@ModelAttribute AdminUser adminUser, Long roleId) {
        AdminUser old = userRepository.findById(adminUser.getId()).get();
        old.setEmail(adminUser.getEmail());
        old.setPhone(adminUser.getPhone());
        old.setRole(roleRepository.findById(roleId).get());
        userRepository.save(old);
        return ResultUtil.success(old);
    }

    /**
     * 重置用户密码
     * @return 随机新密码
     */
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    @PostMapping("/{id}")
    public String updatePwd(@PathVariable Long id) {
        AdminUser adminUser = userRepository.findById(id).get();
        String randomString = StringUtil.getRandomString(10);
        adminUser.setPassword(securityPwdEncode.encode(randomString));
        userRepository.save(adminUser);
        return ResultUtil.success(randomString);
    }

    /**
     * 更改用户的可用状态
     * @param id 用户的id
     * @return 用户信息
     */
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    @PostMapping("/changeEnable/{id}")
    public String changeEnable(@PathVariable Long id) {
        AdminUser user = userRepository.findById(id).get();
        user.setIsEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResultUtil.success(user);
    }

}
