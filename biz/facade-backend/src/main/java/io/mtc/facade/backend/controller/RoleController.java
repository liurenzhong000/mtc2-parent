package io.mtc.facade.backend.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.entity.AdminPermission;
import io.mtc.facade.backend.entity.AdminRole;
import io.mtc.facade.backend.repository.AdminPermissionRepository;
import io.mtc.facade.backend.repository.AdminRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 * 角色控制器
 *
 * @author Chinhin
 * 2018/6/14
 */
@Slf4j
@RestController
@Transactional(readOnly = true)
@RequestMapping("/role")
public class RoleController {

    @Resource
    private AdminRoleRepository roleRepository;

    @Resource
    private AdminPermissionRepository permissionRepository;

    @PreAuthorize("hasAuthority('role:select')")
    @GetMapping
    public String select(String rolename, @ModelAttribute PagingModel pageModel) {
        Specification<AdminRole> specification = (Specification<AdminRole>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(rolename)) {
                list.add(criteriaBuilder.like(root.get("rolename"), "%" + rolename + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        Page<AdminRole> all = roleRepository.findAll(specification, pageModel.make());
        return PagingResultUtil.list(all);
    }

    @PreAuthorize("hasAuthority('role:insert')")
    @GetMapping("/allPermission")
    public String allPermission() {
        List<Map<String, Object>> allPermission = new ArrayList<>();
        permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .forEach(it -> {
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("name", it.getName());
                    temp.put("id", it.getId());
                    allPermission.add(temp);
                });
        return ResultUtil.success(allPermission);
    }

    @PreAuthorize("hasAuthority('role:insert')")
    @PutMapping
    @Transactional
    public String insert(String rolename, @RequestParam(value = "permissionIds[]") long[] permissionIds) {
        AdminRole role = new AdminRole();
        role.setRolename(rolename);
        Set<AdminPermission> permissions = new HashSet<>();
        for (long tempId : permissionIds) {
            permissions.add(permissionRepository.findById(tempId).get());
        }
        role.setPermissions(permissions);
        roleRepository.save(role);
        return ResultUtil.success(role);
    }

    @PreAuthorize("hasAuthority('role:update')")
    @PostMapping
    @Transactional
    public String update(Long id, String rolename, @RequestParam(value = "permissionIds[]") long[] permissionIds) {
        AdminRole role = roleRepository.findById(id).get();
        role.setRolename(rolename);
        Set<AdminPermission> permissions = new HashSet<>();
        for (long tempId : permissionIds) {
            permissions.add(permissionRepository.findById(tempId).get());
        }
        role.setPermissions(permissions);
        roleRepository.save(role);
        return ResultUtil.success(role);
    }

    @PreAuthorize("hasAuthority('role:delete')")
    @Transactional
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

}
