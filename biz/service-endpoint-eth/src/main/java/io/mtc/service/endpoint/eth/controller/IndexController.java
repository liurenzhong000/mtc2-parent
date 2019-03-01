package io.mtc.service.endpoint.eth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.MTCError;
import io.mtc.common.dto.EthereumRequest;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.common.web3j.util.MeshTransactionData;
import io.mtc.service.endpoint.eth.service.TransactionService;
import io.mtc.service.endpoint.eth.util.EndpointUrlFactory;
import io.mtc.service.endpoint.eth.util.GethUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通用接口
 *
 * @author Chinhin
 * 2018/6/21
 */
@Slf4j
@RestController
public class IndexController {

    @Value("${endpoint.index}")
    private int endpointIndex;

    @Value("${deposit-address}")
    private String depositAddress;

    @Resource
    private TransactionService transactionService;

    /**
     * 以太坊通用接口
     * @param ethereumDTO 请求参数
     * @return 结果
     */
    @PostMapping("/ethApi")
    public String ethApi(@RequestBody EthereumRequest ethereumDTO) {
        log.info("ethApi {}", CommonUtil.toJson(ethereumDTO));

        if (ethereumDTO == null
                || ethereumDTO.getParams() == null
                || StringUtil.isBlank(ethereumDTO.getMethod())) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }

        int txType = 0;
        long txId = 0L;

        MeshTransactionData transactionData = null;
        // 无网交易去重
        if ("eth_sendRawTransaction".equals(ethereumDTO.getMethod())) {
            Object txDataObj = ethereumDTO.getParams()[0];
            transactionData = MeshTransactionData.from((String) txDataObj);
            String txHash = transactionData.txHash;
            String result = transactionService.checkIfTxExist(txHash);
            if (result != null) {
                return result;
            }
            // 充值的转账
            if (ethereumDTO.isDeposit()) {
                txId = ethereumDTO.getDepositBillId();
                txType = 1;
                // 如果充值地址错误，则返回错误
                if (!depositAddress.equals(transactionData.toAddress)) {
                    return ResultUtil.error(MTCError.DEPOSIT_DEST_ERROR);
                }
            }
        // 获取区块高度
        } else if ("eth_blockNumber".equals(ethereumDTO.getMethod())
                || "eth_getTransactionCount".equals(ethereumDTO.getMethod())) {
            return ResultUtil.error(MTCError.REQUEST_WEB3J_ERROR);
        }

        String result = GethUtil.request(EndpointUrlFactory.getEndpointAtIndex(endpointIndex), ethereumDTO.getMethod(), ethereumDTO.getParams());
        if (result == null) {
            log.error("Request endpoint error: {}", CommonUtil.toJson(ethereumDTO.getParams()));
            return ResultUtil.error(MTCError.REQUEST_ENDPOINT_ERROR);
        }

        // 交易转账的请求，需要记录交易记录 eth_sendTransaction
        if ("eth_sendRawTransaction".equals(ethereumDTO.getMethod())) {
            log.info("Transaction request: {}", CommonUtil.toJson(ethereumDTO.getParams()));
            // 解析
            JSONObject resultJson = JSON.parseObject(result);
            // The transaction hash, or the zero hash if the transaction is not yet available
            if (!resultJson.containsKey("result") || "0".equals(resultJson.getString("result"))) {
                log.error("Transaction fail ethApi: {}", resultJson);
            } else {
                // 得到交易记录的hash
                String transHash = resultJson.getString("result");
                transactionService.transactionHandler(transactionData, ethereumDTO.getType(),
                        ethereumDTO.getMesh_gas(), ethereumDTO.getRemark(), txType, txId);
                log.info("Transaction success: {}", transHash);
            }
        }
        return result;
    }

}
