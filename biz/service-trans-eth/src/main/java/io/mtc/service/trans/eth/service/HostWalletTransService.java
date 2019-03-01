package io.mtc.service.trans.eth.service;

import io.mtc.common.dto.EthHostWalletAddressTrans;
import io.mtc.common.dto.TransactionStatusEnum;
import io.mtc.common.mongo.dto.TransactionRecord;
import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.util.EthRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * 托管用户的钱包有交易
 *
 * @author Chinhin
 * 2019-01-24
 */
@Slf4j
@Service
public class HostWalletTransService {

    @Resource(name = "ethHostWalletTransProducer")
    private Producer producer;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private ApplicationContext applicationContext;

    void filter(TransactionRecord record) {
        // from 和 to 都不是托管用户的钱包地址
        if (ethRedisUtil.isUserHostWalletAddress(record.getFrom()) == 0
                && ethRedisUtil.isUserHostWalletAddress(record.getTo()) == 0) {
            return;
        }
        if (record.getStatus() != TransactionStatusEnum.Success.getValue()) {
            return;
        }

        EthHostWalletAddressTrans info = new EthHostWalletAddressTrans();
        info.setFromAddress(record.getFrom());
        info.setWalletAddress(record.getTo());
        info.setTokenAddress(record.getContractAddress());
        // 以太坊的交易
        if (io.mtc.common.constants.Constants.ETH_ADDRESS.equals(record.getContractAddress())) {
            info.setIncome(new BigInteger(record.getValue()));
        } else {
            info.setIncome(new BigInteger(record.getTokenCounts()));
        }
        info.setTxHash(record.getHash());
        info.setNonce(record.getNonce());

        Constants.Tag tag;
        if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
            tag = Constants.Tag.ETH_BIZ_HOST_WALLET_TRANS_PROD;
        } else {
            tag = Constants.Tag.ETH_BIZ_HOST_WALLET_TRANS;
        }

        producer.send(Constants.Topic.MTC_BIZ_TRANS, tag, info, tag.name() + ":" + info.getTxHash());
    }

}
