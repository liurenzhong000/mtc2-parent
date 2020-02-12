package io.mtc.service.trans.eth.service;

import com.mongodb.client.result.DeleteResult;
import io.mtc.common.mongo.dto.TransactionRecord;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 清理mongodb数据的service
 *
 * @author Chinhin
 * 2018/6/29
 */
@Slf4j
@Service
public class CleanMongoService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 清楚指定时间之前的记录
     * @param lowerLimitTimes 指定时间毫秒timestamp
     */
    public void cleanBeforeTimes(long lowerLimitTimes) {
        // 将平台的钱包地址对应的交易记录都变成平台交易记录
        updatePlatformRecord();

        Query query = new Query();
        // 记录时间早于下限时间
        query.addCriteria(Criteria.where("times").lt(lowerLimitTimes));
        // 不是平台用户的交易记录
        query.addCriteria(Criteria.where("isPlatformUser").is(false));
        // 由定时任务创建
        query.addCriteria(Criteria.where("isMadeBySchedule").is(true));

        DeleteResult remove = mongoTemplate.remove(query, TransactionRecord.class);
        log.info("清理完成, 删除[{}]条记录" , remove.getDeletedCount());
    }

    private void updatePlatformRecord() {

        Set<String> platformUserKeys = redisUtil.getKeysBeginWith(RedisKeys.PLATFORM_USER_PREFIX);
        // 所有钱包地址集合
        Set<String> addresses = new HashSet<>();
        for (String temp : platformUserKeys) {
            addresses.add(temp.substring(14));
        }

        // 更新该钱包地址的交易记录为平台用户交易记录
        Query query = new Query();
        Criteria tempC = new Criteria().orOperator(
                Criteria.where("from").in(addresses),
                Criteria.where("to").in(addresses)
        );
        query.addCriteria(tempC);
        Update update = Update.update("isPlatformUser", true);
        mongoTemplate.updateMulti(query, update, TransactionRecord.class);
    }

}
