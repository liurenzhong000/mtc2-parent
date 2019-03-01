package io.mtc.service.notification.repository;

import io.mtc.service.notification.entity.NotificationTemplet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 推送模板DAO
 *
 * @author Chinhin
 * 2018/7/16
 */
public interface NotificationTempletRepository extends PagingAndSortingRepository<NotificationTemplet, Long>,
        JpaSpecificationExecutor<NotificationTemplet> {

    @SuppressWarnings("NullableProblems")
    Page<NotificationTemplet> findAll(Specification<NotificationTemplet> spec, Pageable pageable);

}
