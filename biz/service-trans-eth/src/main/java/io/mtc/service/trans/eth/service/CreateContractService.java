package io.mtc.service.trans.eth.service;

import io.mtc.common.dto.EthTransObj;
import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.util.EthRedisUtil;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.annotation.Resource;

/**
 * 发币的业务处理
 *
 * @author Chinhin
 * 2018/8/16
 */
@Service
public class CreateContractService {

    @Resource(name = "ethTransPendingProducer")
    private Producer producer;

    @Resource
    private EthRedisUtil ethRedisUtil;

    void createContractHandler(TransactionReceipt receipt) {

        String status = receipt.getStatus();

        EthTransObj request = new EthTransObj();
        request.setTxType(3);
        request.setTxId(ethRedisUtil.getCreateContractId());
        request.setCurrencyAddress(receipt.getContractAddress());
        // 成功
        if ("0x1".equals(status)) {
            request.setStatus(1);
        } else { // 失败
            request.setStatus(2);
        }
        producer.send(Constants.Topic.MTC_BIZ_TRANS,
                Constants.Tag.ETH_BIZ_CREATION_COMPLETE,
                request,
                Constants.Tag.ETH_BIZ_CREATION_COMPLETE.name() + ":" + request.getTxId());
    }

}
