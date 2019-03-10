package io.mtc.service.endpoint.eth.service;

import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.constants.Constants;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.dto.TransactionStatusEnum;
import io.mtc.common.mongo.dto.TransactionRecord;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.web3j.util.MeshTransactionData;
import io.mtc.service.endpoint.eth.mongoRepository.TransactionRepository;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易记录处理
 *
 * 对我们平台产生的交易，获取交易记录，并存入mongodb
 *
 * @author Chinhin
 * 2018/6/22
 */
@Slf4j
@Service
public class TransactionService implements MsgHandler {

    @Resource
    private Web3jPool web3jPool;

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private Producer producer;

    public void transactionHandler(MeshTransactionData transactionData, Integer type, BigInteger meshGas,
                                   String remark, Integer txType, Long txId) {
        // 写入mongoDB
        TransactionRecord record = new TransactionRecord();

        record.setHash(transactionData.txHash);
        record.setFrom(transactionData.fromAddress);
        record.setInput(transactionData.input);

        record.setContractAddress(transactionData.contractAddress);
        record.setTo(transactionData.toAddress);
        // 以太坊交易
        if (Constants.ETH_ADDRESS.equals(transactionData.contractAddress)) {
            record.setTokenCounts(Constants.EMPTY);
            record.setValue(transactionData.value.toString());
        } else {
            record.setTokenCounts(transactionData.value.toString());
            record.setValue(Constants.EMPTY);
        }

        record.setGasPrice(transactionData.gasPrice);
        record.setGas(transactionData.gasLimit);
        record.setNonce(transactionData.nonce);
        record.setCreateTime(System.currentTimeMillis());

        record.setTimes(System.currentTimeMillis());
        record.setStatus(TransactionStatusEnum.UnConfirmed.getValue());
        record.setType(type);
        record.setMeshGas(meshGas);
        record.setRemark(remark);
        record.setTxType(txType);
        record.setTxId(txId);
        record.setIsMadeBySchedule(false);
        // 获取属于平台的币种集合
        record.setShortName(ethRedisUtil.getCurrencyShortName(transactionData.contractAddress));
        transactionRepository.save(record);
    }

    /**
     * 判断交易是否已经存在
     * @param txHash 交易hash
     * @return 结果
     */
    public String checkIfTxExist(String txHash) {
        TransactionRecord transactionRecord = transactionRepository.findById(txHash).orElse(null);
        if (transactionRecord == null) {
            return null;
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("jsonrpc", "2.0");
            body.put("id", 1);
            body.put("result", txHash);
            return CommonUtil.toJson(body);
        }
    }

    @Override
    public Action doConsume(String json) {
        EthTransObj transInfo = CommonUtil.fromJson(json, EthTransObj.class);
        log.info("打包交易:{}", json);

        Web3j web3j = null;
        try {
            web3j = web3jPool.getConnection();
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(transInfo.getSignedTransactionData()).sendAsync().get();
            if (transInfo.getTxType() == EthTransObj.TxType.CREATE_CONTRACT.ordinal()) {//发币
                ethRedisUtil.setCreateContractInfo(ethSendTransaction.getTransactionHash(), transInfo.getTxId());
            } else {
                // 打包失败
                if (ethSendTransaction.getTransactionHash() == null) {
                    transFailure(transInfo.getTxType(), transInfo.getTxId());
                } else {
                    transactionHandler(MeshTransactionData.from(transInfo.getSignedTransactionData()),
                            1, null, Constants.EMPTY,
                            transInfo.getTxType(), transInfo.getTxId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (web3j != null) {
                web3jPool.close(web3j);
            }
        }
        return Action.CommitMessage;
    }

    /**
     * 打包失败的时候，通知其他业务进行业务失败的处理
     */
    private void transFailure(Integer txType, Long txId) {
            // 平台业务交易
            EthTransObj request = new EthTransObj();
            request.setTxType(txType);
            request.setTxId(txId);
            request.setStatus(EthTransObj.Status.FAIL.ordinal());
            producer.send(io.mtc.common.mq.aliyun.Constants.Topic.MTC_BIZ_TRANS,
                    io.mtc.common.mq.aliyun.Constants.Tag.ETH_BIZ_TRANS_COMPLETE,
                    request,
                    io.mtc.common.mq.aliyun.Constants.Tag.ETH_BIZ_TRANS_COMPLETE.name() + request.getTxId());
    }

}
