package io.mtc.facade.backend.repository;

import io.mtc.facade.backend.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 管理员Dao
 *
 * @author Chinhin
 * 2018/6/11
 */
public interface AdminUserRepository extends PagingAndSortingRepository<AdminUser, Long>, JpaSpecificationExecutor<AdminUser> {

    public AdminUser findByUsername(String username);

    @SuppressWarnings("NullableProblems")
    Page<AdminUser> findAll(Specification<AdminUser> spec, Pageable pageable);

}
