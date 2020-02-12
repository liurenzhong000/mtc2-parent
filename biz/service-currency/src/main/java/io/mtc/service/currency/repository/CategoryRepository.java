package io.mtc.service.currency.repository;

import io.mtc.service.currency.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/6/15
 */
public interface CategoryRepository extends PagingAndSortingRepository<Category, Long>,
        JpaSpecificationExecutor<Category> {

    Category findByName(String name);

    @SuppressWarnings("NullableProblems")
    Page<Category> findAll(Specification<Category> spec, Pageable pageable);

}
