package io.mtc.facade.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.util.*;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.*;
import io.mtc.facade.user.repository.*;
import io.mtc.facade.user.util.EnvelopeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

/**
 * 红包的service
 *
 * @author Chinhin
 * 2018/8/1
 */
@Transactional(readOnly = true)
@Service
public class RedEnvelopeService {

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private BillRepository billRepository;

    @Resource
    private FundService fundService;

    @Resource
    private RedEnvelopeRepository redEnvelopeRepository;

    @Resource
    private RedEnvelopeCache redEnvelopeCache;

    @Resource
    private ReceivedRedEnvelopeRepository receivedRedEnvelopeRepository;

    @Resource
    private GuestRedEnvelopeRepository guestRedEnvelopeRepository;

    /**
     * 支持发红包的代币
     * @return 结果
     */
    public Object enabledCurrency(String key) {
        JSONArray currencyList = redEnvelopeCache.getCurrencyList();
        if (StringUtil.isBlank(key)) {
            return ResultUtil.successObj(currencyList);
        } else {
            List<Object> result = new ArrayList<>();
            for (Object tempObj : currencyList) {
                JSONObject temp = (JSONObject) tempObj;
                String shortName = temp.getString("shortName");
                String regex = "*" + key + "*";
                if (PatternMatchUtils.simpleMatch(regex, shortName)) {
                    result.add(temp);
                }
            }
            return ResultUtil.successObj(result);
        }
    }

    /**
     * 发红包
     * @param uid 用户id
     * @param currencyAddress 代币地址
     * @param amountStr 金额
     * @param num 数量
     * @param content 内容
     * @param type 1拼手气, 2等额
     * @return 结果
     */
    @Transactional
    public Object send(Long uid, String fundPassword, String currencyAddress, Integer currencyType, String amountStr, int num, String content, int type) {
        if (StringUtil.isBlank(content) || StringUtil.isBlank(currencyAddress)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        JSONArray enableCurrencyList = redEnvelopeCache.getCurrencyList();
        String image = null;
        for (Object tempObj : enableCurrencyList) {
            JSONObject temp = (JSONObject) tempObj;
            if (temp.getString("address").equals(currencyAddress)) {
                image = temp.getString("image");
                break;
            }
        }
        // 该代币不支持红包
        if (image == null) {
            return ResultUtil.errorObj(MTCError.CURRENCY_NOT_ENABLE_ENVELOPE);
        }
        // 余额check
        User user = userRepository.findById(uid).get();
        Object errorInfo = fundService.userVerifyAIP(user, fundPassword);
//        if (errorInfo != null) {
//            return errorInfo;
//        }
        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, currencyType);
        BigInteger amount = NumberUtil.toBigInteger(amountStr);
        // 红包总金额
        BigInteger totalAmount;
        if (type == 1) { // 拼手气
            totalAmount = amount;
            // 小于最低金额
            if (totalAmount.compareTo(Constants.CURRENCY_UNIT.multiply(BigInteger.valueOf(num))) < 0) {
                return ResultUtil.errorObj(MTCError.RED_ENVELOP_AMOUNT_TOO_LOW);
            }
        } else { // 等额
            totalAmount = amount.multiply(BigInteger.valueOf(num));
            // 小于最低金额
            if (amount.compareTo(Constants.CURRENCY_UNIT) < 0) {
                return ResultUtil.errorObj(MTCError.RED_ENVELOP_AMOUNT_TOO_LOW);
            }
        }
        // 余额不足
        if (userBalance == null || userBalance.getBalance().compareTo(totalAmount) < 0) {
            return ResultUtil.errorObj(MTCError.BALANCE_NOT_ENOUGH);
        }

        // 红包信息
        RedEnvelope redEnvelope = new RedEnvelope();
        redEnvelope.setAmount(totalAmount);
        redEnvelope.setContent(content);
        redEnvelope.setCurrencyType(currencyType);
        redEnvelope.setCurrencyAddress(currencyAddress);
        redEnvelope.setCurrencyShortName(redEnvelopeCache.getCurrencyShortName(currencyAddress));
        redEnvelope.setNum(num);
        redEnvelope.setType(type);
        redEnvelope.setUser(user);
        redEnvelope.setCurrencyImage(image);
        redEnvelopeRepository.save(redEnvelope);

        // 账单信息
        Bill bill = new Bill();
        bill.setStatus(BillStatus.PROCESSING);
        bill.setBalance(userBalance);
        bill.setOutcome(totalAmount);
        bill.setType(BillType.SEND_RED_ENVELOPE);
        bill.setRelativeId(redEnvelope.getId());
        bill.setCurrentBalance(userBalance.getBalance());
        billRepository.save(bill);

        redEnvelope.setBillId(bill.getId());
        redEnvelopeRepository.save(redEnvelope);

        // 扣除余额
        userBalance.setBalance(userBalance.getBalance().subtract(totalAmount));
        userBalanceRepository.save(userBalance);

        /* redis 处理 */
        // 拆分成小红包
        BigInteger[] pieces;
        if (type == 1) { // 拼手气
            pieces = EnvelopeUtil.luckyDraw(totalAmount, num);
        } else { // 等额
            pieces = new BigInteger[num];
            Arrays.fill(pieces, amount);
        }
        redEnvelopeCache.splitRedEnvelope2cache(redEnvelope.getId(), pieces);
        // 设置红包弹窗缓存
        redEnvelopeCache.setEnvelopePopDetail(redEnvelope);
        // 设置发送信息缓存
        redEnvelopeCache.setSendInfo(redEnvelope, true);
        return ResultUtil.successObj(JSON.parseObject(CommonUtil.toJson(redEnvelope)));
    }

