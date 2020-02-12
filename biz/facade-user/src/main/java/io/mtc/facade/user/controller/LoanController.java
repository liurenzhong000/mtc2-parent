package io.mtc.facade.user.controller;

import com.alibaba.fastjson.JSON;
import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
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
import io.mtc.facade.user.service.UserService;
import io.swagger.annotations.*;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 借款控制器
 *
 * @author Chinhin
 * 2018/10/11
 */
@Api(description="借款相关", tags = {"借款"})
@Transactional(readOnly = true)
@RequestMapping("/loan")
@RestController
public class LoanController {

    @Resource
    private LoanConfigRepository loanConfigRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserService userService;

    @Resource
    private LoanRecordRepository loanRecordRepository;

    @Resource
    private LoanBonusRepository loanBonusRepository;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @ApiOperation(value="获取借款的下拉选项", tags = {"需要token"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "mortgageToken: 抵押币种（用逗号分隔）\n" +
                    "borrowToken：借入币种（用逗号分隔）\n" +
                    "borrowTime: 借款期限（逗号分隔的纯数字，单位是日）\n" +
                    "borrowRate：借款利率（逗号分隔的纯数字，单位是%）\n")
    })
    @GetMapping("/config")
    public Object getLoanConfig() {
        LoanConfig loanConfig = loanConfigRepository.findById(1L).orElse(null);
        if (loanConfig == null) {
            return ResultUtil.successObj(new LoanConfig());
        } else {
            return ResultUtil.successObj(loanConfig);
        }
    }

    @ApiOperation(value="借币", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="mortgageToken", value = "抵押币种", required = true, dataType = "String"),
            @ApiImplicitParam(name="borrowToken", value = "借入币种", required = true, dataType = "String"),
            @ApiImplicitParam(name="borrowNumber", value = "借入数量(wei)", required = true, dataType = "String"),
            @ApiImplicitParam(name="borrowDayNum", value = "借款期限（日）", required = true, dataType = "int"),
            @ApiImplicitParam(name="borrowRate", value = "借款利率(需要与返回的下拉选项保持高度一致）", required = true, dataType = "String"),
            @ApiImplicitParam(name="name", value = "姓名", required = true, dataType = "String"),
            @ApiImplicitParam(name="identifyNum", value = "身份证", required = true, dataType = "String"),
            @ApiImplicitParam(name="phone", value = "手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="wechat", value = "微信号", required = true, dataType = "String"),
            @ApiImplicitParam(name="promoter", value = "推广人(手机号/邮箱), 没有请传null或空串", dataType = "String"),
    })
    @Transactional
    @PutMapping
    public Object loan(@RequestHeader Long uid, String mortgageToken, String borrowToken, String borrowNumber,
                       Integer borrowDayNum, String borrowRate, String name, String identifyNum,
                       String phone, String wechat, String promoter) {
        // 是否已经设置了参数的check
        LoanConfig loanConfig = loanConfigRepository.findById(1L).orElse(null);
        if (loanConfig == null
                || StringUtil.isEmpty(loanConfig.getMortgageToken())
                || StringUtil.isEmpty(loanConfig.getBorrowRate())
                || StringUtil.isEmpty(loanConfig.getBorrowTime())
                || StringUtil.isEmpty(loanConfig.getBorrowToken())) {
            return ResultUtil.errorObj(MTCError.SYSTEM_BUSY);
        }
        List<String> permitMortgageToken = Arrays.asList(loanConfig.getMortgageToken().split(","));
        if (!permitMortgageToken.contains(mortgageToken)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }

        List<String> permitBorrowRate = Arrays.asList(loanConfig.getBorrowRate().split(","));
        if (!permitBorrowRate.contains(borrowRate)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }

        List<String> permitBorrowTime = Arrays.asList(loanConfig.getBorrowTime().split(","));
        if (!permitBorrowTime.contains(String.valueOf(borrowDayNum))) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }

        List<String> permitBorrowToken = Arrays.asList(loanConfig.getBorrowToken().split(","));
        if (!permitBorrowToken.contains(borrowToken)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        LoanRecord loanRecord = new LoanRecord();
        loanRecord.setMortgageToken(mortgageToken);
        loanRecord.setBorrowToken(borrowToken);
        loanRecord.setBorrowDayNum(borrowDayNum);
        loanRecord.setBorrowRate(new BigDecimal(borrowRate));
        loanRecord.setBorrowNumber(new BigDecimal(borrowNumber).toBigInteger());
        loanRecord.setName(name);
        loanRecord.setIdentifyNum(identifyNum);
        loanRecord.setPhone(phone);
        loanRecord.setWechat(wechat);
        loanRecord.setStatus(1);
        loanRecord.setUser(userRepository.findById(uid).get());
        // 序列号
        String sn = "J" + DateUtil.formatDateTimeMillis(new Date()) + StringUtil.randomNumber(3);
        loanRecord.setSn(sn);
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
                return ResultUtil.errorObj(MTCError.INVITER_USER_NOT_EXIST);
            }
            if (promoterUser.getId().equals(uid)) {
                return ResultUtil.errorObj(MTCError.INVITER_USER_CANT_BE_SELF);
            }
            loanRecord.setPromoter(promoter);
        }

        loanRecordRepository.save(loanRecord);
        return ResultUtil.successObj(CommonUtil.jsonToMap(CommonUtil.toJson(loanRecord)));
    }

    @ApiOperation(value="借币记录", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>返回的字段意义与新增传参一致，下面只列出未传参的字段：</strong>\n" +
                    "sn: 编号\n" +
                    "repayment：还款金额\n" +
                    "status：状态：1审核中，2审核未通过，3借款处理中(审核通过)，4待还币(已发币)，5已完成\n" +
                    "verifyTime: 审核时间（状态大于1的时候有值）\n" +
                    "borrowTime：放款时间（状态为4，5有值）\n" +
                    "shouldReturnTime：应还款时间（状态为4，5有值）,状态为4且当前时间晚于此时间，前端显示为已逾期\n" +
                    "returnTime：应还款时间（状态为4，5有值）\n")
    })
    @GetMapping
    public Object records(@RequestHeader Long uid, @ModelAttribute PagingModel pageModel) {
        User user = userRepository.findById(uid).get();
        Page<LoanRecord> records = loanRecordRepository.findAllByUser(user, pageModel.make());
        return JSON.parseObject(PagingResultUtil.list(records));
    }

    @ApiOperation(value="提成奖励记录", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "sn: 编号\n" +
                    "bonusStatus：状态：1未发放，2已发放\n" +
                    "createTime: 创建时间（对应借款记录审核通过时间）\n" +
                    "bonus：奖励数额（wei）\n" +
                    "promoter：推广人\n" +
                    "sendTime：奖励发放时间\n" +
                    "invitee：被邀请人\n")
    })
    @GetMapping("/bonus")
    public Object bonusRecords(@RequestHeader Long uid, @ModelAttribute PagingModel pageModel) {
        User user = userRepository.findById(uid).get();
        Page<LoanBonus> records = loanBonusRepository.findAllByUser(user, pageModel.make());
        return JSON.parseObject(PagingResultUtil.list(records));
    }

    @ApiOperation(value="提成奖励统计信息", tags = {"需要token"}, notes = "分享链接为：\n" +
            "测试服：（注意域名其实就是测试ip地址，为了过微信才用的域名）http://mtc.test.heymking.com/share/{手机号或邮箱}\n" +
            "正式服：（暂未部署）https://app.mtc.io/share/{手机号或邮箱}")
    @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "num: 成功邀请好友个数\n" +
                    "totalRewards：累积获得mtc数量(单位wei)\n")
    })
    @GetMapping("/bonus/count")
    public Object bonusCount(@RequestHeader Long uid) {
        String sql = "SELECT COUNT(id) num, SUM(bonus) totalRewards FROM loan_bonus WHERE user_id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, uid);
        return ResultUtil.successObj(result);
    }

}
