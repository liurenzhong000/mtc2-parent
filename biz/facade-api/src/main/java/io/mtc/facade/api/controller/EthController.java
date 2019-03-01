package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSONArray;
import io.mtc.common.constants.MTCError;
import io.mtc.common.dto.EthereumRequest;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.api.feign.ServiceEndpointEth;
import io.mtc.facade.api.feign.ServiceNotification;
import io.mtc.facade.api.service.EthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 以太坊接口
 *
 * @author Chinhin
 * 2018/6/17
 */
@Slf4j
@RestController
@Api(description="以太坊", tags = {"其他"})
public class EthController {

    @Resource
    private EthService ethService;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private ServiceNotification serviceNotification;

    @Resource
    private RateCacheUtil rateCacheUtil;

    @ApiOperation(value="获取人民币兑美元汇率")
    @GetMapping("/rate")
    public Object rate() {
        return ResultUtil.successObj(rateCacheUtil.getUSD2CNY());
    }

    @ApiOperation(value="首页币种一览【2018.9.10开始废弃】")
    @ApiImplicitParam(name = "address", value = "余额地址", required = true, dataType = "String")
    @GetMapping("/currency/{address}")
    public Object appList(@PathVariable String address) {
        if (StringUtil.isBlank(address)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        address = address.toLowerCase();
        // 获得币种一览列表
        JSONArray list = ethService.getCurrencyList(1);
        Map<String, Object> tempAddressInfo = ethService.wallectInfo(address, list);
        return ResultUtil.successObj(tempAddressInfo);
    }

    @ApiOperation(value="首页币种初始化接口")
    @ApiImplicitParam(name="type", value = "主链类型(1:ETH, 2:BCH, 3:EOS，4:BTC)", required = true, dataType = "int")
    @GetMapping("/currency/init")
    public Object indexInit(Integer type) {
        return ResultUtil.successObj(ethService.getCurrencyList(type));
    }

    @ApiOperation(value="代币动态信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name = "tokenAddressesWithComma", value = "逗分的合约地址", required = true, dataType = "String")
    })
    @GetMapping("/currency/dynamicInfo")
    public Object tokensDynamicInfo(String walletAddress, String tokenAddressesWithComma) {
        if (StringUtil.isBlank(walletAddress) || StringUtil.isBlank(tokenAddressesWithComma)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        walletAddress = walletAddress.toLowerCase();
        tokenAddressesWithComma = tokenAddressesWithComma.toLowerCase();

        List<Object> result = new ArrayList<>();
        for (String temp : tokenAddressesWithComma.split(",")) {
            result.add(ethService.tokenDynamicInfo(temp, walletAddress));
        }
        return ResultUtil.successObj(result);
    }

    @ApiOperation(value="获取多个地址的币种信息【2018.9.10开始废弃】")
    @ApiImplicitParam(name = "addressesWithComma", value = "多个余额用半角逗号隔开", required = true, dataType = "String")
    @PostMapping("/multiAddressInfo")
    public Object multiAddressInfo(String addressesWithComma) {
        if (StringUtil.isBlank(addressesWithComma)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        addressesWithComma = addressesWithComma.toLowerCase();
        String[] addresses = addressesWithComma.split(",");
        // 获得币种一览列表
        JSONArray list = ethService.getCurrencyList(1);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String temp : addresses) {
            String listStr = CommonUtil.toJson(list);
            JSONArray clone = CommonUtil.fromJson(listStr, JSONArray.class);
            result.add(ethService.wallectInfo(temp, clone));
        }
        return ResultUtil.successObj(result);
    }

    @ApiOperation(
            value="批量设置钱包语言",
            notes="注意每次更换语言或者手机端添加钱包地址后<strong>【必须】调用, 否则收不到推送</strong>"
            )
    @ApiImplicitParams({
            @ApiImplicitParam(name="addressesWithComma", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="langCode", value = "语言编号(1:英文, 2:中文, 3:韩文)", required = true, dataType = "int")
    })
    @PostMapping("/setLanguage")
    public Object setLanguage(String addressesWithComma, int langCode) {
        if (StringUtil.isBlank(addressesWithComma)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        addressesWithComma = addressesWithComma.toLowerCase();
        String[] addresses = addressesWithComma.split(",");

        for (String walletAddress : addresses) {
            ethService.setLanguage(walletAddress, langCode);
        }
        return ResultUtil.successObj("Success");
    }

    @ApiOperation(value="调用以太坊通用接口", notes="对应老版本的 '/rpc/api'，目前只是为了前端走通转账流程，而交易记录会在后面开发出来")
    @ApiImplicitParam(name = "ethereumDTO", value = "请求实体", required = true, dataType = "EthereumRequest")
    @PostMapping("/ethApi")
    public Object ethApi(@RequestBody EthereumRequest ethereumDTO) {
        return CommonUtil.jsonToMap(serviceEndpointEth.ethApi(ethereumDTO));
    }

    @ApiOperation(value="消息列表", notes="item说明：\n" +
            "address: 推送目标地址\n" +
            "title: 标题\n" +
            "content: 内容\n" +
            "type: 通知类型 1:交易通知, 2:后台推送通知\n" +
            "url：链接地址(type为2有可能有值)\n" +
            "txHash：交易hash(type为1有值)\n" +
            "isSender: 是否付款方\n" +
            "otherAddress：交易的另一方钱包地址 \n")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "类型：1:交易通知, 2:后台推送通知，默认不限", dataType = "String"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String"),
    })
    @GetMapping("/notificationHistory")
    public Object notificationHistory(String walletAddress, Integer type, Integer pageNumber, Integer pageSize,
                       String order, String sort) {
        if (StringUtil.isBlank(walletAddress)) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        walletAddress = walletAddress.toLowerCase();

        if (StringUtil.isBlank(order)) {
            order = "DESC";
            sort = "createTime";
        }

        String result = serviceNotification.select(walletAddress, type, pageNumber, pageSize, order, sort);
        return CommonUtil.jsonToMap(result);
    }

}
