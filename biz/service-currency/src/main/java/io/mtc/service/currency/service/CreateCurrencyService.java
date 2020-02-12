package io.mtc.service.currency.service;

import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.common.web3j.util.MeshTransactionData;
import io.mtc.service.currency.entity.CreateCurrency;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.feign.ServiceEndpointEth;
import io.mtc.service.currency.repository.CreateCurrencyRepository;
import io.mtc.service.currency.repository.CurrencyRepository;
import io.mtc.service.currency.util.PackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

/**
 * 发币service
 *
 * @author Chinhin
 * 2018/8/16
 */
@Slf4j
@Service
public class CreateCurrencyService implements MsgHandler {

    @Value("${create-contract-address}")
    private String createContractAddress;

    @Value("${create-contract-path}")
    private String createContractKeyStorePath;

    @Value("${create-contract-password}")
    private String createContractKeyStorePassword;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CreateCurrencyRepository createCurrencyRepository;

    @Resource
    private Producer producer;

    @Resource
    private CurrencyRepository currencyRepository;

    /**
     * 根据存在数据库里面的待创建的记录进行创建
     */
    public void doCreate() {
        CreateCurrency createCurrency = createCurrencyRepository.findTopByStatusOrderByIdAsc(1);
        if (createCurrency == null) {
            // 是否有还未过期的处理中的创建记录
            Set<CreateCurrency> pending = createCurrencyRepository.findAllByStatusAndExpireTimeGreaterThan(
                    2, System.currentTimeMillis());
            // 有正在提现的交易，则不做处理
            if (pending.size() > 0) {
                return;
            } else {
                CreateCurrency expireCreateCurrency = createCurrencyRepository.findTopByStatusAndExpireTimeLessThanOrderByIdAsc(
                        2, System.currentTimeMillis());
                if (expireCreateCurrency == null) {
                    return;
                } else {
                    createCurrency = expireCreateCurrency;
                }
            }
        }
        // 获取锁失败则直接返回
        if (!redisUtil.distributeLock(RedisKeys.PENDING_CREATE_CONTRACT_PROCESS, 20)) {
            return;
        }
        try {
            log.info("开始发币 {}", createCurrency.getId());
            createCurrency.setStatus(2);

            BigInteger[] gasPriceAndNonce = serviceEndpointEth.getGasPriceAndNonce(createContractAddress);
            String signedTransactionData = PackageUtil.packageCreation(gasPriceAndNonce, createCurrency,
                    createContractKeyStorePath, createContractKeyStorePassword);

            MeshTransactionData transactionData = MeshTransactionData.from(signedTransactionData);

            // 签名失败
            if (transactionData.txHash == null) {
                createCurrency.setExpireTime(System.currentTimeMillis());
                createCurrencyRepository.save(createCurrency);
                return;
            }

            createCurrency.setTxHash(transactionData.txHash);
            createCurrency.setTxNonce(transactionData.nonce);
            // 设置本次提现过期时间为10分钟后
            createCurrency.setExpireTime(System.currentTimeMillis() + 1000 * 60 * 10);
            // 打包请求
            EthTransObj request = new EthTransObj();
            request.setSignedTransactionData(signedTransactionData);
            // 类型：发币
            request.setTxType(3);
            request.setTxId(createCurrency.getId());

            boolean sendResult = producer.send(
                    Constants.Topic.MTC_BIZ_TRANS,
                    Constants.Tag.ETH_BIZ_TRANS_PENDING,
                    request,
                    Constants.Tag.ETH_BIZ_TRANS_PENDING.name() + ":" + createCurrency.getId());

            if (sendResult) {
                createCurrencyRepository.save(createCurrency);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            redisUtil.delete(RedisKeys.PENDING_CREATE_CONTRACT_PROCESS);
        }
    }

    @Override
    public Action doConsume(String json) {
        log.info("完成创建:{}", json);
        EthTransObj transInfo = CommonUtil.fromJson(json, EthTransObj.class);
        if (transInfo.getTxType() == 3) { // 发币
            CreateCurrency createCurrency = createCurrencyRepository.findById(transInfo.getTxId()).get();
            // 如果该记录已经成功了
            if (createCurrency.getStatus() == 3) {
                return Action.CommitMessage;
            }
            if (transInfo.getStatus() == 1) { // 成功
                createCurrency.setStatus(3);
                createCurrency.setTokenAddress(transInfo.getCurrencyAddress());
                createCurrency.setSuccessTime(new Date());
                createCurrency2platform(createCurrency);
            } else if (transInfo.getStatus() == 2) {// 失败
                createCurrency.setStatus(4);
            }
            createCurrencyRepository.save(createCurrency);
            // 查看是否有其他的处理中的记录，让他重新排队
            if (transInfo.getStatus() == 1) {
                createCurrencyRepository.setExpireBillStatus(1, 2, createCurrency.getTxNonce());
            }
        }
        return Action.CommitMessage;
    }

    public void createCurrency2platform(CreateCurrency createCurrency) {
        Currency currency = new Currency();

        Currency ether = currencyRepository.findByAddress(io.mtc.common.constants.Constants.ETH_ADDRESS);
        if (StringUtil.isEmpty(createCurrency.getImage())) {
            currency.setImage(ether.getImage());
        } else {
            currency.setImage(createCurrency.getImage());
        }
        currency.setSourceType(1);
        currency.setFee(ether.getFee());
        currency.setAddress(createCurrency.getTokenAddress());
        currency.setName(createCurrency.getName());
        currency.setShortName(createCurrency.getSymbol());
        currency.setPrice(BigDecimal.ZERO);
        currency.setCnyPrice(BigDecimal.ZERO);
        currencyRepository.save(currency);
    }

}
