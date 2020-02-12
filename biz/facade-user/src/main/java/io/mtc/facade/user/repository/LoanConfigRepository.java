package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.loan.LoanConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 借款配置的DAO
 *
 * @author Chinhin
 * 2018/10/10
 */
public interface LoanConfigRepository extends JpaRepository<LoanConfig, Long> {
}
