package io.mtc.service.trans.eth.service;

import io.mtc.common.constants.Constants;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.dto.TransInfo;
import io.mtc.common.dto.TransactionStatusEnum;
import io.mtc.common.mongo.dto.TransactionRecord;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.common.web3j.util.TransactionUtil;
import io.mtc.service.trans.eth.feign.ServiceEndPointEth;
import io.mtc.service.trans.eth.mongoRepository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * service
 *
 * @author Chinhin
 * 2018/6/26
 */
@Slf4j
@Service
public class TransactionService {

    @Resource
    private ServiceEndPointEth serviceEndPointEth;

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private CreateContractService createContractService;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private HostWalletTransService hostWalletTransService;

    @Value("${endpointUrl}")
    private String endpointUrl;

    // 要扫最多多少秒前的区块
    @Value("${scanMaxMinutesBeforeNow}")
    private long scanMaxMinutesBeforeNow;

    @Resource(name = "ethTransPendingProducer")
    private Producer producer;

    /**
     * 扫描该区块所有交易记录
     * @param blockNum 区块号
     */
    public void scan(int blockNum) {
        log.info("扫描区块 {}", blockNum);

//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNum), true).send();
        EthBlock block = serviceEndPointEth.getBlock(blockNum);

        // 这次获取的节点还没有最新高度情况下，会获取不到区块
        if (block == null
                || block.getBlock() == null
                || block.getBlock().getTransactions() == null) {
            jumpHandler("block 为空，等待下次轮询");
        }

        // 区块的秒timestamp
//        long createBlockTimes = block.getBlock().getTimestamp().longValue();
        long createBlockTimes = TransactionUtil.hexCheck(block.getBlock().getTimestampRaw()).longValue(); // rawOnly
        // 当前的秒timestamp
        long nowTimes = System.currentTimeMillis() / 1000;
        if (createBlockTimes < (nowTimes - scanMaxMinutesBeforeNow * 60)) {
            ethRedisUtil.set(RedisKeys.SCAN_LOWER_LIMIT, blockNum);
            log.info("已经达到设置的需要扫描的最小时间的区块[{}], 区块时间: {}，最低要求时间: {}", blockNum,
                    DateUtil.formatStandardDate(createBlockTimes * 1000),
                    DateUtil.formatStandardDate((nowTimes - scanMaxMinutesBeforeNow * 60) * 1000)
            );
            throw new IllegalMonitorStateException("");
        }

        // 获取属于平台的币种集合
        Map<String, String> platformCurrencyCollection = ethRedisUtil.platformCurrencyCollection();
        if (platformCurrencyCollection == null) {
            throw new IllegalMonitorStateException("平台币种集合获取失败，等待下次轮询");
        }
        for (EthBlock.TransactionResult it : block.getBlock().getTransactions()) {
            Transaction transaction = ((EthBlock.TransactionObject) it.get()).get();

            // 平台用户发出的交易，会扣除以太币
            if (ethRedisUtil.isPlatformUser(transaction.getFrom())) {
                String fromBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(transaction.getFrom(), Constants.ETH_ADDRESS);
                needRefreshBalance(fromBalanceKey, blockNum);
            }
            String processingTxHash = ethRedisUtil.getCreateContractTxHash();
            // 是正在交易中的发币交易
            if (transaction.getHash().equals(processingTxHash)) {
                // 创建代币交易的check
                createContractService.createContractHandler(getTxReceipt(transaction.getHash()));
                continue;
            }
            // 如果不是平台有的币种交易
            if (!TransactionUtil.ifPlatformCurrencyTrans(transaction, platformCurrencyCollection)) {
                continue;
            }
//            EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transaction.getHash()).send();
//            Optional<TransactionReceipt> transactionReceiptOptional = receipt.getTransactionReceipt();
//            TransactionReceipt transactionReceipt= transactionReceiptOptional.orElse(null);

            analysis(transaction, getTxReceipt(transaction.getHash()), block, platformCurrencyCollection);
        }
    }

