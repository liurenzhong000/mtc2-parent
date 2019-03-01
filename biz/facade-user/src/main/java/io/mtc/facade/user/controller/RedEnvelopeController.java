package io.mtc.facade.user.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.facade.user.service.RedEnvelopeService;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 红包控制器
 *
 * @author Chinhin
 * 2018/7/31
 */
@Api(description="红包相关", tags = {"红包"})
@RequestMapping("/redEnvelope")
@RestController
public class RedEnvelopeController {

    @Resource
    private RedEnvelopeService redEnvelopeService;

    @ApiOperation(value="获取可以发红包的币种一览", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="key", value = "关键字", dataType = "String"),
    })
    @GetMapping("/enabledCurrency")
    public Object enabledCurrency(String key) {
        return redEnvelopeService.enabledCurrency(key);
    }

    @ApiOperation(value="发红包: 成功时候，返回红包详情（具体格式参照sentHistory的item说明）", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyAddress", value = "代币地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", dataType = "Integer"),
            @ApiImplicitParam(name="amount", value = "等额传每份的金额，而拼手气传总金额（单位wei）", required = true, dataType = "String"),
            @ApiImplicitParam(name="num", value = "红包数量", required = true, dataType = "int"),
            @ApiImplicitParam(name="content", value = "祝福语，不传会报错。前端根据国际化来传", required = true, dataType = "String"),
            @ApiImplicitParam(name="type", value = "1拼手气, 2等额", required = true, dataType = "int")
    })
    @PutMapping("/send")
    public Object send(@RequestHeader Long uid, String fundPassword, String currencyAddress, @RequestParam(defaultValue = "1") Integer currencyType, String amount, int num, String content, int type) {
        return  redEnvelopeService.send(uid, fundPassword, currencyAddress, currencyType, amount, num, content, type);
    }

    @ApiOperation(value="用户发送的红包", tags = {"需要token"}, notes = "<b>红包item:</b> \n" +
            "    id: 红包id,\n" +
            "    isGrabbedOut: 是否抢光(boolean),\n" +
            "    currencyShortName: 代币简称,\n" +
            "    currencyImage: 代币图片,\n" +
            "    num: 红包发放数量,\n" +
            "    amount: 红包总金额(wei),\n" +
            "    currencyAddress: 红包代币地址,\n" +
            "    content: 红包祝福语,\n" +
            "    type: 红包类型(1:拼手气, 2:等额),\n" +
            "    status: 红包状态(1进行中，2暂停，3结束)为3的时候跳往明细页面, 为1||2的时候跳往发送页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @GetMapping("/sentHistory")
    public Object sentRedEnvelopes(@RequestHeader Long uid, @ModelAttribute PagingModel pageModel) {
        return redEnvelopeService.sendRedEnvelopes(uid, pageModel);
    }

    @ApiOperation(value="改变红包状态，返回改变后的状态", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "status", value = "要改变成什么状态(1进行中，2暂停，3结束-结束后不能再改回去)", required = true, dataType = "int"),
    })
    @PostMapping("/updateStatus")
    public Object updateEnvelopeStatus(@RequestHeader Long uid, Long envelopeId, int status) {
        return redEnvelopeService.updateEnvelopeStatus(uid, envelopeId, status);
    }

    @ApiOperation(value="收到红包,主要是让服务器增加一条收到的记录，没有返回信息", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @PutMapping("/receive")
    public Object receiveEnvelope(@RequestHeader Long uid, Long envelopeId) {
        return redEnvelopeService.receiveEnvelope(uid, envelopeId);
    }

    @ApiOperation(value="红包弹窗详情，收到蓝牙广播的红包ID后，先调用此接口，同时调用receive接口", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @GetMapping("/popDetail/{envelopeId}")
    public Object envelopePopDetail(@PathVariable Long envelopeId) {
        return redEnvelopeService.redEnvelopePopDetail(envelopeId);
    }

    @ApiOperation(value="红包发送情况，发送页面轮询用", tags = {"需要token"}, notes = "<b>红包item:</b> \n" +
            "    content: 红包祝福语,\n" +
            "    amount: 红包总金额(wei),\n" +
            "    currencyShortName: 代币简称,\n" +
            "    currencyAddress: 红包代币地址,\n" +
            "    currencyImage: 代币图片,\n" +
            "    num: 红包发放数量,\n" +
            "    status: 红包状态(1进行中，2暂停，3结束),\n" +
            "    grabbedAmount: 抢到的总额,\n" +
            "    grabbedNum：抢到的数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @GetMapping("/sendingInfo/{envelopeId}")
    public Object envelopeSendInfo(@PathVariable Long envelopeId) {
        return redEnvelopeService.sendEnvelopeInfo(envelopeId);
    }

    @ApiOperation(value="打开红包,返回抢到的金额，如果返回0表示没抢到", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @PostMapping("/open")
    public Object open(@RequestHeader Long uid, Long envelopeId) {
        return redEnvelopeService.grab(uid, envelopeId);
    }

    @ApiOperation(value="获取用户收到的红包一览", tags = {"需要token"}, notes = "<b>收到红包item:</b> \n" +
            "    amount: 抢到金额(wei),\n" +
            "    isOpened: 是否打开了boolean,\n" +
            "    redEnvelope: 参照红包item")
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @GetMapping("/receivedHistory")
    public Object receivedHistory(@RequestHeader Long uid, @ModelAttribute PagingModel pageModel) {
        return redEnvelopeService.receivedHistory(uid, pageModel);
    }

    @ApiOperation(value="红包领取明细", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @GetMapping("/detail/{envelopeId}")
    public Object envelopeDetail(@RequestHeader Long uid, @PathVariable Long envelopeId) {
        return redEnvelopeService.envelopeDetail(uid, envelopeId);
    }

    @ApiOperation(value="游客打开红包, 返回抢到的金额，如果返回0表示没抢到", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="deviceId", value = "设备id 200个字符以内", required = true, dataType = "String"),
            @ApiImplicitParam(name = "envelopeId", value = "红包id", required = true, dataType = "Long"),
    })
    @PostMapping("/guestOpen")
    public Object guestGrab(String deviceId, Long envelopeId) {
        return redEnvelopeService.guestGrab(deviceId, envelopeId);
    }

    @ApiOperation(value="游客抢到的红包一览", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id 200个字符以内", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段'", dataType = "String")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "redEnvelope: 红包对象\n" +
                    "amount: 抢到的金额\n" +
                    "createTime：抢到的时间（超过此时间60分钟显示 过期）")
    })
    @GetMapping("/guestGrabbedHistory")
    public Object guestGrabbedHistory(String deviceId, @ModelAttribute PagingModel pageModel) {
        return redEnvelopeService.guestGrabbedHistory(deviceId, pageModel);
    }

    @ApiOperation(value="认领红包，", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "deviceId", value = "设备id 200个字符以内", required = true, dataType = "String"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "successNum: 认领成功个数\n" +
                    "failureNum: 认领失败个数")
    })
    @PostMapping("/guestReclaim")
    public Object guestReclaim(@RequestHeader Long uid, String deviceId) {
        return redEnvelopeService.guestReclaim(deviceId, uid);
    }

}
