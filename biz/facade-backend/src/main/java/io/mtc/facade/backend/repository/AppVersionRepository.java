package io.mtc.facade.backend.repository;

import io.mtc.facade.backend.entity.AppVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/7/5
 */
public interface AppVersionRepository extends PagingAndSortingRepository<AppVersion, Long>, JpaSpecificationExecutor<AppVersion> {

    @SuppressWarnings("NullableProblems")
    Page<AppVersion> findAll(Specification<AppVersion> spec, Pageable pageable);

    AppVersion findByIsActiveAndIsAndroid(Boolean isActive, Boolean isAndroid);

    /**
     * 更新某个平台的版本为无效
     * @param isAndroid true：安卓端，false：苹果端
     */
    @Modifying
    @Query(value = "update AppVersion set isActive = false where isAndroid = :isAndroid")
    void setAllNotActiveWithPlatform(@Param("isAndroid") Boolean isAndroid);
}
