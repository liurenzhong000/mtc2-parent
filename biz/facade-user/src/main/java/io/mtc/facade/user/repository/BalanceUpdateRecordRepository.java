package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.BalanceUpdateRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 管理员对余额改动记录
 *
 * @author Chinhin
 * 2018/12/13
 */
public interface BalanceUpdateRecordRepository extends PagingAndSortingRepository<BalanceUpdateRecord, Long> {
}
