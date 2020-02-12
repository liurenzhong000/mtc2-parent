package io.mtc.service.endpoint.eth.service;

import io.mtc.common.constants.Constants;
import io.mtc.service.endpoint.eth.util.Web3jPool;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.tx.exceptions.ContractCallException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * web3j的业务层
 *
 * @author Chinhin
 * 2018/8/16
 */
@Service
public class Web3jService {

    @Resource
    private Web3jPool web3JPool;

    public String getInfoByAddress(String info, String address) {
        Web3j web3j = null;
        String symbol = null;
        try {
            web3j = web3JPool.getConnection();
            Function function = new Function(info,
                    Collections.emptyList(),
                    Collections.singletonList(new TypeReference<Utf8String>() {
                    })
            );
            String data = FunctionEncoder.encode(function);

            Transaction ethCallTransaction = Transaction.createEthCallTransaction(Constants.MTC_ADDRESS, address, data);
            EthCall ethCall = web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send();
            List<Type> types = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if (types != null && !types.isEmpty()) {
                Type result = types.get(0);
                if (result == null) {
                    throw new ContractCallException("Empty value (0x) returned from contract");
                }
                symbol = result.getValue().toString();
            }
            return symbol;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }

    public int getIntInfoByAddress(String info, String address) {
        Web3j web3j = null;
        int value = 0;
        try {
            web3j = web3JPool.getConnection();
            Function function = new Function(info,
                    Collections.emptyList(),
                    Collections.singletonList(new TypeReference<Uint>() {
                    })
            );
            String data = FunctionEncoder.encode(function);

            Transaction ethCallTransaction = Transaction.createEthCallTransaction(Constants.MTC_ADDRESS, address, data);
            EthCall ethCall = web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send();
            List<Type> types = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if (types != null && !types.isEmpty()) {
                Type result = types.get(0);
                if (result == null) {
                    throw new ContractCallException("Empty value (0x) returned from contract");
                }
                String temp = result.getValue().toString();
                value = Integer.parseInt(temp);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (web3j != null) {
                web3JPool.close(web3j);
            }
        }
    }
}