    /**
     * 发送的红包
     * @param uid 用户id
     * @param pageModel 翻页信息
     * @return 结果
     */
    public Object sendRedEnvelopes(long uid, PagingModel pageModel) {
        User user = userRepository.findById(uid).get();
        Page<RedEnvelope> redEnvelopes = redEnvelopeRepository.findAllByUserOrderByCreateTimeDesc(user, pageModel.make());
        return JSON.parseObject(PagingResultUtil.list(redEnvelopes));
    }

    /**
     * 改变红包状态
     * @param envelopeId 红包id
     * @param status 状态：1进行中，2暂停，3结束
     * @return 红包状态
     */
    @Transactional
    public Object updateEnvelopeStatus(long uid, long envelopeId, int status) {
        RedEnvelope redEnvelope = redEnvelopeRepository.findById(envelopeId).get();
        if (redEnvelope.getUser().getId() != uid) {
            return ResultUtil.errorObj(MTCError.ENVELOPE_UPDATE_NO_AUTH);
        }
        if (redEnvelope.getStatus() == 3) {
            return ResultUtil.errorObj(MTCError.ENVELOPE_ENDED);
        }
        redEnvelope.setStatus(status);
        redEnvelopeRepository.save(redEnvelope);
        redEnvelopeCache.setSendInfo(redEnvelope, false);
        // 关闭红包流程
        if (status == 3) {
            return redEnvelopeCache.end(redEnvelope);
        } else {
            return ResultUtil.successObj(redEnvelope.getStatus());
        }
    }

    /**
     * 收到红包
     * @param uid 用户id
     * @param envelopeId 红包id
     * @return 红包详情
     */
    @Transactional
    public Object receiveEnvelope(long uid, long envelopeId) {
        if (uid == 0 || envelopeId == 0) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        redEnvelopeCache.saveReceivedRecordById(uid, envelopeId, BigInteger.ZERO, false);
        return ResultUtil.successObj();
    }

    /**
     * 红包弹窗详情
     * @param envelopeId 红包id
     * @return 详情
     */
    public Object redEnvelopePopDetail(long envelopeId) {
        return ResultUtil.successObj(redEnvelopeCache.getPopDetail(envelopeId));
    }

    /**
     * 红包发送情况
     * @param envelopeId 红包id
     * @return 结果
     */
    public Object sendEnvelopeInfo(long envelopeId) {
        return ResultUtil.successObj(redEnvelopeCache.getSendInfo(envelopeId));
    }

