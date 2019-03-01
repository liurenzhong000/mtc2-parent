package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.loan.LoanRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 借款记录's dao
 *
 * @author Chinhin
 * 2018/7/27
 */
public interface LoanRecordRepository extends PagingAndSortingRepository<LoanRecord, Long>{

    Page<LoanRecord> findAllByUser(User user, Pageable pageable);

    @SuppressWarnings("NullableProblems")
    Page<LoanRecord> findAll(Specification<LoanRecord> spec, Pageable pageable);

}
