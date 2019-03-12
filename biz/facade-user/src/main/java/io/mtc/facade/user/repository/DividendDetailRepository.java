package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.dividend.DividendDetail;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 13:56
 * @Description:
 */
public interface DividendDetailRepository extends PagingAndSortingRepository<DividendDetail, Long> {

}
