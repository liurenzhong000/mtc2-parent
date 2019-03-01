package io.mtc.service.currency.repository;

import io.mtc.service.currency.entity.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/6/15
 */
public interface CurrencyRepository extends PagingAndSortingRepository<Currency, Long>,
        JpaSpecificationExecutor<Currency> {

    @SuppressWarnings("NullableProblems")
    Page<Currency> findAll(Specification<Currency> spec, Pageable pageable);

    List<Currency> findAllByIsEnabled(Boolean isEnabled, Sort sort);

    List<Currency> findAllBySourceTypeNot(Integer sourceType);

    Currency findByAddress(String address);

    List<Currency> findAllByRedPacketEnabled(Boolean redPacketEnabled);

    List<Currency> findAllByHostEnabled(Boolean hostEnable);
}
