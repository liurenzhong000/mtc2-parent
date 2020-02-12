package io.mtc.facade.backend.repository;

import io.mtc.facade.backend.entity.CustomPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/6/20
 */
public interface CustomPageRepostitory extends PagingAndSortingRepository<CustomPage, Long>, JpaSpecificationExecutor<CustomPage> {

    @SuppressWarnings("NullableProblems")
    Page<CustomPage> findAll(Specification<CustomPage> spec, Pageable pageable);

    CustomPage findByLinkTag(String linkTag);

}
