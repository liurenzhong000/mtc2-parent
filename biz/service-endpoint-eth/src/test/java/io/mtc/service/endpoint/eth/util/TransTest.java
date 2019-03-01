package io.mtc.service.endpoint.eth.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * 测试
 *
 * @author Chinhin
 * 2018/6/21
 */
@Slf4j
public class TransTest {

    private static final String myWalletAddress = "0x291DDf0ECD369F6ba1310bF6035055fEf5300972";
    private static final String endpointUrl = "http://47.74.179.117:7000";
    private static final String outEndpoint = "https://mainnet.infura.io/DwsTJXLCQR2aMhi7QTLR";
    private static final String mtcAddress = "0xdfdc0d82d96f8fd40ca0cfb4a288955becec2088";

    @Test
    public void gasPrice() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        EthGasPrice gasPrice = web3j.ethGasPrice().send();
//
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        web3j.ethGasPrice().sendAsync().whenComplete((ethGasPrice, throwable) -> {
//            System.out.println(ethGasPrice.getGasPrice());
//        });
    }

    @Test
    public void getName() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//
//        Function function = new Function("name",
//                Collections.emptyList(),
//                Collections.singletonList(new TypeReference<Utf8String>() {
//                })
//        );
//
//        String data = FunctionEncoder.encode(function);
//
//        Transaction ethCallTransaction = Transaction.createEthCallTransaction(myWalletAddress, "0xdfdc0d82d96f8fd40ca0cfb4a288955becec2088", data);
//
//        EthCall ethCall = web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).send();
//        List<Type> types = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
//        if (types != null && !types.isEmpty()) {
//            Type result = types.get(0);
//            if (result == null) {
//                throw new ContractCallException("Empty value (0x) returned from contract");
//            }
//            System.out.println(result.getValue());
//        }
    }

    @Test
    public void etherTransaction() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        EthGasPrice gasPrice = web3j.ethGasPrice().send();
//
//        BigDecimal gasPriceUse = new BigDecimal(gasPrice.getGasPrice()).multiply(new BigDecimal(1.1));
//        //设置需要的矿工费
//        BigInteger GAS_PRICE = gasPriceUse.toBigInteger();
//        BigInteger GAS_LIMIT = BigInteger.valueOf(60000);
//
//        // 转账人账户地址
//        String ownAddress = "0x69701ea5fc87523ad4c324ff4e860f7c9b1dce96";
//        // 被转人账户地址
//        String toAddress = "0x291DDf0ECD369F6ba1310bF6035055fEf5300972";
//        // 转账人私钥
//        Credentials credentials = WalletUtils.loadCredentials("cs314816", "/Users/Chinhin/Desktop/証明書/host.store");
//
//        // getNonce（这里的Nonce我也不是很明白，大概是交易的笔数吧）
//        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(ownAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
//        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//        // 创建交易，这里是转0.01个以太币
//        BigInteger value = Convert.toWei("0.01", Convert.Unit.ETHER).toBigInteger();
//        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
//                nonce, GAS_PRICE, GAS_LIMIT, toAddress, value);
//
//        //签名Transaction，这里要对交易做签名
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//        String hexValue = Numeric.toHexString(signedMessage);
//        System.out.println(hexValue);
//
//        //发送交易
//        EthSendTransaction ethSendTransaction =
//                web3j.ethSendRawTransaction(hexValue).sendAsync().get();
//        String transactionHash = ethSendTransaction.getTransactionHash();
//
//        //获得到transactionHash后就可以到以太坊的网站上查询这笔交易的状态了
//        System.out.println(transactionHash);
    }

    @Test
    public void etherTransaction2() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        String toAddress = "0x291DDf0ECD369F6ba1310bF6035055fEf5300972";
//        Credentials credentials = Credentials.create("");
//
//        TransactionReceipt transactionReceipt = Transfer.sendFunds(
//                web3j, credentials, toAddress,
//                BigDecimal.valueOf(0.001), Convert.Unit.ETHER).send();
//        System.out.println(transactionReceipt.getTransactionHash());
    }

    @Test
    public void contractTransaction() {
//        Web3j web3j = Web3j.build(new HttpService(endpointUrl));
//        String ownAddress = "0x69701ea5fc87523ad4c324ff4e860f7c9b1dce96";
//
//        BigInteger value = Convert.toWei("10", Convert.Unit.ETHER).toBigInteger();
//        System.out.println(value);
//        Function function = new Function("transfer",
//                Arrays.asList(new Address("0x291ddf0ecd369f6ba1310bf6035055fef5300972"),
//                        new Uint256(value)),
//                Collections.emptyList());
//
//        String data = FunctionEncoder.encode(function);
//
//        EthGasPrice gasPrice = web3j.ethGasPrice().send();
//        BigDecimal gasPriceUse = new BigDecimal(gasPrice.getGasPrice()).multiply(new BigDecimal(1.1));
//
//        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(ownAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
//        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
//
//        RawTransaction transaction = RawTransaction.createTransaction(nonce, gasPriceUse.toBigInteger(), BigInteger.valueOf(100000), mtcAddress, data);
//
//        // 转账人私钥
//        Credentials credentials = WalletUtils.loadCredentials("cs314816", "/Users/Chinhin/Desktop/証明書/host.store");
//        byte[] signedMessage = TransactionEncoder.signMessage(transaction, credentials);
//        String hexValue = Numeric.toHexString(signedMessage);
//        System.out.println(hexValue);
//
//        EthSendTransaction send = web3j.ethSendRawTransaction(hexValue).send();
//        System.out.println(send.getTransactionHash());
    }

    @Test
    public void testParse() {
//        MeshTransactionData data = MeshTransactionData.from("0xf8ab8183850147d35700830186a094dfdc0d82d96f8fd40ca0cfb4a288955becec208880b844a9059cbb000000000000000000000000291ddf0ecd369f6ba1310bf6035055fef53009720000000000000000000000000000000000000000000000008ac7230489e800001ba0119f4ccdb8df2f75e39e19026497a1d5f8c47091481ccc8f29b35da70dba4f66a009d0cbc4a1ade278b16dffe6ce3930683faa59f004b15600fa06ebb9a58ad27b");
//        MeshTransactionData data = MeshTransactionData.from("0xf86c818485019680f10082ea6094291ddf0ecd369f6ba1310bf6035055fef5300972872386f26fc10000801ba0b7159687e70ad0cae7fef3864ffe333f19621d103d066d81442abc539bd2fb7ba0255b5fa73edbd43df0646c2e6d59897d5a7609a6a5acd94dba58536d8436692a");
//        System.out.println(data);
    }

    @Test
    public void makeAccount() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        String path = "/Users/Chinhin/Desktop/";
        String pwd = "shenbin";

        long begin = System.currentTimeMillis();
        String walletFile = WalletUtils.generateNewWalletFile(pwd, new File(path), false);
        System.out.println(walletFile);
        Credentials ALICE = WalletUtils.loadCredentials(pwd, path);
        ALICE.getEcKeyPair().getPrivateKey();
    }

    @Test
    public void makeAccount2() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {
        String pwd = "shenbin";

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        WalletFile walletFile = Wallet.createLight(pwd, ecKeyPair);

        String address = walletFile.getAddress();
        BigInteger privateKey = ecKeyPair.getPrivateKey();

        System.out.println(address + " : " + privateKey.toString(16));

        Credentials credentials = Credentials.create(privateKey.toString(16));
        String address1 = credentials.getAddress();
        System.out.println(address1);
    }

}