package io.mtc.facade.user.apiController;

import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.redis.util.EthRedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.constants.BillTypeConverter;
import io.mtc.facade.user.entity.BalanceUpdateRecord;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import io.mtc.facade.user.entity.yukuang.YukuangTrade;
import io.mtc.facade.user.repository.*;
import io.mtc.facade.user.service.ApiService;
import io.mtc.facade.user.util.YunKuangApiUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.math.BigInteger;
import java.util.*;

/**
 * 微服务的服务提供
 *
 * @author Chinhin
 * 2018/8/6
 */
@ApiIgnore
@Transactional(readOnly = true)
@RequestMapping("/api")
@RestController
public class ApiController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserBalanceRepository userBalanceRepository;

    @Resource
    private YunkuangTradeRepository yunkuangTradeRepository;

    @Resource
    private BalanceUpdateRecordRepository balanceUpdateRecordRepository;

    @Resource
    private BillRepository billRepository;

    @Resource
    private ApiService apiService;

    @Resource
    private EthRedisUtil ethRedisUtil;

    @Value("${aip-login-url}")
    private String aipLoginUrl;

    /**
     * 获取平台用户
     * @param email 邮件地址
     * @param phone 电话地址
     * @param pageModelStr 分页信息
     * @return 结果
     */
    @GetMapping("/user")
    public String selectUser(String email, String phone, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<User> specification = (Specification<User>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(email)) {
                list.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
            }
            if (StringUtil.isNotBlank(phone)) {
                list.add(criteriaBuilder.like(root.get("phone"), "%" + phone + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(userRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 查询云矿交易信息
     * @param payCurrency 支付币种（简称）
     * @param tradeType 交易类型，与bill的一致
     * @param selectUserType 查询方式：1手机号码，2邮箱，3ID
     * @param userInfo 对应上面的值
     * @param tradeId 交易ID
     */
    @GetMapping("/yunkuangTrade")
    public String selectYunkuangTrade(String startTime, String endTime, String payCurrency, Integer tradeType,
                                      Integer selectUserType, String userInfo, Long tradeId, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<YukuangTrade> specification = (Specification<YukuangTrade>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotEmpty(startTime)) {
                list.add(criteriaBuilder.greaterThanOrEqualTo(root.get("tradeTime"),
                        DateUtil.parseDate(startTime, "yyyy-MM-dd HH:mm:ss")));
            }
            if (StringUtil.isNotEmpty(endTime)) {
                list.add(criteriaBuilder.lessThanOrEqualTo(root.get("tradeTime"),
                        DateUtil.parseDate(endTime, "yyyy-MM-dd HH:mm:ss")));
            }
            if (StringUtil.isNotEmpty(payCurrency)) {
                list.add(criteriaBuilder.equal(root.get("payCurrency"), payCurrency));
            }
            if (tradeType != null) {
                BillTypeConverter billTypeConverter = new BillTypeConverter();
                Join<YukuangTrade, Bill> joinBill = root.join("bill", JoinType.LEFT);
                Predicate coursePredicate = criteriaBuilder.equal(joinBill.get("type"),
                        billTypeConverter.convertToEntityAttribute(tradeType));
                list.add(coursePredicate);
            }
            if (tradeId != null) {
                Join<YukuangTrade, Bill> joinBill = root.join("bill", JoinType.LEFT);
                Predicate coursePredicate = criteriaBuilder.equal(joinBill.get("id"), tradeId);
                list.add(coursePredicate);
            }
            if (selectUserType != null && selectUserType != 0 && StringUtil.isNotEmpty(userInfo)) {
                Join<YukuangTrade, User> payUserJoin = root.join("payUser", JoinType.LEFT);
                Join<YukuangTrade, User> receiveUserJoin = root.join("receiveUser", JoinType.LEFT);
                Predicate payUserPredicate;
                Predicate receiveUserPredicate;
                if (selectUserType == 1) {
                    payUserPredicate = criteriaBuilder.equal(payUserJoin.get("phone"), userInfo);
                    receiveUserPredicate = criteriaBuilder.equal(receiveUserJoin.get("phone"), userInfo);
                } else if (selectUserType == 2) {
                    payUserPredicate = criteriaBuilder.like(payUserJoin.get("email"), "%" + userInfo + "%");
                    receiveUserPredicate = criteriaBuilder.like(receiveUserJoin.get("email"), "%" + userInfo + "%");
                } else {
                    payUserPredicate = criteriaBuilder.equal(payUserJoin.get("id"), Integer.parseInt(userInfo));
                    receiveUserPredicate = criteriaBuilder.equal(receiveUserJoin.get("id"), Integer.parseInt(userInfo));
                }
                list.add(criteriaBuilder.or(payUserPredicate, receiveUserPredicate));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(yunkuangTradeRepository.findAll(specification, pageModel.make()));
    }

    @GetMapping("/userBalance")
    public String userBalance(Long uid) {
        User user = userRepository.findById(uid).get();
        List<Map<String, Object>> result = new ArrayList<>();
        userBalanceRepository.findAllByUser(user).forEach(it -> {
            Map<String, Object> temp = new HashMap<>();
            temp.put("currencyAddress", it.getCurrencyAddress());
            temp.put("currencyType", it.getCurrencyType());
            temp.put("balance", it.getBalance());
            temp.put("freezingAmount", it.getFreezingAmount());
            String currencyShortName = ethRedisUtil.getCurrencyShortName(it.getCurrencyAddress());
            temp.put("currencyShortName", currencyShortName);
            result.add(temp);
        });
        return ResultUtil.success(result);
    }

    @GetMapping("/bills")
    public String bills(Long uid, String currencyAddress, Integer type, Integer status, String pageModelStr) {
        return apiService.selectBill(uid, currencyAddress, type, status, pageModelStr);
    }

    @GetMapping("/bill/{id}")
    public String billDetail(@PathVariable("id") Long id) {
        return CommonUtil.toJson(billRepository.findById(id).orElse(null));
    }

    @Transactional
    @PostMapping("/updatePromoter")
    public String updatePromoter(Long uid, String promoter) {
        User user = userRepository.findById(uid).get();
        if (StringUtil.isNotEmpty(promoter)) {
            // 推广人
            User promoterUser;
            // 邮箱登录
            if (StringUtil.checkEmail(promoter)) {
                promoterUser = userRepository.findByEmail(promoter);
            } else {// 手机号登录
                promoterUser = userRepository.findByPhone(promoter);
                if (promoterUser == null) {
                    promoter = "86" + promoter;
                    promoterUser = userRepository.findByPhone(promoter);
                }
            }
            if (promoterUser == null) {
                return ResultUtil.error(MTCError.INVITER_USER_NOT_EXIST);
            }
            user.setPromoter(promoter);
            // 刷新云矿用户的推广人
            boolean success = YunKuangApiUtil.login(user, aipLoginUrl);
            if (success) {
                userRepository.save(user);
            } else {
                return ResultUtil.error("更新云矿的推广人失败");
            }
            return ResultUtil.success();
        } else {
            return ResultUtil.error("请输入推荐人");
        }
    }

    /**
     * 充值
     * @param uid 用户id
     * @param currencyAddress 代币地址
     * @return 结果
     */
    @Transactional
    @PostMapping("/deposit")
    public String deposit(Long uid, String number, String currencyAddress, Integer type, String admin, String note) {
        User user = userRepository.findById(uid).get();
        UserBalance userBalance = userBalanceRepository.findByUserAndCurrencyAddressAndCurrencyType(
                user, currencyAddress, type);

        BalanceUpdateRecord record = new BalanceUpdateRecord();
        record.setAddNum(CommonUtil.toWei(number));
        record.setNote(note);
        record.setAdminName(admin);
        record.setUid(uid);
        record.setUsername(user.getUserName());
        record.setCurrencyAddress(currencyAddress);

        if (userBalance == null) {
            userBalance = new UserBalance();
            userBalance.setUser(user);
            userBalance.setCurrencyAddress(currencyAddress);
            userBalance.setCurrencyType(type);

            userBalance.setBalance(CommonUtil.toWei(number));

            record.setBeforeNum(BigInteger.ZERO);
            record.setAfterNum(userBalance.getBalance());
        } else {
            BigInteger before = userBalance.getBalance();
            record.setBeforeNum(before);
            userBalance.setBalance(userBalance.getBalance().add(CommonUtil.toWei(number)));
            record.setAfterNum(userBalance.getBalance());
        }
        balanceUpdateRecordRepository.save(record);

        userBalanceRepository.save(userBalance);
        return ResultUtil.success();
    }

}