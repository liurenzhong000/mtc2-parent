package io.mtc.facade.user.service;

import com.alibaba.fastjson.JSONArray;
import io.mtc.common.constants.Constants;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.*;
import io.mtc.facade.user.feign.ServiceCurrency;
import io.mtc.facade.user.repository.*;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;

/**
 * 主要红包缓存处理
 * 目的是降低service的代码量
 *
 * @author Chinhin
 * 2018/8/2
 */
@Slf4j
@Service
public class RedEnvelopeCache {

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Resource
    private ServiceCurrency serviceCurrency;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ReceivedRedEnvelopeRepository receivedRedEnvelopeRepository;

    @Resource
    private RedEnvelopeRepository redEnvelopeRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private BillRepository billRepository;

    @Resource
    private GuestRedEnvelopeRepository guestRedEnvelopeRepository;

    /**
     * 获取币种一览
     * @return 币种一览
     */
    JSONArray getCurrencyList() {
        Map result;
        // 币种一览
        Object listObj = ethRedisUtil.get(RedisKeys.ENABLE_RED_ENVELOPE_CURRENCY);
        if (listObj != null) {
            result = (Map) listObj;
        } else {
            String s = serviceCurrency.redPacketEnableCurrency();
            result = CommonUtil.fromJson(s, HashMap.class);
            // 5秒钟刷新一次缓存
            ethRedisUtil.set(RedisKeys.ENABLE_RED_ENVELOPE_CURRENCY, result, 30);
        }
        return (JSONArray) result.get("result");
    }

    String getCurrencyShortName(String currencyAddress) {
        return ethRedisUtil.getCurrencyShortName(currencyAddress);
    }

    void splitRedEnvelope2cache(long envelopeId, BigInteger[] splitAmount) {
        // 存入缓存
        String key = RedisKeys.UN_GRABBED_ENVELOPE_QUEUE(envelopeId);
        for (BigInteger temp : splitAmount) {
            ethRedisUtil.lPush(key,  temp);
        }
    }

    /**
     * 存储红包弹窗缓存
     * @param redEnvelope 红包对象
     */
    void setEnvelopePopDetail(@NotNull RedEnvelope redEnvelope) {
        User user = redEnvelope.getUser();
        // 红包弹窗详情key
        String key = RedisKeys.ENVELOPE_POP_DETAIL(redEnvelope.getId());
        ethRedisUtil.hsetString(key, "nick", user.getNick());
        ethRedisUtil.hsetString(key, "photo", user.getPhoto());
        ethRedisUtil.hsetString(key, "content", redEnvelope.getContent());
        ethRedisUtil.hsetString(key, "currencyShortName", redEnvelope.getCurrencyShortName());
    }

    /**
     * 设置红包发送详情
     * @param redEnvelope 红包对象
     */
    void setSendInfo(@NotNull RedEnvelope redEnvelope, Boolean isInit) {
        String key = RedisKeys.ENVELOPE_SEND_INFO(redEnvelope.getId());
        ethRedisUtil.hsetString(key, "content", redEnvelope.getContent());
        ethRedisUtil.hsetString(key, "amount", redEnvelope.getAmount().toString());
        ethRedisUtil.hsetString(key, "currencyShortName", redEnvelope.getCurrencyShortName());
        ethRedisUtil.hsetString(key, "currencyAddress", redEnvelope.getCurrencyAddress());
        ethRedisUtil.hsetString(key, "currencyImage", redEnvelope.getCurrencyImage());
        ethRedisUtil.hsetString(key, "num", redEnvelope.getNum().toString());
        ethRedisUtil.hsetString(key, "status", redEnvelope.getStatus().toString());
        // 抢到的金额 抢到的个数
        if (isInit) {
            ethRedisUtil.hsetString(key, "grabbedAmount", "0");
            ethRedisUtil.hsetString(key, "grabbedNum", "0");
        }
    }

    Map getPopDetail(long envelopeId) {
        return ethRedisUtil.hget(RedisKeys.ENVELOPE_POP_DETAIL(envelopeId));
    }

    Map getSendInfo(long envelopeId) {
        return ethRedisUtil.hget(RedisKeys.ENVELOPE_SEND_INFO(envelopeId));
    }

    /**
     * 判断是否抢过了
     * @param uid 用户id
     * @param envelopeId 红包id
     * @return true表示抢过了
     */
    @Synchronized
    boolean isGrabbed(String uid, long envelopeId) {
        String grabbedUserKey = RedisKeys.ENVELOPE_GRABBED_USER(envelopeId);
        Object isGrabbed = ethRedisUtil.hgetString(grabbedUserKey, uid);
        ethRedisUtil.hsetString(grabbedUserKey, uid, Constants.EMPTY);
        // 已经抢过了
        return isGrabbed != null;
    }

