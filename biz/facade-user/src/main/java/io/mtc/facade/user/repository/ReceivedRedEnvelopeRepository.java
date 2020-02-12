package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.ReceivedRedEnvelope;
import io.mtc.facade.user.entity.RedEnvelope;
import io.mtc.facade.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * 收到的红包DAO
 *
 * @author Chinhin
 * 2018/8/1
 */
public interface ReceivedRedEnvelopeRepository extends PagingAndSortingRepository<ReceivedRedEnvelope, Long> {

    ReceivedRedEnvelope findByUser(User user);

    ReceivedRedEnvelope findByUserAndRedEnvelope(User user, RedEnvelope redEnvelope);

    Page<ReceivedRedEnvelope> findAllByUserOrderByCreateTimeDesc(User user, Pageable pageable);

    List<ReceivedRedEnvelope> findAllByRedEnvelopeAndAmountNot(
            RedEnvelope redEnvelope, BigInteger notAmount, Sort sort);
}
