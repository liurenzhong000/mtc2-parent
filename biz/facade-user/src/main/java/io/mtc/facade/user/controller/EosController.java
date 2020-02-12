package io.mtc.facade.user.controller;


import io.mtc.common.constants.Constants;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.util.AesCBC;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.user.service.DepositWithdrawService;
import io.mtc.facade.user.service.EosService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * EOS相关接口
 *
 * @author Chinhin
 * 2018/9/11
 */
@Slf4j
@Api(description="EOS相关接口", tags = {"EOS"})
@Transactional(readOnly = true)
@RequestMapping("/eos")
@RestController
public class EosController {

    @Resource
    private EosService eosService;

    @Resource
    private DepositWithdrawService depositWithdrawService;

    @ApiOperation(value="获取创建EOS账户手续费", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "money: 手续费价值多少美元\n" +
                    "moneyCny：手续费价值多少人民币\n" +
                    "needEos: 需要多少EOS币(10e+4)\n" +
                    "eosBalance：eos余额\n" +
                    "needMtc：需要多少MTC币(10e+18)\n" +
                    "mtcBalance：用户mtc余额\n" +
                    "needEth：需要多少eth币(10e+18)\n" +
                    "ethBalance：用户eth余额\n")
    })
    @GetMapping("/createEosAccountFee")
    public Object createEosAccountFee(@RequestHeader Long uid) {
        return eosService.createEosAccountFee(uid);
    }

    @ApiOperation(value="创建EOS账户", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="payType", value = "支付方式(1:MTC, 2:ETH, 3:BCH[暂不支持], 4:EOS)", required = true, dataType = "int"),
            @ApiImplicitParam(name="accountName", value = "EOS账户名", required = true, dataType = "String"),
            @ApiImplicitParam(name="ownerKey", value = "EOS账户公钥1 public-owner-key", required = true, dataType = "String"),
            @ApiImplicitParam(name="activeKey", value = "EOS账户公钥2 public-active-key", required = true, dataType = "String"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @Transactional
    @PutMapping("/createEosAccount")
    public Object createEosAccount(@RequestHeader Long uid, Integer payType, String accountName, String ownerKey, String activeKey,
                                   String fundPassword, Boolean isValidByPhone, String validCode) {
//        log.info("uid:{}, payType:{}, accountName:{}, ownerKey:{}, activeKey:{}, fundPassword:{}, isValidByPhone:{}, validCode:{}",
//                uid, payType, accountName, ownerKey, activeKey, fundPassword, isValidByPhone, validCode);
        return eosService.createEosAccount(uid, payType, accountName, ownerKey, activeKey, fundPassword, isValidByPhone, validCode);
    }

    @ApiOperation(value="完成充值", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="params", value = "加密后的json\n" +
                    "{\n" +
                    "\ttxId:'账单id'【long】, \n" +
                    "\tamount: '转账金额str(10的18次方为1)'【string】, \n" +
                    "\tstatus: (1成功，2失败)【int】\n" +
                    "}", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/eosApi/completeDeposit")
    public Object completeDeposit(String params) {
        EthTransObj transInfo;
        try {
            String json = AesCBC.getInstance().simpleDecrypt(params, Constants.EOS_SERVER_SECRET);
            transInfo = CommonUtil.fromJson(json, EthTransObj.class);
        } catch (Exception e) {
            log.error("充值解密出现错误");
            return ResultUtil.error("解密出现错误", 500);
        }
        transInfo.setCoinType(3);
        depositWithdrawService.completeDeposit(transInfo);
        return ResultUtil.successObj();
    }

    @ApiOperation(value="完成提现", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="params", value = "加密后的json\n" +
                    "{\n" +
                    "\ttxId:'账单id'【long】, \n" +
                    "\tamount: '转账金额str(10的18次方为1)'【string】, \n" +
                    "\tstatus: (1成功，2失败)【int】\n" +
                    "}", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/eosApi/completeWithdraw")
    public Object completeWithdraw(String params) {
        EthTransObj transInfo;
        try {
            String json = AesCBC.getInstance().simpleDecrypt(params, Constants.EOS_SERVER_SECRET);
            transInfo = CommonUtil.fromJson(json, EthTransObj.class);
        } catch (Exception e) {
            log.error("提现解密出现错误");
            return ResultUtil.error("解密出现错误", 500);
        }
        transInfo.setCoinType(3);
        depositWithdrawService.completeWithdraw(transInfo);
        return ResultUtil.successObj();
    }

}
