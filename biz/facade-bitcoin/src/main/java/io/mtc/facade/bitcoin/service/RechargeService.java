package io.mtc.facade.bitcoin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.dto.CurrencyBean;
import io.mtc.common.dto.OmniHostWalletAddressTrans;
import io.mtc.common.mq.aliyun.Constants;
import io.mtc.common.mq.aliyun.MsgHandler;
import io.mtc.common.mq.aliyun.Producer;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.bitcoin.data.entity.Recharge;
import io.mtc.facade.bitcoin.data.enums.RechargeStatus;
import io.mtc.facade.bitcoin.data.repository.RechargeRepository;
import io.mtc.facade.bitcoin.feign.ServiceCurrency;
import io.mtc.facade.bitcoin.util.FacadeBtcRpcUtil;
import io.mtc.facade.bitcoin.util.OmniTransactionBean;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: hyp
 * @Date: 2019/3/15 16:44
 * @Description:
 */
@Service
@Transactional
@Slf4j
public class RechargeService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RechargeRepository rechargeRepository;

    @Resource
    private Producer producer;

    @Autowired
    private FacadeService facadeService;

    //已经扫描过的区块高度
//    private Integer scanedBlockCount = 567120;

//    @PostConstruct
//    public void init(){
//        omniScanRecharge();
//    }

    private Integer getScanedBlockCount(){
        return redisUtil.get(RedisKeys.USDT_SCANED_BLOCKE_COUNT, Integer.class);
    }

    private void setScanedBlockCount(Integer blockCount){
        redisUtil.set(RedisKeys.USDT_SCANED_BLOCKE_COUNT, blockCount);
    }

    //  USDT充值扫描
    public void omniScanRecharge() {
        log.info("===============================启动区块扫描任务===================================");
        while (true) {
            // 获取支持托管的omni币种
            List<CurrencyBean> currencyList = facadeService.getOmniCurrencyList();
            if (currencyList == null) {
                log.warn("USDT充值扫描 job - 未从redis中获取到支持omni的币种数据");
                return;
            }
            //  从redis中获取获取用户托管钱包集合
            Map<String, String> hostUserWalletMap = facadeService.getOmniHostUserWalletMap();
            if (hostUserWalletMap == null) {
                log.warn("USDT充值扫描 job - 未从redis中获取到本地托管用户数据");
                return;
            }
            Set<String> walletAddressSet = hostUserWalletMap.keySet();

            //  最新区块高度
            Integer newlyBlockCount = FacadeBtcRpcUtil.getBlockCount();
            Integer scanedBlockCount = getScanedBlockCount();
            if (scanedBlockCount == null) {
                setScanedBlockCount(newlyBlockCount - 1);
                scanedBlockCount = newlyBlockCount - 1;
            }
            if (newlyBlockCount <= scanedBlockCount) {
                log.info("已经扫描到达最新的区块高度，scanedBlockCount={}", scanedBlockCount);
                return;
            }
            log.info("开始USDT充值扫描，区块高度：{}", scanedBlockCount);
            List<String> hashList = FacadeBtcRpcUtil.omniListBlockTransactions(scanedBlockCount);
            for (String txId : hashList) {
                OmniTransactionBean transaction = FacadeBtcRpcUtil.omniGetTransaction(txId);
                if (!transaction.isValid()) {
                    log.info("不是有效数据");
                    continue;
                }
                int propertyId = transaction.getPropertyid();
                if (propertyId != 31){//不属于usdt
                    continue;
                }
                Optional<CurrencyBean> first = currencyList.stream()
                        .filter(e -> e.getBaseType().equals(5))
                        .findFirst();
                if (!first.isPresent()) {
                    continue;
                }

                String amountStr = transaction.getAmount();
                BigDecimal amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    CurrencyBean currencyBean = first.get();
                    Optional<String> optionalRechargeAddress = walletAddressSet.stream()
                            .filter(rechargeAddress -> rechargeAddress.equalsIgnoreCase(transaction.getReferenceaddress()))
                            .findFirst();
                    if (optionalRechargeAddress.isPresent()) {
                        String rechargeAddress = optionalRechargeAddress.get();
                        log.info("检查到USDT充值 - rechargeAddress={} - amount={}", rechargeAddress, amount);
                        Recharge recharge = new Recharge();
                        recharge.setCurrencyId(currencyBean.getId());
                        recharge.setFromAddress(transaction.getSendingaddress());
                        recharge.setToAddress(rechargeAddress);
                        recharge.setQty(amount);
                        recharge.setTxId(txId);
                        recharge.setRemark("USDT充值");
                        recharge.setStatus(RechargeStatus.INIT);
                        recharge.setUserId(Long.parseLong(hostUserWalletMap.get(rechargeAddress)));
                        try {
                            rechargeRepository.save(recharge);
                            //发送mq消息
                            sendRechargeMq(rechargeAddress, propertyId+"", 5, amount, transaction.getSendingaddress(), txId);
                            recharge.setStatus(RechargeStatus.SEND);
                            rechargeRepository.save(recharge);
                        } catch (Exception e) {
                            log.warn("save recharge warn - txId={} e={}", txId, e);
                        }
                    }
                }
            }
            setScanedBlockCount(scanedBlockCount + 1);
        }
    }

    private void sendRechargeMq(String walletAddress, String propertyId,
                                Integer currencyType, BigDecimal amount, String fromAddress, String txHash){
        OmniHostWalletAddressTrans trans = new OmniHostWalletAddressTrans();
        trans.setWalletAddress(walletAddress);
        trans.setPropertyId(propertyId);
        trans.setCurrencyType(currencyType);
        trans.setAmount(amount);
        trans.setFromAddress(fromAddress);
        trans.setTxHash(txHash);
        producer.send(Constants.Topic.MTC_BIZ_TRANS,
                Constants.Tag.OMNI_BIZ_HOST_WALLET_TRANS,
                trans,
                Constants.Tag.OMNI_BIZ_HOST_WALLET_TRANS.name() + ":" + trans.getTxHash());
    }

    //  TODO 重发充值处理的mq消息（处理那些充值异常的记录）
    public void processingRecharge(){

    }
}
