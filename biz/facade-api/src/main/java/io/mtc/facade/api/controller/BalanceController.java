package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.MTCError;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.NumberUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.api.service.EthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余额相关
 *
 * @author Chinhin
 * 2018/6/22
 */
@Api(description="以太坊", tags = {"余额相关"})
@RestController
public class BalanceController {

    @Resource
    private EthService ethService;

    @ApiOperation(value="获取余额", notes="查询eth的时候地址传0")
    @ApiImplicitParams({
            @ApiImplicitParam(name="walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="contractAddress", value = "合约地址", required = true, dataType = "String")
    })
    @GetMapping("/balance")
    public Object balance(String walletAddress, String contractAddress) {
        if (StringUtil.isBlank(walletAddress)
                || StringUtil.isBlank(contractAddress)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        BigInteger balance = ethService.getBalance(walletAddress.toLowerCase(), contractAddress.toLowerCase());
        return ResultUtil.successObj(balance);
    }

    @ApiOperation(value="获取多个地址的余额【2018.9.10开始废弃】")
    @ApiImplicitParam(name = "addressesWithComma", value = "多个余额用半角逗号隔开", required = true, dataType = "String")
    @PostMapping("/totalMoney")
    public Object totalMoney(String addressesWithComma) {
        if (StringUtil.isBlank(addressesWithComma)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        addressesWithComma = addressesWithComma.toLowerCase();
        String[] addresses = addressesWithComma.split(",");

        JSONArray list = ethService.getCurrencyList(1);
        List<Map<String, Object>> resultList = new ArrayList<>();

        BigDecimal totalMoney = new BigDecimal(0);
        BigDecimal totalCnyMoney = new BigDecimal(0);

        // 以太币的价格
        BigDecimal ethPrice = ethService.getEthPrice();

        for (String address : addresses) {
            if (StringUtil.isBlank(address)) {
                continue;
            }
            BigDecimal addressMoney = new BigDecimal(0);
            BigDecimal addressCnyMoney = new BigDecimal(0);
            for (Object temp : list) {
                JSONObject tempJson = (JSONObject) temp;

                // 代币余额（代币数量）
                BigInteger balance = ethService.getBalance(address, tempJson.getString("address"));
                // 币种价格
                BigDecimal cnyPrice = tempJson.getBigDecimal("cnyPrice");
                BigDecimal price = tempJson.getBigDecimal("price");

                BigDecimal balanceDeci = CommonUtil.getFormatAmount(balance.toString());
                // 币种金额
                BigDecimal cnyMoney = cnyPrice.multiply(balanceDeci);
                BigDecimal money = price.multiply(balanceDeci);
                // 总额计算
                addressMoney = addressMoney.add(money);
                addressCnyMoney = addressCnyMoney.add(cnyMoney);
            }
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("address", address);
            tempMap.put("addressMoney", NumberUtil.scale2(addressMoney));
            tempMap.put("addressCnyMoney", NumberUtil.scale2(addressCnyMoney));
            resultList.add(tempMap);
            // 资产等于多少以太币
            BigDecimal ethAmount = addressMoney.divide(ethPrice, 5, RoundingMode.DOWN);
            tempMap.put("ethAmount", ethAmount);

            totalMoney = totalMoney.add(addressMoney);
            totalCnyMoney = totalCnyMoney.add(addressCnyMoney);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("addresses", resultList);
        result.put("totalMoney", NumberUtil.scale2(totalMoney));
        result.put("totalCnyMoney", NumberUtil.scale2(totalCnyMoney));
        result.put("totalEthAmount", totalMoney.divide(ethPrice, 5, RoundingMode.DOWN));
        return ResultUtil.successObj(result);
    }

}
