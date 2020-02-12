package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.GuestRedEnvelope;
import io.mtc.facade.user.entity.RedEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

/**
 * 收到的红包DAO
 *
 * @author Chinhin
 * 2018/8/1
 */
public interface GuestRedEnvelopeRepository extends PagingAndSortingRepository<GuestRedEnvelope, Long> {

    List<GuestRedEnvelope> findAllByDeviceIdAndCreateTimeGreaterThanEqual(String deviceId, Date expireTimeLimit);

    Page<GuestRedEnvelope> findAllByDeviceIdOrderByCreateTimeDesc(String deviceId, Pageable pageable);

    void deleteAllByRedEnvelope(RedEnvelope redEnvelope);

    void deleteAllByDeviceId(String deviceId);
}
