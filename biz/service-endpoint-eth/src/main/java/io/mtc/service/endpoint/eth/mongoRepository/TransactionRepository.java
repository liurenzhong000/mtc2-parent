package io.mtc.service.endpoint.eth.mongoRepository;

import io.mtc.common.mongo.dto.TransactionRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 交易记录Dao
 *
 * @author Chinhin
 * 2018/6/25
 */
public interface TransactionRepository extends MongoRepository<TransactionRecord, String> {

}
