package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.wheel.WheelRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Chinhin
 * 2018/12/13
 */
public interface WheelRecordRepository extends PagingAndSortingRepository<WheelRecord, Long>, JpaSpecificationExecutor<WheelRecord> {

    @SuppressWarnings("NullableProblems")
    Page<WheelRecord> findAll(Specification<WheelRecord> spec, Pageable pageable);

    Page<WheelRecord> findAllByUser(User user, Pageable pageable);

}
