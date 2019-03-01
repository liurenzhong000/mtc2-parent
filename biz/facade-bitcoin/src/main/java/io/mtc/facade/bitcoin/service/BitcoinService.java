package io.mtc.facade.bitcoin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.bitcoin.data.entity.BchTxRemark;
import io.mtc.facade.bitcoin.data.entity.BtcTxRemark;
import io.mtc.facade.bitcoin.data.repository.BchTxRemarkRepository;
import io.mtc.facade.bitcoin.data.repository.BtcTxRemarkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 比特币服务
 *
 * @author Chinhin
 * 2019/1/10
 */
@Slf4j
@Service
public class BitcoinService {

    @Resource
    private RedisUtil redisUtil;

    @Value("${btc.api.url:http://47.74.154.247/api/}")
    private String btcUrl;

    @Value("${bch.api.url:http://47.74.233.202/api/}")
    private String bchUrl;

    @Resource
    private BtcTxRemarkRepository btcTxRemarkRepository;

    @Resource
    private BchTxRemarkRepository bchTxRemarkRepository;

    private String getApiUrl(BitcoinTypeEnum bitcoinType) {
        if (bitcoinType == BitcoinTypeEnum.BTC) {
            return btcUrl;
        } else {
            return bchUrl;
        }
    }

    public Object dynamicInfo(BitcoinTypeEnum bitcoinType, String address) {
        if (StringUtil.isBlank(address)){
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        String tokenAddress;
        if (bitcoinType == BitcoinTypeEnum.BTC) {
            tokenAddress = Constants.BTC_ADDRESS;
        } else {
            tokenAddress = Constants.BCH_ADDRESS;
        }

        List<Object> result = new ArrayList<>();

        Map<String, Object> temp = new HashMap<>();
        temp.put("address", tokenAddress);
        BigDecimal price = redisUtil.get(RedisKeys.ETH_TOKEN_PRICE(tokenAddress), BigDecimal.class);
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        // 币种价格
        BigDecimal cnyPrice = redisUtil.get(RedisKeys.ETH_TOKEN_CNY_PRICE(tokenAddress), BigDecimal.class);
        if (cnyPrice == null) {
            cnyPrice = BigDecimal.ZERO;
        }
        BigDecimal changeHourly = redisUtil.get(RedisKeys.ETH_TOKEN_CHANGE(tokenAddress), BigDecimal.class);
        if (changeHourly == null) {
            changeHourly = BigDecimal.ZERO;
        }
        temp.put("cnyPrice", cnyPrice);
        temp.put("price", price);
        temp.put("changeHourly", changeHourly);

        Object balanceObj = balance(bitcoinType, address);
        Map<String, Object> balanceMap = CommonUtil.jsonToMap(CommonUtil.toJson(balanceObj));
        BigDecimal balance = new BigDecimal((String) balanceMap.get("result"))
                .divide(new BigDecimal(Math.pow(10, 8)), 9, RoundingMode.HALF_UP);
        temp.put("balance", balance);

        // 币种金额
        BigDecimal money = price.multiply(balance);
        BigDecimal cnyMoney = cnyPrice.multiply(balance);
        temp.put("cnyMoney", cnyMoney);
        temp.put("money", money);
        temp.put("baseType", 4);

        result.add(temp);
        return ResultUtil.successObj(result);
    }

    /**
     * 获取余额
     */
    public Object balance(BitcoinTypeEnum bitcoinType, String address) {
        String url = getApiUrl(bitcoinType) + "addr/" + address + "/balance";
        return ResultUtil.successObj(HttpUtil.get(url));
    }

    /**
     * 交易详情
     */
    public Object txDetail(BitcoinTypeEnum bitcoinType, String txHash) {
        String url = getApiUrl(bitcoinType) + "tx/" + txHash;
        String resultStr = HttpUtil.get(url);
        JSONObject resultJSON = JSONObject.parseObject(resultStr);
        simplifyTx(resultJSON);

        String remark = Constants.EMPTY;
        if (bitcoinType == BitcoinTypeEnum.BTC) {
            BtcTxRemark btcTxRemark = btcTxRemarkRepository.findById(txHash).orElse(null);
            if (btcTxRemark != null) {
                remark = btcTxRemark.getRemark();
            }
        } else {
            BchTxRemark bchTxRemark = bchTxRemarkRepository.findById(txHash).orElse(null);
            if (bchTxRemark != null) {
                remark = bchTxRemark.getRemark();
            }
        }
        resultJSON.put("remark", remark);
        return ResultUtil.successObj(resultJSON);
    }

    /**
     * 交易一览
     */
    public Object listTx(BitcoinTypeEnum bitcoinType, String address, Integer pageNumber, Integer pageSize, String order, String sort) {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Integer from = (pageNumber - 1) * pageSize;
        Integer to = pageNumber * pageSize - 1;

        String url = getApiUrl(bitcoinType) + "addrs/" + address + "/txs?from=" + from + "&to=" + to;
        String resultStr = HttpUtil.get(url);
        JSONObject resultJSON = JSONObject.parseObject(resultStr);
        JSONArray list = resultJSON.getJSONArray("items");
        for (Object tempObj : list) {
            JSONObject temp = (JSONObject) tempObj;
            simplifyTx(temp);
        }

        // 分页信息
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", pageNumber);
        pageInfo.put("pageSize", pageSize);
        Integer totalItems = resultJSON.getInteger("totalItems");
        pageInfo.put("totalElements", totalItems);
        Integer totalPages = totalItems / pageSize;
        if (totalItems % pageSize != 0) {
            totalPages ++;
        }
        pageInfo.put("totalPages", totalPages);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", pageInfo);
        result.put("status", 200);
        result.put("timestamp", new Date().getTime());
        result.put("result", data);
        return result;
    }

    /**
     * utxo一览
     */
    public Object listUTXO(BitcoinTypeEnum bitcoinType, String address) {
        String url = getApiUrl(bitcoinType) + "addr/" + address + "/utxo";
        String resultStr = HttpUtil.get(url);
        JSONArray array = JSONArray.parseArray(resultStr);
        for (Object tempObj : array) {
            JSONObject temp = (JSONObject) tempObj;
            temp.remove("satoshis");
            temp.remove("scriptPubKey");
            temp.remove("height");
            temp.remove("confirmations");
        }
        return ResultUtil.successObj(array);
    }

    /**
     * 发送交易
     */
    public Object sendTransaction(BitcoinTypeEnum bitcoinType, String hex, String remark) {
        String url = getApiUrl(bitcoinType) + "tx/send";
        Map<String, String> param = new HashMap<>();
        param.put("rawtx", hex);
        String result = HttpUtil.post(url, param);
        log.info(result);
        Map<String, Object> resultMap = CommonUtil.jsonToMap(result);
        String txid = (String) resultMap.get("txid");

        if (bitcoinType == BitcoinTypeEnum.BTC) {
            BtcTxRemark btcTxRemark = new BtcTxRemark();
            btcTxRemark.setTxHash(txid);
            btcTxRemark.setRemark(remark);
            btcTxRemarkRepository.save(btcTxRemark);
        } else {
            BchTxRemark bchTxRemark = new BchTxRemark();
            bchTxRemark.setTxHash(txid);
            bchTxRemark.setRemark(remark);
            bchTxRemarkRepository.save(bchTxRemark);
        }
        return ResultUtil.successObj(txid);
    }

    private void simplifyTx(JSONObject temp) {
        temp.remove("locktime");
        temp.remove("blockhash");
//        temp.remove("confirmations");
        temp.remove("blocktime");
//        temp.remove("valueOut");
//        temp.remove("valueIn");
        for (Object vinObj : temp.getJSONArray("vin")) {
            JSONObject vin = (JSONObject) vinObj;
//            vin.remove("sequence");
            int vout = vin.getIntValue("vout");
            vin.remove("vout");
            vin.put("n", vout);
            vin.remove("scriptSig");
            vin.remove("valueSat");
            vin.remove("doubleSpentTxID");
            vin.remove("txid");
        }
        for (Object voutObj : temp.getJSONArray("vout")) {
            JSONObject vout = (JSONObject) voutObj;
            vout.remove("spentTxId");
            vout.remove("spentIndex");
            vout.remove("spentHeight");

            JSONObject scriptPubKey = vout.getJSONObject("scriptPubKey");
            JSONArray addresses = scriptPubKey.getJSONArray("addresses");
            vout.remove("scriptPubKey");

            String addressStr;
            if (addresses.size() == 1) {
                addressStr = addresses.getString(0);
            } else {
                addressStr = CommonUtil.toJson(addresses);
            }
            vout.put("addr", addressStr);
        }
    }

}
