package io.mtc.facade.backend.feignController;

import io.mtc.common.constants.Constants;
import io.mtc.common.constants.TransactionConstants;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.backend.feign.ServiceEndpointEth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 节点信息
 *
 * @author Chinhin
 * 2018/7/19
 */
@RequestMapping("/endpoint")
@RestController
public class EndpointController {

    private static final BigInteger GAS_AMOUNT = new BigInteger("2040927");

    @Value("${create-contract-address}")
    private String createContractAddress;

    @Value("${deposit-address}")
    private String depositAddress;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ServiceEndpointEth serviceEndpointEth;

    @GetMapping
    public Object endpointInfo() {
        Map<String, Object> result = new HashMap<>();
        // 获取公网区块高度
        int pubBlockNum = 0;
        Object pubBlockNumObj = redisUtil.get(RedisKeys.ETH_LAST_BLOCK_NUM);
        if (pubBlockNumObj != null) {
            pubBlockNum = (int) pubBlockNumObj;
        }
        result.put("pubBlockNum", pubBlockNum);

        BigInteger createContractAccountBalance = getBalance(createContractAddress, Constants.ETH_ADDRESS);
        BigInteger hostAccountBalance = getBalance(depositAddress, Constants.ETH_ADDRESS);
        BigDecimal createContractBalanceFormat = CommonUtil.getFormatAmount(createContractAccountBalance.toString());
        result.put("createContractAccountBalance", createContractBalanceFormat);
        // 还可以创建多少次
        result.put("createContractTimes", getCreateContractTimes(createContractBalanceFormat));
        result.put("hostAccountBalance", CommonUtil.getFormatAmount(hostAccountBalance.toString()));

        // 获取各个节点的区块高度
        Set<String> endpoints = redisUtil.getKeysBeginWith(RedisKeys.ETH_ENDPOINT_BLOCK_NUMBER_PREFIX);
        List<Map> endpointInfo = new ArrayList<>();
        for (String temp : endpoints) {
            Integer endpointBlockNum = redisUtil.get(temp, Integer.class);
            if (endpointBlockNum == null) {
                redisUtil.delete(temp);
                continue;
            }
            int endpointIndex = Integer.parseInt(temp.substring(RedisKeys.ETH_ENDPOINT_BLOCK_NUMBER_PREFIX.length()));
            Map<String, Object> map = new HashMap<>();
            map.put("index", endpointIndex);
            map.put("blockNum", endpointBlockNum);

            Object rebootTimeObj = redisUtil.get(RedisKeys.ETH_ENDPOINT_PRE_REBOOT_TIME(endpointIndex));
            if (rebootTimeObj == null) {
                map.put("rebootTime", "未重启过");
            } else {
                map.put("rebootTime", DateUtil.formatDate((Date) rebootTimeObj, "MM/dd HH:mm:ss"));
            }
            endpointInfo.add(map);
        }
        result.put("endpointInfo", endpointInfo);
        return ResultUtil.successObj(result);
    }

    private BigInteger getBalance(String walletAddress, String contractAddress) {
        String redisKey = RedisKeys.ETH_CONTRACT_BALANCE(walletAddress, contractAddress);
        Object balanceObj = redisUtil.get(redisKey);
        if (balanceObj == null) {
            return serviceEndpointEth.balance(walletAddress, contractAddress);
        } else {
            return (BigInteger) balanceObj;
        }
    }

    @GetMapping("/instance")
    public Object instance() {
        return ResultUtil.successObj(redisUtil.get(RedisKeys.SERVER_INSTANCE_INFO));
    }

    /**
     * 计算还可以创建多少次合约
     * @param createContractBalance 创建合约的钱包的余额
     * @return 次数
     */
    private int getCreateContractTimes(BigDecimal createContractBalance) {
        BigInteger gasPrice = redisUtil.get(RedisKeys.GAS_PRICE, BigInteger.class);
        if (gasPrice == null) {
            return 0;
        }
        BigInteger gasUsed = TransactionConstants.getUseGasPrice(gasPrice.multiply(GAS_AMOUNT));
        BigDecimal divide = createContractBalance.divide(
                CommonUtil.getFormatAmount(gasUsed.toString()), 0, BigDecimal.ROUND_DOWN);
        return divide.intValue();
    }

}
