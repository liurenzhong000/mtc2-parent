package io.mtc.facade.user.apiController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.loan.LoanBonus;
import io.mtc.facade.user.entity.loan.LoanConfig;
import io.mtc.facade.user.entity.loan.LoanRecord;
import io.mtc.facade.user.repository.LoanBonusRepository;
import io.mtc.facade.user.repository.LoanConfigRepository;
import io.mtc.facade.user.repository.LoanRecordRepository;
import io.mtc.facade.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 借款配置的后台接口控制器
 *
 * @author Chinhin
 * 2018/10/10
 */
@ApiIgnore
@Transactional(readOnly = true)
@RequestMapping("/loanApi")
@RestController
public class LoanApiController {

    @Resource
    private LoanConfigRepository loanConfigRepository;

    @Resource
    private LoanRecordRepository loanRecordRepository;

    @Resource
    private LoanBonusRepository loanBonusRepository;

    @Resource
    private UserRepository userRepository;

    @GetMapping("/config")
    public String getLoanConfig() {
        LoanConfig loanConfig = loanConfigRepository.findById(1L).orElse(null);
        if (loanConfig == null) {
            return ResultUtil.success(new LoanConfig());
        } else {
            return ResultUtil.success(loanConfig);
        }
    }

    @Transactional
    @PostMapping("/config")
    public String updateLoanConfig(String loanConfigJson) {
        LoanConfig loanConfig = loanConfigRepository.findById(1L).orElse(null);
        if (loanConfig == null) {
            loanConfig = new LoanConfig();
        }
        LoanConfig loanConfigTemp = CommonUtil.fromJson(loanConfigJson, LoanConfig.class);
        loanConfig.setBorrowRate(loanConfigTemp.getBorrowRate());
        loanConfig.setBorrowTime(loanConfigTemp.getBorrowTime());
        loanConfig.setBorrowToken(loanConfigTemp.getBorrowToken());
        loanConfig.setMortgageToken(loanConfigTemp.getMortgageToken());
        loanConfigRepository.save(loanConfig);
        return ResultUtil.success(loanConfig);
    }

    @GetMapping
    public String select(String sn, Integer status, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<LoanRecord> specification = (Specification<LoanRecord>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(sn)) {
                list.add(criteriaBuilder.like(root.get("sn"), "%" + sn + "%"));
            }
            if (status != null) {
                if (status == 6) { // 已逾期
                    list.add(criteriaBuilder.equal(root.get("status"), 4));
                    list.add(criteriaBuilder.lessThan(root.get("shouldReturnTime"), new Date()));
                } else {
                    list.add(criteriaBuilder.equal(root.get("status"), status));
                }
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(loanRecordRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 更新记录
     * @param id 记录ID
     * @param targetStatus 状态：1审核中，2审核未通过，3借款处理中(审核通过)，4待还币(已发币)，5已完成
     * @param repayment 还款金额(个)
     */
    @Transactional
    @PostMapping
    public String update(Long id, Integer targetStatus, String repayment) {
        LoanRecord loanRecord = loanRecordRepository.findById(id).get();
        // 已还币
        if (targetStatus == 5) {
            if (loanRecord.getStatus() != 4) {
                return ResultUtil.error("只有状态为待还币的记录，才能设为已完成");
            }
            loanRecord.setRepayment(CommonUtil.toWei(repayment));
            loanRecord.setReturnTime(new Date());
        } else if (targetStatus == 4) { // 发币了
            if (loanRecord.getStatus() != 3) {
                return ResultUtil.error("只有状态为借款处理中的记录，才能设为待还币");
            }
            loanRecord.setBorrowTime(new Date());
            loanRecord.setShouldReturnTime(DateUtil.plusSeconds(new Date(), loanRecord.getBorrowDayNum() * 24 * 3600));
        } else if (targetStatus == 3 || targetStatus == 2) { // 审核通过了
            if (loanRecord.getStatus() != 1) {
                return ResultUtil.error("只有未审核中的记录，才能进行审核");
            }
            // 审批通过, 且有推广人
            if (targetStatus == 3 && StringUtil.isNotEmpty(loanRecord.getPromoter())) {
                // 推广人
                User promoterUser;
                // 邮箱登录
                if (StringUtil.checkEmail(loanRecord.getPromoter())) {
                    promoterUser = userRepository.findByEmail(loanRecord.getPromoter());
                } else {// 手机号登录
                    promoterUser = userRepository.findByPhone(loanRecord.getPromoter());
                }
                // 插入一条借币提成记录
                LoanBonus loanBonus = new LoanBonus();
                loanBonus.setUser(promoterUser);
                loanBonus.setPromoter(loanRecord.getPromoter()); // 方便显示
                loanBonus.setSn(loanRecord.getSn());
                loanBonus.setInvitee(loanRecord.getUser().getUserName()); // 被邀请人
                loanBonusRepository.save(loanBonus);
            }
            loanRecord.setVerifyTime(new Date());
        }
        loanRecord.setStatus(targetStatus);
        loanRecordRepository.save(loanRecord);
        return ResultUtil.success();
    }

    @GetMapping("/bonus")
    public String bonusRecord(Integer bonusStatus, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<LoanBonus> specification = (Specification<LoanBonus>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (bonusStatus != null) {
                list.add(criteriaBuilder.equal(root.get("bonusStatus"), bonusStatus));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(loanBonusRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 发放提成
     * @param id 记录id
     * @param bonus 提成额
     * @return 结果
     */
    @Transactional
    @PostMapping("/bonus")
    public String bonus(Long id, String bonus) {
        LoanBonus loanBonus = loanBonusRepository.findById(id).get();
        loanBonus.setBonus(CommonUtil.toWei(bonus));
        loanBonus.setBonusStatus(2);
        loanBonus.setSendTime(new Date());
        loanBonusRepository.save(loanBonus);
        return ResultUtil.success();
    }

}
