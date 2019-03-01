package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.MTCError;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.api.feign.ServiceEndpointEth;
import io.mtc.facade.api.feign.ServiceTransEth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易相关接口
 *
 * @author Chinhin
 * 2018/6/27
 */
@Slf4j
@RestController
@Api(description="以太坊", tags = {"交易相关"})
public class TransactionController {

    @Resource
    private ServiceTransEth serviceTransEth;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private RedisUtil redisUtil;

    @ApiOperation(value="交易记录", notes="获取交易记录, item说明：\n" +
            "txHash: 交易hash\n" +
            "status: 状态(0未确认，1成功，2失败)\n" +
            "tokenCounts: 转账代币数量，eth交易为空\n" +
            "shortName: 币种单位（与后台设置的一致）\n" +
            "from：付款方\n" +
            "to：收款方\n" +
            "value: 转账ether数量，代币交易为空\n" +
            "createTime：录入系统时间(爬取入库或转账入库时间) \n" +
            "times：交易时间(一般展现这个)\n" +
            "blockHash：区块hash\n" +
            "blockNumber：区块号\n" +
            "contractAddress：合约地址，以太坊为'0'\n" +
            "\n"+
            "---------------------------- ↓附加信息↓ ----------------------------\n" +
            "\n"+
            "isMadeBySchedule：是否通过爬取获得(false表示通过平台转账创建) \n" +
            "isPlatformUser：是否平台用户的交易记录\n" +
            "nonce: nonce \n" +
            "input: eth交易一般为0x，而合约交易则包含了收款方及代币数量\n" +
            "transactionIndex:在block中的position\n" +
            "remark: 备注，爬取的记录为空 \n" +
            "type:类型（1:Ether, 2:Mesh），爬取固定为1 \n" +
            "\n"+
            "---------------------------- ↓矿工费相关↓ ----------------------------\n" +
            "\n"+
            "actualCostFee：实际花费的矿工费\n" +
            "gas：gas \n" +
            "meshGas: 消耗的gas \n" +
            "gasPrice：gas价格 \n" +
            "cumulativeGasUsed：累积消耗gas；"
            )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name = "contractAddress", value = "合约地址,默认所有 ", dataType = "String"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String"),
    })
    @GetMapping("/trans")
    public Object list(String walletAddress, String contractAddress, Integer pageNumber, Integer pageSize,
                       String order, String sort) {
        if (StringUtil.isBlank(walletAddress)) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        walletAddress = walletAddress.toLowerCase();
        if (StringUtil.isNotBlank(contractAddress)) {
            contractAddress = contractAddress.toLowerCase();
        }

        String result = serviceTransEth.list(walletAddress, contractAddress, pageNumber, pageSize, order, sort);
        HashMap resultMap = CommonUtil.fromJson(result, HashMap.class);
        Map listMap = (Map) resultMap.get("result");
        JSONArray list = (JSONArray) listMap.get("list");
        for (Object temp : list) {
            JSONObject tempJson = (JSONObject) temp;
            Object hash = tempJson.get("hash");
            tempJson.put("txHash", hash);
            tempJson.remove("hash");
        }
        return resultMap;
    }

    @ApiOperation(value="区块高度", notes="获取全网最新区块高度")
    @GetMapping("/blockNum")
    public Object blockNum() {
        return ResultUtil.successObj(redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM));
    }

    @ApiOperation(value="获取交易详情")
    @ApiImplicitParam(name = "txHash", value = "交易hash", required = true, dataType = "String")
    @GetMapping("/detail/{txHash}")
    public Object detail(@PathVariable("txHash") String txHash) {
        if (StringUtil.isBlank(txHash)) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        txHash = txHash.toLowerCase();
        String detail = serviceTransEth.detail(txHash);
        Map<String, Object> detailMap = CommonUtil.jsonToMap(detail);
        JSONObject resultMap = (JSONObject) detailMap.get("result");
        if (resultMap != null) {
            Object hash = resultMap.get("hash");
            resultMap.put("txHash", hash);
            resultMap.remove("hash");
        }
        return detailMap;
    }

    @ApiOperation(value="获取交易的count")
    @ApiImplicitParam(name = "address", value = "钱包地址", required = true, dataType = "String")
    @GetMapping("/getTransactionCount/{address}")
    public Object getTransactionCount(@PathVariable("address") String address) {
        return ResultUtil.successObj(serviceEndpointEth.getTransactionCount(address));
    }

}
