package io.mtc.service.endpoint.eth.controller;

import io.mtc.common.constants.Constants;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.service.endpoint.eth.service.Web3jService;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.exceptions.ContractCallException;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * 余额
 *
 * @author Chinhin
 * 2018/6/21
 */
@Slf4j
@RestController
public class BalanceController {

    @Resource
    private Web3jPool web3jPool;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private Web3jService web3jService;

    @GetMapping("/balance")
    public BigInteger getBalance(String walletAddress, String contractAddress) {

        BigInteger balance = null;
        // 以太坊
        if ("0".equals(contractAddress)) {
            Web3j connection = null;
            try {
                connection = web3jPool.getConnection();
                balance = connection.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().getBalance();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    web3jPool.close(connection);
                }
            }
        } else { // 合约

            Function function = new Function("balanceOf",
                    Collections.singletonList(new Address(walletAddress)),
                    Collections.singletonList(new TypeReference<Uint256>() {
                    }));
            String data = FunctionEncoder.encode(function);

            EthCall ethCall = null;
            Web3j connection = null;
            try {
                connection = web3jPool.getConnection();
                ethCall = connection.ethCall(Transaction.createEthCallTransaction(walletAddress, contractAddress, data),
                        DefaultBlockParameterName.LATEST).send();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    web3jPool.close(connection);
                }
            }
            if (ethCall == null) {
                log.error("BalanceController.getBalance-contract: Request Balance failure");
            } else {
                List<Type> types = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                if (types != null && !types.isEmpty()) {
                    Type result = types.get(0);
                    if (result == null) {
                        throw new ContractCallException("Empty value (0x) returned from contract");
                    }
                    balance = (BigInteger) result.getValue();
                }
            }
        }

        if (balance == null) {
            balance = BigInteger.ZERO;
        } else {
            Integer blockNumber = redisUtil.get(RedisKeys.ETH_ENDPOINT_BLOCK_NUMBER(web3jPool.getEndpointIndex()), Integer.class);
            if (blockNumber != null) {
                String redisKey = RedisKeys.ETH_CONTRACT_BALANCE(walletAddress, contractAddress);
                Integer cacheBalanceThreshold = redisUtil.get(RedisKeys.CACHE_BALANCE_THRESHOLD(redisKey), Integer.class);
                // 缓存余额阈值不存在，或者小于节点区块高度时，才缓存余额
                if (cacheBalanceThreshold == null || blockNumber > cacheBalanceThreshold) {
                    redisUtil.set(RedisKeys.SET_BALANCE_BLOCK(redisKey), blockNumber);
                    Integer balanceDecimals = redisUtil.get(RedisKeys.DECIMALS_TOKEN(contractAddress), Integer.class);
                    if (balanceDecimals == null) {
                        balanceDecimals = getBalanceDecimals(contractAddress);
                    }
                    balance = CommonUtil.balanceCorrect(balance, balanceDecimals);
                    redisUtil.set(redisKey, balance);
                }
            }
        }
        return balance;
    }

    /***
     * 获取代币的余额精度
     * @param contractAddress 代币地址
     * @return 结果
     */
    @GetMapping("/balanceDecimals")
    public Integer getBalanceDecimals(String contractAddress) {
        int decimals = 18;
        if (!Constants.ETH_ADDRESS.equals(contractAddress)){
            decimals = web3jService.getIntInfoByAddress("decimals", contractAddress);
        }
        redisUtil.set(RedisKeys.DECIMALS_TOKEN(contractAddress), decimals);
        return decimals;
    }

}
