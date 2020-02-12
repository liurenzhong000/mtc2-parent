package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.loan.LoanBonus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * LoanBonus's dao
 *
 * @author Chinhin
 * 2018/10/16
 */
public interface LoanBonusRepository extends PagingAndSortingRepository<LoanBonus, Long> {

    Page<LoanBonus> findAllByUser(User user, Pageable pageable);
    @SuppressWarnings("NullableProblems")
    Page<LoanBonus> findAll(Specification<LoanBonus> spec, Pageable pageable);

}
