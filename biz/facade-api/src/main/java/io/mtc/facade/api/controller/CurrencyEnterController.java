package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.MTCError;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.api.feign.ServiceCurrency;
import io.mtc.facade.api.service.CurrencyApplyService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 代币入驻
 *
 * @author Chinhin
 * 2018/8/13
 */
@Slf4j
@Api(description="以太坊", tags = {"代币"})
@Transactional(readOnly = true)
@RequestMapping("/currencyEnter")
@RestController
public class CurrencyEnterController {

    @Resource
    private ServiceCurrency serviceCurrency;

    @Resource
    private CurrencyApplyService currencyApplyService;

    @ApiOperation(value="添加token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "合约地址", required = true, dataType = "String")
    })
    @PutMapping
    public Object enter(String address) {
        if (StringUtil.isEmpty(address) || address.length() !=42 || !address.startsWith("0x")) {
            return ResultUtil.errorObj(MTCError.CONTRACT_ADDRESS_WRONG);
        }
        String name = currencyApplyService.getNameByAddress(address);
        if (name == null) {
            return ResultUtil.errorObj(MTCError.CURRENCY_NOT_EXIST);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", address);
        jsonObject.put("name", name);
        jsonObject.put("shortName", currencyApplyService.getSymbolByAddress(address));
        jsonObject.put("price", BigDecimal.ZERO);
        return CommonUtil.jsonToMap(serviceCurrency.apply(jsonObject.toJSONString()));
    }

    @ApiOperation(value="获取链上代币信息")
    @ApiImplicitParam(name = "address", value = "代币地址", required = true, dataType = "String")
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "symbol: 代币简称\n" +
                    "address: 代币地址\n" +
                    "name：代币名字")
    })
    @GetMapping("/info/{address}")
    public Object info(@PathVariable String address) {
        if (StringUtil.isEmpty(address) || address.length() !=42 || !address.startsWith("0x")) {
            return ResultUtil.errorObj(MTCError.CONTRACT_ADDRESS_WRONG);
        }
        String name = currencyApplyService.getNameByAddress(address);
        if (name == null) {
            return ResultUtil.errorObj(MTCError.CURRENCY_NOT_EXIST);
        }
        Map<String, String> result = new HashMap<>();
        result.put("address", address);
        result.put("name", name);
        result.put("symbol", currencyApplyService.getSymbolByAddress(address));
        return ResultUtil.successObj(result);
    }

}
