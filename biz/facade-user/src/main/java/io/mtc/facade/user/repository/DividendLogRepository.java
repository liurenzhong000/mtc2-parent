package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.dividend.DividendLog;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 13:55
 * @Description:
 */
public interface DividendLogRepository extends PagingAndSortingRepository<DividendLog, Long> {

}
