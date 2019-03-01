package io.mtc.facade.backend.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.entity.AdminPermission;
import io.mtc.facade.backend.repository.AdminPermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 权限控制类
 *
 * @author Chinhin
 * 2018/6/13
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@Transactional(readOnly = true)
public class PermissionController {

    @Resource
    private AdminPermissionRepository adminPermissionRepository;

    @PreAuthorize("hasAuthority('permission:select')")
    @GetMapping
    public String permissions(String name, String permission, @ModelAttribute PagingModel pageModel) {

        Specification<AdminPermission> specification = (Specification<AdminPermission>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(name)) {
                list.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtil.isNotBlank(permission)) {
                list.add(criteriaBuilder.like(root.get("permission"), "%" + permission + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        Page<AdminPermission> all = adminPermissionRepository.findAll(specification, pageModel.make());
        return PagingResultUtil.list(all);
    }

    @PreAuthorize("hasAuthority('permission:insert')")
    @PutMapping
    @Transactional
    public String insert(String name, String permission) {
        AdminPermission temp = new AdminPermission(name, permission);
        adminPermissionRepository.save(temp);
        return ResultUtil.success(temp);
    }

    @PreAuthorize("hasAuthority('permission:delete')")
    @Transactional
    @DeleteMapping("/{id}")
    public String del(@PathVariable Long id) {
        adminPermissionRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @PreAuthorize("hasAuthority('permission:update')")
    @Transactional
    @PostMapping
    public String update(Long id, String name, String permission) {
        Optional<AdminPermission> optional = adminPermissionRepository.findById(id);
        AdminPermission adminPermission = optional.get();
        adminPermission.setName(name);
        adminPermission.setPermission(permission);
        adminPermissionRepository.save(adminPermission);
        return ResultUtil.success(adminPermission);
    }

}
