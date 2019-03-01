package io.mtc.service.currency.repository;

import io.mtc.service.currency.entity.CreateCurrency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Set;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/6/15
 */
public interface CreateCurrencyRepository extends PagingAndSortingRepository<CreateCurrency, Long>,
        JpaSpecificationExecutor<CreateCurrency> {

    @SuppressWarnings("NullableProblems")
    Page<CreateCurrency> findAll(Specification<CreateCurrency> spec, Pageable pageable);

    CreateCurrency findTopByStatusOrderByIdAsc(Integer status);

    Set<CreateCurrency> findAllByStatusAndExpireTimeGreaterThan(Integer status, long processExpireTime);

    CreateCurrency findTopByStatusAndExpireTimeLessThanOrderByIdAsc(Integer status, long processExpireTime);

    /**
     * 更新过期的账单
     */
    @Modifying
    @Query(value = "update CreateCurrency set status = :targetStatus where status = :beforeStatus and txNonce <= :nonce")
    void setExpireBillStatus(@Param("targetStatus") Integer targetStatus, @Param("beforeStatus") Integer beforeStatus,
                             @Param("nonce") BigInteger nonce);

}
