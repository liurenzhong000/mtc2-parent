package io.mtc.service.trans.eth.mongoRepository;

import io.mtc.common.mongo.dto.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * 交易记录Dao
 *
 * @author Chinhin
 * 2018/6/25
 */
public interface TransactionRepository extends MongoRepository<TransactionRecord, String> {

    @Deprecated
    TransactionRecord findByHashAndIsMadeByScheduleAndStatus(String hash, boolean isMadeBySchedule, int status);

    Page<TransactionRecord> findAllByFromEqualsOrToEqualsAndContractAddressEquals(String from, String to, String contract, Pageable pageable);

    Page<TransactionRecord> findAllByFromEqualsOrToEquals(String from, String to, Pageable pageable);

    List<TransactionRecord> findAllByTimesBetweenAndStatus(long start, long end, int status);

    TransactionRecord findByFromAndNonceAndStatus(String from, BigInteger nonce, Integer status);

}
