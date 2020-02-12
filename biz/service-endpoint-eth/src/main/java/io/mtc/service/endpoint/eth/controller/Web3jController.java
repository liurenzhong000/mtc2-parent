package io.mtc.service.endpoint.eth.controller;

import io.mtc.service.endpoint.eth.service.Web3jService;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * 采用web3j做节点
 *
 * @author Chinhin
 * 2018/6/28
 */
@Slf4j
@RestController
public class Web3jController {

    @Resource
    private Web3jPool web3JPool;

    @Resource
    private Web3jService web3jService;

    @GetMapping("/getTransactionCount/{address}")
    public String getTransactionCount(@PathVariable String address) {
        Web3j web3j = null;
        try {
            web3j = web3JPool.getConnection();
            EthGetTransactionCount send = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
            BigInteger transactionCount = send.getTransactionCount();
            return transactionCount.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    @GetMapping("/getReceipt/{txHash}")
    public TransactionReceipt getReceipt(@PathVariable String txHash) {
        Web3j web3j = null;
        try {
            web3j = web3JPool.getConnection();
            EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(txHash).send();
            return ethGetTransactionReceipt.getTransactionReceipt().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    @GetMapping("/getBlock/{blockNum}")
    public EthBlock getBlock(@PathVariable int blockNum) {
        Web3j web3j = null;
        try {
            web3j = web3JPool.getConnection();
            return web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNum), true).send();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    @GetMapping("/getGasPrice")
    public BigInteger gasPrice() {
        Web3j web3j = null;
        try {
            web3j = web3JPool.getConnection();
            EthGasPrice gasPrice = web3j.ethGasPrice().send();
            return gasPrice.getGasPrice();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    /**
     * 通过Transaction来预估gasLimit
     */
    @GetMapping("/getGasLimit")
    public BigInteger gasLimit(@RequestBody Transaction transaction) {
        Web3j web3j = null;
        try {
            web3j = web3JPool.getConnection();
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            if (ethEstimateGas.hasError()){
                throw new RuntimeException(ethEstimateGas.getError().getMessage());
            }
            return ethEstimateGas.getAmountUsed();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    /**
     * 获得gasPrice及当前钱包的nonce值
     * @param walletAddress 钱包地址
     * @return 结果
     */
    @GetMapping("/getGasPriceAndNonce")
    public BigInteger[] getGasPriceAndNonce(String walletAddress) {
        Web3j web3j = null;
        BigInteger[] results = new BigInteger[2];
        try {
            web3j = web3JPool.getConnection();
            EthGasPrice gasPrice = web3j.ethGasPrice().send();
            results[0] = gasPrice.getGasPrice();

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    walletAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
            results[1] = ethGetTransactionCount.getTransactionCount();

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    @GetMapping("/getSymbolByAddress/{address}")
    public String getSymbolByAddress(@PathVariable String address) {
        return web3jService.getInfoByAddress("symbol", address);
    }

    @GetMapping("/getNameByAddress/{address}")
    public String getNameByAddress(@PathVariable String address) {
        return web3jService.getInfoByAddress("name", address);
    }

}
