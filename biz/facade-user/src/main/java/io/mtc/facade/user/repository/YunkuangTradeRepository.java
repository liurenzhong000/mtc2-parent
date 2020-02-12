package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.yukuang.YukuangTrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 云矿交易记录's dao
 *
 * @author Chinhin
 * 2018/7/27
 */
public interface YunkuangTradeRepository extends PagingAndSortingRepository<YukuangTrade, Long>{

    @SuppressWarnings("NullableProblems")
    Page<YukuangTrade> findAll(Specification<YukuangTrade> spec, Pageable pageable);

}
