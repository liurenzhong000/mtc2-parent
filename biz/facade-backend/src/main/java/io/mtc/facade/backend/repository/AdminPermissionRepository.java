package io.mtc.facade.backend.repository;

import io.mtc.facade.backend.entity.AdminPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 管理员权限dao
 *
 * @author Chinhin
 * 2018/6/11
 */
public interface AdminPermissionRepository extends PagingAndSortingRepository<AdminPermission, Long>,
        JpaSpecificationExecutor<AdminPermission> {

    @SuppressWarnings("NullableProblems")
    Page<AdminPermission> findAll(Specification<AdminPermission> spec, Pageable pageable);

}