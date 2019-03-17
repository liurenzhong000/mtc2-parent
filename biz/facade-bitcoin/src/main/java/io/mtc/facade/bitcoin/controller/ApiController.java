package io.mtc.facade.bitcoin.controller;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.facade.bitcoin.service.BitcoinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 接口的请求类
 *
 * @author Chinhin
 * 2018/12/10
 */
@Api(description="比特系", tags = {"钱包接口"})
@RestController
public class ApiController {

    @Resource
    private BitcoinService bitcoinService;

//    @ApiOperation(value="动态信息")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String")
//    })
//    @GetMapping("/{bitcoinType}/dynamicInfo")
//    public Object dynamicInfo(@PathVariable BitcoinTypeEnum bitcoinType, String address) {
//        return bitcoinService.dynamicInfo(bitcoinType, address);
//    }
//
//    @ApiOperation(value="获取余额")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String"),
//    })
//    @GetMapping("/{bitcoinType}/balance/{address}")
//    public Object balance(@PathVariable BitcoinTypeEnum bitcoinType, @PathVariable String address) {
//        return bitcoinService.balance(bitcoinType, address);
//    }
//
//    @ApiOperation(value="获取交易详情")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "txHash", value = "交易hash", required = true, dataType = "String"),
//    })
//    @GetMapping("/{bitcoinType}/tx/{txHash}")
//    public Object txDetail(@PathVariable BitcoinTypeEnum bitcoinType, @PathVariable String txHash) {
//        return bitcoinService.txDetail(bitcoinType, txHash);
//    }
//
//    @ApiOperation(value="获取交易一览", notes = "item的字段与详情一致")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
//            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
//            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
//            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String"),
//    })
//    @GetMapping("/{bitcoinType}/tx")
//    public Object listTx(@PathVariable BitcoinTypeEnum bitcoinType, String address, Integer pageNumber, Integer pageSize, String order, String sort) {
//        return bitcoinService.listTx(bitcoinType, address, pageNumber, pageSize, order, sort);
//    }
//
//    @ApiOperation(value="获取UTXO一览")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String"),
//    })
//    @GetMapping("/{bitcoinType}/utxo/{address}")
//    public Object listUTXO(@PathVariable BitcoinTypeEnum bitcoinType, @PathVariable String address) {
//        return bitcoinService.listUTXO(bitcoinType, address);
//    }
//
//    @ApiOperation(value="发送交易")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bitcoinType", value = "BTC或BCH", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "hex", value = "签名后的交易信息", required = true, dataType = "String"),
//            @ApiImplicitParam(name = "remark", value = "备注", required = true, dataType = "String"),
//    })
//    @PostMapping("/{bitcoinType}/tx")
//    public Object sendTransaction(@PathVariable BitcoinTypeEnum bitcoinType, String hex, String remark) {
//        return bitcoinService.sendTransaction(bitcoinType, hex, remark);
//    }

    @ApiOperation(value="获取一个Omni/btc地址")
    @GetMapping("/{bitcoinType}/newAddress")
    public Object listUTXO(@PathVariable BitcoinTypeEnum bitcoinType) {
        return bitcoinService.getNewAddress(bitcoinType);
    }

}