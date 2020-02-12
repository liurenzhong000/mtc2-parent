package io.mtc.facade.backend.repository;

import io.mtc.facade.backend.entity.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 管理员角色Dao
 *
 * @author Chinhin
 * 2018/6/11
 */
public interface AdminRoleRepository extends PagingAndSortingRepository<AdminRole, Long>, JpaSpecificationExecutor<AdminRole> {

    @SuppressWarnings("NullableProblems")
    Page<AdminRole> findAll(Specification<AdminRole> spec, Pageable pageable);

}