    private TransactionReceipt getTxReceipt(String txHash) {
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = serviceEndPointEth.getReceipt(txHash);
        } catch (Exception e) {
            jumpHandler("receipt为空，等待下次轮询");
        }
        if (transactionReceipt == null) {
            jumpHandler("receipt为空，等待下次轮询");
        }
        return transactionReceipt;
    }

    private void jumpHandler(String info) {
        try {
            // 休眠4秒，便于下个worker跳过此block请求上一个
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new IllegalMonitorStateException(info);
    }

    public boolean txCheck(String txHash) {
        TransactionRecord thisTx = transactionRepository.findById(txHash).get();
        TransactionRecord sameNonceTrans = transactionRepository.findByFromAndNonceAndStatus(
                thisTx.getFrom(), thisTx.getNonce(), TransactionStatusEnum.Success.getValue());
        // 有相同nonce成功的交易
        if (sameNonceTrans != null) {
            txFailureHandler(thisTx);
            return true;
        } else {
            return reloadTx(txHash);
        }
    }

    /**
     * 重新加载某个交易信息
     * @param txHash 交易hash
     * @return false表示失败，true表示成功
     */
    private boolean reloadTx(String txHash) {
        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
        try {
            Transaction transaction = web3j.ethGetTransactionByHash(txHash).send().getTransaction().orElse(null);
            if (transaction == null) {
                return false;
            }
            TransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transaction.getHash()).send().getTransactionReceipt().orElse(null);
            EthBlock ethBlock = web3j.ethGetBlockByHash(transaction.getBlockHash(), false).send();
            if (transactionReceipt == null || ethBlock == null) {
                return false;
            }
            // 获取属于平台的币种集合
            analysis(transaction, transactionReceipt, ethBlock, ethRedisUtil.platformCurrencyCollection());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 分析交易, 存入数据库
     * @param transaction 交易实例
     * @param receipt 回执实例
     * @param block 区块实例
     * @param platformCurrencyCollection 币种（地址：单位）集合
     */
    private void analysis(Transaction transaction, TransactionReceipt receipt, EthBlock block, Map<String, String> platformCurrencyCollection) {

        if (receipt == null) {
            throw new IllegalMonitorStateException("2 receipt为空，等待下次轮询");
        }

        // 是否本平台的未被确认的交易记录
        TransactionRecord record = transactionRepository.findById(transaction.getHash()).orElse(null);
        if (record == null) {
            // 写入mongoDB
            record = TransactionUtil.makeRecordByTransaction(transaction);
            // 不是交易的transaction
            if (record.getHash() == null) {
                int blockNum = TransactionUtil.hexCheck(receipt.getBlockNumberRaw()).intValue();
                String fromBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getFrom(), record.getContractAddress());
                needRefreshBalance(fromBalanceKey, blockNum);
                analysisInput(record.getInput(), record.getContractAddress(), blockNum);
                return;
            }
            record.setTimes(TransactionUtil.hexCheck(block.getBlock().getTimestampRaw()).longValue() * 1000); // rawOnly
//            record.setTimes(block.getBlock().getTimestamp().longValue() * 1000);
            record.setType(1);
            record.setRemark(Constants.EMPTY);
            record.setIsMadeBySchedule(true);
        }

//        record.setMeshGas(receipt.getGasUsed());
//        record.setBlockNumber(receipt.getBlockNumber());
//        record.setCumulativeGasUsed(receipt.getCumulativeGasUsed());
        record.setMeshGas(TransactionUtil.hexCheck(receipt.getGasUsedRaw())); // rawOnly
        record.setBlockNumber(TransactionUtil.hexCheck(receipt.getBlockNumberRaw())); // rawOnly
        record.setCumulativeGasUsed(TransactionUtil.hexCheck(receipt.getCumulativeGasUsedRaw())); // rawOnly

        record.setBlockHash(receipt.getBlockHash());

        record.setInput(transaction.getInput());
//        record.setTransactionIndex(transaction.getTransactionIndex());
//        record.setGasPrice(transaction.getGasPrice());
        record.setTransactionIndex(TransactionUtil.hexCheck(transaction.getTransactionIndexRaw())); // rawOnly
        record.setGas(TransactionUtil.hexCheck(transaction.getGasRaw())); // rawOnly

        // 总共花费的gas以太币
//        BigInteger gasCost = receipt.getGasUsed().multiply(record.getGasPrice());
        BigInteger gasCost = TransactionUtil.hexCheck(receipt.getGasUsedRaw()).multiply(record.getGasPrice()); // rawOnly
        record.setActualCostFee(gasCost);
        record.setShortName(platformCurrencyCollection.get(record.getContractAddress()));
        // 合约交易
        if (record.isContract()) {
            String status = receipt.getStatus();
            // 成功
            if ("0x1".equals(status)) {
                if (receipt.getLogs() == null || receipt.getLogs().size() == 0) {
                    record.setStatus(TransactionStatusEnum.Failure.getValue());
                } else {
                    record.setStatus(TransactionStatusEnum.Success.getValue());
                }
            } else {
                record.setStatus(TransactionStatusEnum.Failure.getValue());
            }
        // eth交易
        } else {
            record.setStatus(TransactionStatusEnum.Success.getValue());
        }

        // 平台业务的处理
        bizTxHandler(record);
        // 平台用户的交易
        if (ethRedisUtil.isPlatformUser(record.getFrom())
                || ethRedisUtil.isPlatformUser(record.getTo())) {
            record.setIsPlatformUser(true);
            // 发送通知
            makeNotificationInfo(record);
        } else {
            record.setIsPlatformUser(false);
        }
        // 检查是否要刷新余额缓存
//        checkBalanceChange(block.getBlock().getNumber().intValue(), record);
        checkBalanceChange(TransactionUtil.hexCheck(block.getBlock().getNumberRaw()).intValue(), record); // rawOnly
        transactionRepository.save(record);

        // 是否是托管账户钱包地址的交易
        hostWalletTransService.filter(record);
    }

    /**
     * 判断该区块是否改变了平台用户钱包地址的余额
     * @param blockNum 区块号
     * @param record 交易记录
     */
    private void checkBalanceChange(int blockNum, TransactionRecord record) {
        // 合约交易
        if (record.isContract()) {
            // 支付方 合约钱包地址缓存 key
            String fromBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getFrom(), record.getContractAddress());
            needRefreshBalance(fromBalanceKey, blockNum);

            // 支付方 ether钱包地址缓存 key 转账的手续费
            String fromEthBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getFrom(), Constants.ETH_ADDRESS);
            needRefreshBalance(fromEthBalanceKey, blockNum);

            // 收入方 合约钱包地址缓存 key
            String toBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getTo(), record.getContractAddress());
            needRefreshBalance(toBalanceKey, blockNum);
        } else { // eth交易
            // 支出方 ether钱包地址缓存 key
            String fromBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getFrom(), Constants.ETH_ADDRESS);
            needRefreshBalance(fromBalanceKey, blockNum);

            // 收入方 ether钱包地址缓存 key
            String toBalanceKey = RedisKeys.ETH_CONTRACT_BALANCE(record.getTo(), Constants.ETH_ADDRESS);
            needRefreshBalance(toBalanceKey, blockNum);
        }
    }

    /**
     * 判断当前区块高度是否 大于等于 放入缓存时候的区块高度，如果是则删除缓存
     * @param balanceKey 钱包缓存key
     * @param blockNum 当前区块高度
     */
    private void needRefreshBalance(String balanceKey, int blockNum) {
        Object balanceObj = ethRedisUtil.get(balanceKey);
        // 在平台上没有余额缓存
        if (balanceObj == null) {
            return;
        }
        // 放缓存时的区块高度
        int setBalanceBlock = (int) ethRedisUtil.get(RedisKeys.SET_BALANCE_BLOCK(balanceKey));
        // 当前高度 大于放缓存时的高度
        if (blockNum >= setBalanceBlock) {
            ethRedisUtil.delete(balanceKey);
            ethRedisUtil.delete(RedisKeys.SET_BALANCE_BLOCK(balanceKey));
            ethRedisUtil.set(RedisKeys.CACHE_BALANCE_THRESHOLD(balanceKey), blockNum);
        }
    }

    /**
     * 将平台用户的交易信息存入缓存，等待推送
     * @param record 交易记录
     */
    private void makeNotificationInfo(TransactionRecord record) {
        // 10分钟前的不用推送
        if (CommonUtil.ignorePush(record.getTimes())) {
            return;
        }
        TransInfo transInfo = new TransInfo();
        transInfo.setTimes(record.getTimes());
        transInfo.setTxHash(record.getHash());
        // 合约交易
        if (record.isContract()) {
            transInfo.setAmount(record.getTokenCounts());
        } else {
            transInfo.setAmount(record.getValue());
        }
        transInfo.setFrom(record.getFrom());
        transInfo.setTo(record.getTo());
        transInfo.setShotName(record.getShortName());
        transInfo.setIsSuccess(record.getStatus() == 1);

        producer.send(io.mtc.common.mq.aliyun.Constants.Topic.MTC_BIZ_TRANS,
                io.mtc.common.mq.aliyun.Constants.Tag.ETH_TRANS_NOTIFI,
                transInfo,
                transInfo.getTxHash());
    }

    /**
     * 重试获取在此时间段的未确认交易记录，并进行刷新处理
     * @param start 开始时间毫秒
     * @param end 结束时间毫秒
     */
    public void retryTxBetweenTimes(long start, long end) {
        // 获取时间段的交易记录
        List<TransactionRecord> certainTimesTx = transactionRepository.findAllByTimesBetweenAndStatus(
                start, end, TransactionStatusEnum.UnConfirmed.getValue());

        List<TransactionRecord> willReloadTrans = new ArrayList<>();
        // 发现有相同nonce的交易，则直接置为失败
        for (TransactionRecord temp : certainTimesTx) {
            TransactionRecord sameNonceTrans = transactionRepository.findByFromAndNonceAndStatus(
                    temp.getFrom(), temp.getNonce(), TransactionStatusEnum.Success.getValue());
            // 有相同nonce成功的交易
            if (sameNonceTrans != null) {
                txFailureHandler(temp);
            } else {
                willReloadTrans.add(temp);
            }
        }
        log.info("将重试刷新{}至{}时间段的{}条交易记录", DateUtil.formatStandardDate(start), DateUtil.formatStandardDate(end), certainTimesTx.size());
        for (TransactionRecord temp : willReloadTrans) {
            reloadTx(temp.getHash());
        }
    }

    /**
     * 分析input，针对非交易的input
     * @param input 非交易的transaction的input
     * @param contractAddress 合约地址
     * @param blockNum 当前交易的区块高度
     */
    private void analysisInput(String input, String contractAddress, int blockNum) {
        List<String> strings = StringUtil.stringSpilt(input.substring(10), 64);
        for (String temp : strings) {
            if (temp.length() > 24) {
                String mayWalletAddress = temp.substring(24);
                // 收入方 ether钱包地址缓存 key
                String balanceKey = RedisKeys.ETH_CONTRACT_BALANCE(mayWalletAddress, contractAddress);
                needRefreshBalance(balanceKey, blockNum);
            }
        }
    }

    /**
     * 交易记录失败的处理
     * @param record 交易记录
     */
    private void txFailureHandler(TransactionRecord record) {
        record.setStatus(TransactionStatusEnum.Failure.getValue());
        transactionRepository.save(record);
        bizTxHandler(record);
    }


    /**
     * 业务交易的check
     * @param record 交易记录
     */
    private void bizTxHandler(TransactionRecord record) {
        // 平台业务交易
        if (record.getTxType() != null && record.getTxType() != 0) {
            EthTransObj request = new EthTransObj();
            request.setTxType(record.getTxType());
            request.setTxId(record.getTxId());
            if (record.getStatus() == TransactionStatusEnum.Success.getValue()) {
                request.setStatus(1);
            } else if (record.getStatus() == TransactionStatusEnum.Failure.getValue()) {
                request.setStatus(2);
            }
            if (record.isContract()) {
                request.setAmount(record.getTokenCounts());
            } else {
                request.setAmount(record.getValue());
            }
            producer.send(io.mtc.common.mq.aliyun.Constants.Topic.MTC_BIZ_TRANS,
                    io.mtc.common.mq.aliyun.Constants.Tag.ETH_BIZ_TRANS_COMPLETE,
                    request,
                    io.mtc.common.mq.aliyun.Constants.Tag.ETH_BIZ_TRANS_COMPLETE.name() + request.getTxId());
        }
    }

}