    BigInteger grab(Long envelopeId) {
        // 没有抢的队列
        String unGrabbedKey = RedisKeys.UN_GRABBED_ENVELOPE_QUEUE(envelopeId);
        Object o = ethRedisUtil.rPop(unGrabbedKey);
        // 已经抢完了
        if (o == null) {
            return BigInteger.ZERO;
        }
        return (BigInteger) o;
    }

    private void updateSendInfo(Long envelopId, BigInteger grabbedAmount) {
        // 发红包缓存信息key
        String sendInfoKey = RedisKeys.ENVELOPE_SEND_INFO(envelopId);
        // 抢到人数+1
        ethRedisUtil.incrby(sendInfoKey, "grabbedNum", 1);
        Object totalGrabbedAmountObj = ethRedisUtil.hgetString(sendInfoKey, "grabbedAmount");
        BigInteger result = new BigInteger(totalGrabbedAmountObj.toString()).add(grabbedAmount);
        // 抢到的金额
        ethRedisUtil.hsetString(sendInfoKey, "grabbedAmount", result.toString());
    }

    /**
     * 是否已经抢完
     * @param envelopeId 红包id
     * @return true表示抢完了
     */
    private boolean isGrabbedOut(long envelopeId) {
        // 没有抢的队列
        String unGrabbedKey = RedisKeys.UN_GRABBED_ENVELOPE_QUEUE(envelopeId);
        return ethRedisUtil.size(unGrabbedKey) == 0;
    }

