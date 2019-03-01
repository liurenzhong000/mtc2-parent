package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.RedEnvelope;
import io.mtc.facade.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

/**
 * 红包dao
 *
 * @author Chinhin
 * 2018/8/1
 */
public interface RedEnvelopeRepository extends PagingAndSortingRepository<RedEnvelope, Long> {

    Page<RedEnvelope> findAllByUserOrderByCreateTimeDesc(User user, Pageable pageable);

    List<RedEnvelope> findAllByStatusAndCreateTimeBefore(int status, Date before);

}
