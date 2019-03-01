package io.mtc.facade.user.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.user.service.BalanceService;
import io.mtc.facade.user.service.FundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * 资金相关
 *
 * @author Chinhin
 * 2018/7/27
 */
@Api(description="交易相关", tags = {"资金交易"})
@Transactional(readOnly = true)
@RequestMapping("/fund")
@RestController
public class FundController {

    @Resource
    private FundService fundService;

    @Resource
    private BalanceService balanceService;

    @ApiOperation(value="获取所有托管代币的余额", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long")
    })
    @GetMapping
    public Object allCurrency(@RequestHeader Long uid) {
        return fundService.allCurrency(uid);
    }

    @ApiOperation(value="托管代币账单", tags = {"需要token"}, notes = "<b>账单item:</b> \n" +
            "    id: 账单id,\n" +
            "    income: 入账(wei),\n" +
            "    outcome: 支出(wei)【提现的时候包含了手续费】,\n" +
            "    outComeFee: 提现手续费(wei),\n" +
            "    refund: 退款金额(wei),\n" +
            "    txHash: 交易hash (充值||提现的时候有值),\n" +
            "    relatedAddress: \n" +
            "    　①type为充值：充值的钱包地址,\n" +
            "    　②type为提现：提现的钱包地址,\n" +
            "    　③type为发币：入账钱包地址,\n" +
            "    note: \n" +
            "    　①type为收红包：发红包的用户昵称,\n" +
            "    　②type为转账：交易另一方的手机号或邮箱,\n" +
            "    　③type为发币：合约名,\n" +
            "    note2: \n" +
            "    　①type为转账：交易另一方的头像，\n" +
            "    relativeId: \n" +
            "    　①type为收发红包：红包id,\n" +
            "    　②type为发币：发币id,\n" +
            "    type: 账单类型 (\n" +
            "    　DEPOSIT：充值\n" +
            "    　WITHDRAW：提现\n" +
            "    　SEND_RED_ENVELOPE：发红包\n" +
            "    　GRAB_RED_ENVELOPE：收红包\n" +
            "    　TRANSFER_FROM：转账-转出\n" +
            "    　TRANSFER_TO：转账-收款\n" +
            "    　CREATE_CONTRACT：发币(创建合约)\n" +
            "    　TRANSFER_PAY：云矿账户支付\n" +
            "    　TRANSFER_WITHDRAW：云矿账户提现\n" +
            "    　つづく...\n" +
            "    )\n" +
            "    status: 状态 (\n" +
            "    　PENDING：排队中(只有提现才有)\n" +
            "    　PROCESSING：处理中\n" +
            "    　SUCCESS：成功\n" +
            "    　FAILURE：失败\n" +
            ")")
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer"),
            @ApiImplicitParam(name="yearMonth", value = "过滤时间，格式为yyyy/MM(可不传)", dataType = "String"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @GetMapping("/{currencyAddress}")
    public Object bills(@RequestHeader Long uid, @PathVariable String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType, String yearMonth,
                         @ModelAttribute PagingModel pageModel) {
        return fundService.bills(uid, currencyAddress, currencyType, yearMonth, pageModel);
    }

    @ApiOperation(value="充值,成功的时候返回账单id,再用此id去作为转账参数转账", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", required = true, dataType = "int"),
            @ApiImplicitParam(name="toAddress", value = "收款方地址, 暂为 0x69701ea5fc87523ad4c324ff4e860f7c9b1dce96", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="nonce", value = "nonce", required = true, dataType = "BigInteger"),
            @ApiImplicitParam(name="income", value = "充值金额", required = true, dataType = "BigInteger"),
            @ApiImplicitParam(name="fromAddress", value = "from地址", required = true, dataType = "String"),
    })
    @PostMapping("/deposit")
    @Transactional
    public Object deposit(@RequestHeader Long uid, @RequestParam(defaultValue = "1") Integer currencyType, String toAddress, String currencyAddress,
                          BigInteger nonce, BigInteger income, String fromAddress) {
        return fundService.deposit(uid, currencyType, toAddress, currencyAddress, nonce, income, fromAddress);
    }

    @ApiOperation(value="更新订单的txHash", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="billId", value = "订单id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="txHash", value = "交易hash", required = true, dataType = "String"),
            @ApiImplicitParam(name="isSuccess", value = "打包是否成功", required = true, dataType = "boolean"),
    })
    @PostMapping("/depositCompensate")
    @Transactional
    public Object depositCompensate(@RequestHeader Long uid, Long billId, String txHash, boolean isSuccess) {
        return fundService.depositCompensate(uid, billId, txHash, isSuccess);
    }

    @ApiOperation(value="获得提现手续费", notes = "该手续费对应需要多少个代币", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer"),
    })
    @GetMapping("/withdrawFee")
    public Object withdrawFee(String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType) {
        return fundService.withdrawFee(currencyAddress, currencyType);
    }

    @Resource
    private ApplicationContext applicationContext;

    @ApiOperation(value="提现", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer"),
            @ApiImplicitParam(name="walletAddress", value = "提现的钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="amount", value = "提现金额(以wei为单位)", required = true, dataType = "String"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @PostMapping("/withdraw")
    @Transactional
    public Object withdraw(@RequestHeader Long uid, String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType, String walletAddress,
                           String amount, String fundPassword, Boolean isValidByPhone, String validCode) {
        // 非正式环境不能提现
//        if (!applicationContext.getEnvironment().getActiveProfiles()[0].equals("prod")) {
//            return ResultUtil.error("非正式环境不能提现");
//        }
        return fundService.withdraw(uid, currencyAddress, currencyType, walletAddress, amount, fundPassword, isValidByPhone, validCode);
    }

    @PostMapping("/withdrawAIP")
    @Transactional
    public Object withdrawAIP(@RequestParam(defaultValue = "1") Integer currencyType, Long uid, String walletAddress,
                           String amount, String fundPassword) {
         //非正式环境不能提现
        if (!applicationContext.getEnvironment().getActiveProfiles()[0].equals("prod")) {
           return ResultUtil.error("非正式环境不能提现");
        }
        return fundService.withdrawAIP(currencyType, uid, walletAddress, amount, fundPassword);
    }

    @ApiOperation(value="获取单个代币的托管账户的余额", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer")
    })
    @GetMapping("/currencyBalance")
    public Object currencyBalance(@RequestHeader Long uid, String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType) {
        return fundService.currencyBalance(uid, currencyAddress, currencyType);
    }

    @ApiOperation(value="转账", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer"),
            @ApiImplicitParam(name="target", value = "目标用户的手机号或邮箱地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="amount", value = "转账金额(以wei为单位)", required = true, dataType = "String"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
//            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
//            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @PostMapping("/transfer")
    @Transactional
    public Object transfer(@RequestHeader Long uid, String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType, String target, String amount,
                           String fundPassword) {// , Boolean isValidByPhone, String validCode
        return fundService.transfer(uid, currencyAddress, currencyType, target, amount, fundPassword); // , isValidByPhone, validCode
    }

    @ApiOperation(value="验证资金密码(应急用)", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
    })
    @GetMapping("/validateFundPassword")
    public Object validateFundPassword(@RequestHeader Long uid, String fundPassword) {
        return fundService.fundPasswordVerify(uid, fundPassword);
    }

    @ApiOperation(value="获取用户的专属钱包地址", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer")
    })
    @Transactional
    @GetMapping("/userWalletAddress")
    public Object userWalletAddress(@RequestHeader Long uid, @RequestParam(defaultValue = "1") Integer currencyType) {
        try {
            String walletAddress = balanceService.getWalletAddress(uid, currencyType);
            if (walletAddress == null) {
                return ResultUtil.error("分配钱包地址出现错误，请稍后再试", 500);
            } else {
                return ResultUtil.successObj(walletAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error(e.getMessage(), 500);
        }
    }

}
