package io.mtc.facade.bitcoin.data.repository;

import io.mtc.facade.bitcoin.data.entity.BchTxRemark;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 交易的dao
 *
 * @author Chinhin
 * 2018/12/18
 */
public interface BchTxRemarkRepository extends MongoRepository<BchTxRemark, String> {

}