    /**
     * 抢红包
     * @param uid 用户id
     * @param envelopId 红包id
     * @return 返回抢到的金额，0表示没抢到
     */
    @Transactional
    public Object grab(Long uid, Long envelopId) {
        if (uid == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 已经抢过了
        if (redEnvelopeCache.isGrabbed(uid.toString(), envelopId)) {
            return ResultUtil.errorObj(MTCError.ENVELOPE_GRABBED);
        }

        // 抢到的金额
        BigInteger grabbedAmount = redEnvelopeCache.grab(envelopId);
        if (BigInteger.ZERO.equals(grabbedAmount)) {
            redEnvelopeCache.saveReceivedRecordById(uid, envelopId, BigInteger.ZERO, true);
            return ResultUtil.successObj(BigInteger.ZERO);
        }
        // 新增或更新抢到的记录
        User user = userRepository.findById(uid).orElse(null);
        RedEnvelope redEnvelope = redEnvelopeRepository.findById(envelopId).orElse(null);
        if (user == null || redEnvelope == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 抢到后的处理
        redEnvelopeCache.grabbed(user, redEnvelope, grabbedAmount);
        return ResultUtil.successObj(grabbedAmount);
    }

    public Object receivedHistory(Long uid, PagingModel pageModel) {
        User user = userRepository.findById(uid).get();
        Page<ReceivedRedEnvelope> list = receivedRedEnvelopeRepository.findAllByUserOrderByCreateTimeDesc(user, pageModel.make());
        return JSON.parseObject(PagingResultUtil.list(list));
    }

    /**
     * 红包明细
     * @param uid 用户id
     * @param envelopeId 红包id
     * @return 结果
     */
    public Object envelopeDetail(Long uid, Long envelopeId) {
        RedEnvelope redEnvelope = redEnvelopeRepository.findById(envelopeId).get();
        User sendUser = redEnvelope.getUser();

        Map<String, Object> result = new HashMap<>();
        result.put("amount", redEnvelope.getAmount());
        result.put("photo", sendUser.getPhoto());
        result.put("nick", sendUser.getNick());
        result.put("type", redEnvelope.getType());
        result.put("content", redEnvelope.getContent());
        result.put("currencyShortName", redEnvelope.getCurrencyShortName());
        result.put("currencyImage", redEnvelope.getCurrencyImage());

        List<Map<String, Object>> list = new ArrayList<>();

        List<ReceivedRedEnvelope> grabbedList = receivedRedEnvelopeRepository.findAllByRedEnvelopeAndAmountNot(
                redEnvelope, BigInteger.ZERO, Sort.by(Sort.Direction.DESC, "amount"));
        for (int i = 0; i < grabbedList.size(); i++) {
            ReceivedRedEnvelope tempGrab = grabbedList.get(i);
            Map<String, Object> temp = new HashMap<>();
            temp.put("bestLuck", i == 0 && (redEnvelope.getType() == 1));
            temp.put("nick", tempGrab.getUser().getNick());
            temp.put("photo", tempGrab.getUser().getPhoto());
            temp.put("time", tempGrab.getUpdateTime().getTime());
            temp.put("amount", tempGrab.getAmount());
            list.add(temp);
            // 当前用户
            if (tempGrab.getUser().getId().equals(uid)) {
                result.put("grabbedAmount", tempGrab.getAmount());
            }
        }
        list.sort((o1, o2) -> {
            long t1 = (long) o1.get("time");
            long t2 = (long) o2.get("time");
            return Long.compare(t1, t2);
        });
        result.put("list", list);
        return ResultUtil.successObj(result);
    }

    /* ########################################### 游客红包 ########################################### */

    /**
     * 游客抢红包
     * @param deviceId 设备id
     * @param envelopeId 红包id
     * @return 返回抢到的金额，0表示没抢到
     */
    @Transactional
    public Object guestGrab(String deviceId, Long envelopeId) {
        if (StringUtil.isEmpty(deviceId)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 已经抢过了
        if (redEnvelopeCache.isGrabbed(deviceId, envelopeId)) {
            return ResultUtil.errorObj(MTCError.ENVELOPE_GRABBED);
        }
        // 抢到的金额
        BigInteger grabbedAmount = redEnvelopeCache.grab(envelopeId);
        if (BigInteger.ZERO.equals(grabbedAmount)) {
            return ResultUtil.successObj(BigInteger.ZERO);
        }
        RedEnvelope redEnvelope = redEnvelopeRepository.findById(envelopeId).orElse(null);
        if (redEnvelope == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 增加一条游客抢到的记录
        GuestRedEnvelope guestRedEnvelope = new GuestRedEnvelope();
        guestRedEnvelope.setAmount(grabbedAmount);
        guestRedEnvelope.setDeviceId(deviceId);
        guestRedEnvelope.setRedEnvelope(redEnvelope);
        guestRedEnvelopeRepository.save(guestRedEnvelope);
        return ResultUtil.successObj(grabbedAmount);
    }

    /**
     * 游客抢到的红包一览
     * @param deviceId 设备id
     * @param pageModel 翻页信息
     * @return 结果
     */
    public Object guestGrabbedHistory(String deviceId, PagingModel pageModel) {
        if (StringUtil.isEmpty(deviceId)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        Page<GuestRedEnvelope> list = guestRedEnvelopeRepository.findAllByDeviceIdOrderByCreateTimeDesc(deviceId, pageModel.make());
        return JSON.parseObject(PagingResultUtil.list(list));
    }

    /**
     * 认领红包
     * @param deviceId 设备id
     * @param uid 用户id
     * @return 结果
     */
    @Transactional
    public Object guestReclaim(String deviceId, Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 1个小时内创建的红包
        List<GuestRedEnvelope> willReclaimEnvelope =
                guestRedEnvelopeRepository.findAllByDeviceIdAndCreateTimeGreaterThanEqual(deviceId,
                        DateUtil.plusSeconds(new Date(),-3600));
        int failureNum = 0;
        int successNum = 0;
        for (GuestRedEnvelope it : willReclaimEnvelope) {
            // 把抢过的放到拦截里
            boolean grabbed = redEnvelopeCache.isGrabbed(user.getId().toString(), it.getRedEnvelope().getId());
            // 已经抢过这个红包了
            if (grabbed) {
                failureNum ++;
                continue;
            }
            successNum ++;
            // 抢到后的处理
            redEnvelopeCache.grabbed(user, it.getRedEnvelope(), it.getAmount());
            guestRedEnvelopeRepository.delete(it);
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("successNum", successNum);
        result.put("failureNum", failureNum);
        return ResultUtil.successObj(result);
    }

}