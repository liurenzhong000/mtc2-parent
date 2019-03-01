package io.mtc.service.notification.repository;

import io.mtc.service.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 通知dao
 *
 * @author Chinhin
 * 2018/7/10
 */
public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {

    @SuppressWarnings("NullableProblems")
    Page<Notification> findAll(Specification<Notification> spec, Pageable pageable);

    Notification findByTxHash(String txHash);

}