    /**
     * 抢到后的处理
     * 1.增加余额
     * 2.增加抢到的账单
     * 3.增加抢到的记录
     * 4.并判断如果抢完了就结束红包
     *
     * @param user 用户
     * @param redEnvelope 红包
     * @param grabbedAmount 抢到的金额
     */
    void grabbed(User user, RedEnvelope redEnvelope, BigInteger grabbedAmount) {
        // 更新发送信息
        updateSendInfo(redEnvelope.getId(), grabbedAmount);

        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, redEnvelope.getCurrencyAddress(), redEnvelope.getCurrencyType());
        if (userBalance == null) {
            userBalance = new UserBalance();
            userBalance.setCurrencyAddress(redEnvelope.getCurrencyAddress());
            userBalance.setUser(user);
            UserBalance sendUserBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                    redEnvelope.getUser(), redEnvelope.getCurrencyAddress(), redEnvelope.getCurrencyType());
            userBalance.setWalletAddress(sendUserBalance.getWalletAddress());
            userBalance.setCurrencyType(sendUserBalance.getCurrencyType());
        }
        // 入库一个抢到红包的收到红包信息
        saveReceiveRecord(user, redEnvelope, grabbedAmount, true);
        // 更新余额
        userBalance.setBalance(userBalance.getBalance().add(grabbedAmount));

        // 新增账单
        Bill bill = new Bill();
        bill.setIncome(grabbedAmount);
        bill.setRelativeId(redEnvelope.getId());
        bill.setStatus(BillStatus.SUCCESS);
        bill.setType(BillType.GRAB_RED_ENVELOPE);
        bill.setBalance(userBalance);
        bill.setNote(redEnvelope.getUser().getNick());
        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        // 已经抢完了
        if (isGrabbedOut(redEnvelope.getId())) {
            redEnvelope.setIsGrabbedOut(true);
            redEnvelope.setStatus(3);
            redEnvelopeRepository.save(redEnvelope);
            setSendInfo(redEnvelope, false);
            end(redEnvelope);
        }
    }

    /**
     * 入库一条收到红包的记录
     * @param uid 用户id
     * @param redEnvelopeId 红包id
     * @param amount 抽到的数量
     * @param isOpened 是否打开了
     */
    void saveReceivedRecordById(long uid, long redEnvelopeId, BigInteger amount, boolean isOpened) {
        User user = userRepository.findById(uid).get();
        RedEnvelope redEnvelope = redEnvelopeRepository.findById(redEnvelopeId).get();
        saveReceiveRecord(user, redEnvelope, amount, isOpened);
    }
    private void saveReceiveRecord(User user, RedEnvelope redEnvelope, BigInteger amount, boolean isOpened) {
        ReceivedRedEnvelope receivedRedEnvelope = receivedRedEnvelopeRepository.findByUserAndRedEnvelope(user, redEnvelope);
        if (receivedRedEnvelope == null) {
            receivedRedEnvelope = new ReceivedRedEnvelope();
            receivedRedEnvelope.setUser(user);
            receivedRedEnvelope.setRedEnvelope(redEnvelope);
        }
        if (!receivedRedEnvelope.getIsOpened()) {
            receivedRedEnvelope.setIsOpened(isOpened);
            receivedRedEnvelope.setAmount(amount);
        }
        receivedRedEnvelopeRepository.save(receivedRedEnvelope);
    }

    /**
     * 结束红包
     * @param redEnvelope 红包对象
     */
    Object end(RedEnvelope redEnvelope) {
        List<ReceivedRedEnvelope> receivedRedEnvelopes = redEnvelope.getReceivedRedEnvelopes();

        BigInteger grabbedAmount = BigInteger.ZERO;
        for (ReceivedRedEnvelope temp : receivedRedEnvelopes) {
            grabbedAmount = grabbedAmount.add(temp.getAmount());
        }
        Bill bill = billRepository.findById(redEnvelope.getBillId()).get();
        bill.setStatus(BillStatus.SUCCESS);
        // 有退款
        if (!grabbedAmount.equals(redEnvelope.getAmount())) {
            bill.setRefund(bill.getOutcome().subtract(grabbedAmount));

            User user = redEnvelope.getUser();
            String currencyAddress = redEnvelope.getCurrencyAddress();
            UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                    user, currencyAddress, redEnvelope.getCurrencyType());
            userBalance.setBalance(userBalance.getBalance().add(bill.getRefund()));
            userBalanceRepository.save(userBalance);

            bill.setCurrentBalance(userBalance.getBalance());
            billRepository.save(bill);
        } else {
            billRepository.save(bill);
        }
        // 删除小红包缓存
        ethRedisUtil.delete(RedisKeys.UN_GRABBED_ENVELOPE_QUEUE(redEnvelope.getId()));
        return ResultUtil.successObj();
    }

    @Transactional
    public void cleanData() {
        /* 关闭过期红包 */
        Date before24hourDate = DateUtil.plusSeconds(new Date(), - 60 * 60 * 24);
        // 获得24小时前发布的正在发送的红包
        List<RedEnvelope> willClose = redEnvelopeRepository.findAllByStatusAndCreateTimeBefore(1, before24hourDate);
        for (RedEnvelope temp : willClose) {
            log.info("关闭红包：【{}】", temp.getId());
            temp.setStatus(3);
            end(temp);
            redEnvelopeRepository.save(temp);
            guestRedEnvelopeRepository.deleteAllByRedEnvelope(temp);
        }

        /* 清理红包缓存 */
        // 所有钱包地址集合
        Set<Long> willClearIds = new HashSet<>();
        Set<String> popKeys = ethRedisUtil.getKeysBeginWith(RedisKeys.ENVELOPE_POP_DETAIL_PREFIX);
        for (String temp : popKeys) {
            String idStr = temp.substring(RedisKeys.ENVELOPE_POP_DETAIL_PREFIX.length());
            willClearIds.add(Long.valueOf(idStr));
        }
        Set<String> grabbedUserKeys = ethRedisUtil.getKeysBeginWith(RedisKeys.ENVELOPE_GRABBED_USER_PREFIX);
        for (String temp : grabbedUserKeys) {
            String idStr = temp.substring(RedisKeys.ENVELOPE_GRABBED_USER_PREFIX.length());
            willClearIds.add(Long.valueOf(idStr));
        }
        Set<String> sendKeys = ethRedisUtil.getKeysBeginWith(RedisKeys.ENVELOPE_SEND_INFO_PREFIX);
        for (String temp : sendKeys) {
            String idStr = temp.substring(RedisKeys.ENVELOPE_SEND_INFO_PREFIX.length());
            willClearIds.add(Long.valueOf(idStr));
        }
        willClearIds.forEach(it -> {
            log.info("清理红包缓存：【{}】", it);
            RedEnvelope redEnvelope = redEnvelopeRepository.findById(it).orElse(null);
            // 已经结束了
            if (redEnvelope == null || redEnvelope.getStatus() == 3) {
                ethRedisUtil.delete(RedisKeys.ENVELOPE_POP_DETAIL(it));
                ethRedisUtil.delete(RedisKeys.ENVELOPE_GRABBED_USER(it));
                ethRedisUtil.delete(RedisKeys.ENVELOPE_SEND_INFO(it));
            }
        });
    }

}
