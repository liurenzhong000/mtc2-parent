//package io.mtc.service.endpoint.eth.util;
//
//import io.mtc.common.util.CommonUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.web3j.abi.FunctionEncoder;
//import org.web3j.abi.FunctionReturnDecoder;
//import org.web3j.abi.TypeReference;
//import org.web3j.abi.datatypes.Address;
//import org.web3j.abi.datatypes.Function;
//import org.web3j.abi.datatypes.Type;
//import org.web3j.abi.datatypes.generated.Uint256;
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.core.DefaultBlockParameterName;
//import org.web3j.protocol.core.DefaultBlockParameterNumber;
//import org.web3j.protocol.core.methods.request.Transaction;
//import org.web3j.protocol.core.methods.response.*;
//import org.web3j.protocol.http.HttpService;
//import org.web3j.tx.exceptions.ContractCallException;
//
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.Collections;
//import java.util.List;
//
///**
// * 测试
// *
// * @author Chinhin
// * 2018/6/21
// */
//@Slf4j
//public class GethUtilTest {
//
//    private static final String myWalletAddress = "0x291DDf0ECD369F6ba1310bF6035055fEf5300972";
//    private static final String endpointUrl = "http://47.74.179.117:7000";
//    private static final String outEndpoint = "https://mainnet.infura.io/DwsTJXLCQR2aMhi7QTLR";
//    private static final String mtcAddress = "0xdfdc0d82d96f8fd40ca0cfb4a288955becec2088";
//
//    @Test
//    public void getTokenBalance() throws IOException {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//
//        Function function = new Function("balanceOf",
//                Collections.singletonList(new Address(myWalletAddress)),
//                Collections.singletonList(new TypeReference<Uint256>() {
//                }));
//        String data = FunctionEncoder.encode(function);
//
//        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(myWalletAddress, "0xdc31472c9d68ebda567df6fc5066ea964f9ac991", data),
//                DefaultBlockParameterName.LATEST).send();
//
//        List<Type> types = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
//        if (types != null && !types.isEmpty()) {
//            Type result = types.get(0);
//            if (result == null) {
//                throw new ContractCallException("Empty value (0x) returned from contract");
//            }
//            System.out.println(result.getValue());
//        }
//    }
//
//    @Test
//    public void getEtherBalance() throws IOException {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        BigInteger balance = web3j.ethGetBalance("0x69701ea5fc87523ad4c324ff4e860f7c9b1dce96", DefaultBlockParameterName.LATEST).send().getBalance();
//        System.out.println(balance);
//    }
//
//    /**
//     * 该方法获得的交易详情的to是合约地址，但是可以通过input获取最终转账的address
//     */
//    @Test
//    public void getTransactionInfo() throws IOException {
//        String txHash = "0x5419a811f8347932772923bf630ead79e6a64b830ad0dc5721292a6518188c49";
//
//        Web3j web3j = Web3j.build(new HttpService(outEndpoint));
//        org.web3j.protocol.core.methods.response.Transaction transaction = web3j.ethGetTransactionByHash(txHash).send().getTransaction().orElse(null);
//
//        System.out.println(CommonUtil.toJson(transaction));
//    }
//
//    @Test
//    public void getTransactionInfo2() {
//        EthGetTransactionReceipt transaction = GethUtil.request4obj(endpointUrl, "eth_getTransactionReceipt", EthGetTransactionReceipt.class,
//                "0xd675f3ad54ecd244afddd266f5ad237c3e10929ccb1660b4c5ffe10b46a87cec");
//
//        TransactionReceipt transactionReceipt = transaction.getTransactionReceipt().get();
//        Log log = transactionReceipt.getLogs().get(0);
//        String s = log.getTopics().get(2);
//        String myAddress = "0x" + s.substring(26);
//    }
//
//    @Test
//    public void getReceipt() {
//        String eth_getTransactionReceipt = GethUtil.request(endpointUrl, "eth_getTransactionReceipt",
//                "0xb3b285fc30591c0f5ffd7188b6aa713bf14d820ed9c01a973b2aa942f5ad1fae");
//        System.out.println(eth_getTransactionReceipt);
//    }
//
//    @Test
//    public void getBlock() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        try {
//            EthBlock block = web3j.ethGetBlockByNumber(
//                    new DefaultBlockParameterNumber(5862384), true).send();
//            System.out.println(block.getBlock().getTimestamp());
//            System.out.println(System.currentTimeMillis());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void txCount() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        try {
//            EthGetTransactionCount send = web3j.ethGetTransactionCount("0xe85e68d1207d9323a75d4a0fdba08d3b4fee9075", DefaultBlockParameterName.LATEST).send();
//            BigInteger transactionCount = send.getTransactionCount();
//            System.out.println(transactionCount);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}