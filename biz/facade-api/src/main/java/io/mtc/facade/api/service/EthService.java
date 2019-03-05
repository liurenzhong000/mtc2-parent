package io.mtc.facade.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.NumberUtil;
import io.mtc.facade.api.feign.ServiceCurrency;
import io.mtc.facade.api.feign.ServiceEndpointEth;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 以太坊service
 *
 * @author Chinhin
 * 2018/6/22
 */
@Service
public class EthService {

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @Resource
    private ServiceCurrency serviceCurrency;

    @Resource
    private RedisUtil redisUtil;

    public BigInteger getBalance(String walletAddress, String contractAddress) {
        String redisKey = RedisKeys.ETH_CONTRACT_BALANCE(walletAddress, contractAddress);
        Object balanceObj = redisUtil.get(redisKey);
        if (balanceObj == null) {
            return serviceEndpointEth.balance(walletAddress, contractAddress);
        } else {
            return (BigInteger) balanceObj;
        }
    }

    /**
     * 设置用户的语言
     * @param walletAddress 钱包地址
     * @param langCode 1:英文, 2:中文, 3:韩文
     */
    public void setLanguage(String walletAddress, int langCode) {
        // 记录此钱包地址为平台用户的钱包地址
        redisUtil.set(RedisKeys.PLATFORM_USER(walletAddress), langCode);
    }

    /**
     * 获得钱包地址的语音
     * @param walletAddress 钱包地址
     * @return 与上相对应
     */
    public int getLanguage(String walletAddress) {
        return (int) redisUtil.get(RedisKeys.PLATFORM_USER(walletAddress));
    }

    /**
     * 获取币种一览
     * @param type 基链类型(1:ETH[默认], 2:BTC, 3:EOS)
     * @return 币种一览
     */
    public JSONArray getCurrencyList(Integer type) {
        Map result;
        // 币种一览
        Object listObj = redisUtil.get(RedisKeys.APP_HOME_CURRENCY);
        if (listObj != null) {
            result = (Map) listObj;
        } else {
            String s = serviceCurrency.appList();
            result = CommonUtil.fromJson(s, HashMap.class);
            // 5秒钟刷新一次缓存
            redisUtil.set(RedisKeys.APP_HOME_CURRENCY, result, 5);
        }
        JSONArray resultArray = new JSONArray();
        JSONArray target = (JSONArray) result.get("result");
        for (Object temp : target) {
            JSONObject tempJson = (JSONObject) temp;
            if (tempJson.getIntValue("baseType") == type) {
                resultArray.add(tempJson);
            }
        }
        return resultArray;
    }

    /**
     * 获取以太坊的价格
     * @return 价格(美元)
     */
    public BigDecimal getEthPrice() {
        Object ethPrice = redisUtil.get(RedisKeys.ETH_PRICE);
        if (ethPrice != null) {
            return (BigDecimal)ethPrice;
        } else {
            BigDecimal ethPriceValue = serviceCurrency.getEthPrice();
            redisUtil.set(RedisKeys.ETH_PRICE, ethPriceValue, 5);
            return ethPriceValue;
        }
    }

    /**
     * 根据钱包地址获得 币种一览及对应的余额
     * @param address 钱包地址
     * @param list 币种一览
     * @return 结果
     */
    public Map<String, Object> wallectInfo(String address, JSONArray list) {
        BigDecimal totalMoney = new BigDecimal(0);
        BigDecimal totalCnyMoney = new BigDecimal(0);
        for (Object temp : list) {
            JSONObject tempJson = (JSONObject) temp;
            tempJson.remove("id");
            tempJson.remove("isEnabled");
            tempJson.remove("updateTime");
            tempJson.remove("createTime");
            tempJson.remove("sourceType");
            // 代币余额（代币数量）
            BigInteger balance = getBalance(address, tempJson.getString("address"));
            tempJson.put("balance", balance);
            // 币种价格
            BigDecimal cnyPrice = tempJson.getBigDecimal("cnyPrice");
            BigDecimal price = tempJson.getBigDecimal("price");

            BigDecimal balanceDeci = CommonUtil.getFormatAmount(balance.toString());
            // 币种金额
            BigDecimal cnyMoney = cnyPrice.multiply(balanceDeci);
            BigDecimal money = price.multiply(balanceDeci);
            tempJson.put("money", NumberUtil.scale2(money));
            tempJson.put("cnyMoney", NumberUtil.scale2(cnyMoney));
            // 总额计算
            totalMoney = totalMoney.add(money);
            totalCnyMoney = totalCnyMoney.add(cnyMoney);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("address", address);
        result.put("list", list);
        result.put("totalMoney", NumberUtil.scale2(totalMoney));
        result.put("totalCnyMoney", NumberUtil.scale2(totalCnyMoney));
        return result;
    }

    /**
     * 获得单个代币的动态信息（价格&余额）
     * @param tokenAddress 代币地址
     * @param walletAddress 钱包地址
     * @return 结果
     */
    public Object tokenDynamicInfo(String tokenAddress, String walletAddress) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("address", tokenAddress);
        // 币种价格
        BigDecimal cnyPrice = redisUtil.get(RedisKeys.ETH_TOKEN_CNY_PRICE(tokenAddress), BigDecimal.class);
        if (cnyPrice == null) {
            cnyPrice = BigDecimal.ZERO;
        }
        BigDecimal price = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(tokenAddress), BigDecimal.class);
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        BigDecimal changeHourly = redisUtil.get(RedisKeys.ETH_TOKEN_CHANGE(tokenAddress), BigDecimal.class);
        if (changeHourly == null) {
            changeHourly = BigDecimal.ZERO;
        }
        temp.put("price", price);
        temp.put("cnyPrice", cnyPrice);
        temp.put("changeHourly", changeHourly);

        BigInteger balance = getBalance(walletAddress, tokenAddress);
        temp.put("balance", balance);

        BigDecimal balanceDeci = CommonUtil.getFormatAmount(balance.toString());
        // 币种金额
        BigDecimal cnyMoney = cnyPrice.multiply(balanceDeci);
        BigDecimal money = price.multiply(balanceDeci);
        temp.put("money", money);
        temp.put("cnyMoney", cnyMoney);
        temp.put("baseType", 1);
        return temp;
    }

}