package io.mtc.facade.bitcoin.service;

import io.mtc.common.util.CommonUtil;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.List;

/**
 * 测试
 *
 * @author Chinhin
 * 2019-01-16
 */
public class BitcoinServiceTest {

    private static BitcoinJSONRPCClient client;

    static {
        try {
            client = new BitcoinJSONRPCClient("http://bitcoin:local321@47.74.154.247:8332/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        ECKey fromKey = DumpedPrivateKey.fromBase58(MainNetParams.get(), "KxWBBASYXkuEEPvzmNncJwxuYBe4TLAr2auppKDHT6hsXzrin2gM").getKey();
//        ECKey fromKey = ECKey.fromPrivate("KxWBBASYXkuEEPvzmNncJwxuYBe4TLAr2auppKDHT6hsXzrin2gM".getBytes());
//        ECKey fromKey = ECKey.fromPrivate(new BigInteger("KxWBBASYXkuEEPvzmNncJwxuYBe4TLAr2auppKDHT6hsXzrin2gM", 16));
        NetworkParameters params = MainNetParams.get();
        System.out.println(fromKey.toAddress(params));
    }

    @Test
    public void sendTx() {
        ECKey fromKey = ECKey.fromPrivate(Numeric.toBigInt(""));

        MainNetParams params = MainNetParams.get();
        Transaction tx = new Transaction(params);
        Coin tval = Coin.parseCoin("0.00022");
        Address toAddress = Address.fromBase58(params, "1NhTXMmcz5rVfyborHitVz29zX1NxVUAT8");
        tx.addOutput(tval, toAddress);

        Coin balance = Coin.parseCoin("0.34936");
        Address balanceAddress = Address.fromBase58(params, "1DjscPGGSS4taT8ACsbX4Qf4Mn7bruXQAS");
        tx.addOutput(balance, balanceAddress);

        Script utxo_script = ScriptBuilder.createOutputScript(fromKey.toAddress(params));

        // 输入1
        String utxo_txid2 = "d66fe4178855a4c3ac597d7ba2678374d30b2c2b1869f4923345f3243b65545e";
        int utxo_vout2 = 0;

        TransactionOutPoint txout2 = new TransactionOutPoint(params, utxo_vout2, Sha256Hash.wrap(utxo_txid2));
        tx.addSignedInput(txout2, utxo_script, fromKey, Transaction.SigHash.ALL, true);

        // 输入2
        String utxo_txid = "31fae912bc7dbe78da56dd6c9e05329c43071477cd0d4a160acf3d27bbb0b9c4";
        int utxo_vout = 1;

        TransactionOutPoint txout = new TransactionOutPoint(params, utxo_vout, Sha256Hash.wrap(utxo_txid));
        tx.addSignedInput(txout, utxo_script, fromKey, Transaction.SigHash.ALL, true);


        String signStr = Hex.toHexString(tx.bitcoinSerialize());

//        String hash = client.sendRawTransaction(signStr);
    }

    @Test
    public void sendTx2() {
        String signHash = "01000000025e54653b24f3453392f469182b2c0bd3748367a27b7d59acc3a4558817e46fd6000000006b483045022100b9ecb6ea4adc09df86fd16b3fe0621b36fad0279c7c94665bb3c037e2a6588d602204b3d8a5508493eb2f1d1f83a29c3199657e6004f83dd8ae007e432881f28964d012102ade3b65b1cdc13ed73b9853f01524b0aacf75c7a02905b8643386ace62c8e9acffffffff21c09d9c24ec7bcb692adaa8cfef483158a591ff18f830fc1b9ced28c5c2cb2e000000006b483045022100c70a2c4e67b357fe52e118993f355926baf3eb6c5be96dbd755cdbd37f4c30d502202e00f505238410cfef3f9cd816c3c1f2395754d97193f7ebf8d573e34e7f1e74012102ade3b65b1cdc13ed73b9853f01524b0aacf75c7a02905b8643386ace62c8e9acffffffff02f0550000000000001976a914ee016baa054e6c27d1cda16f1b5492b5787b5ba088ac60181702000000001976a9148bbd3a19cf34131dcc73bb42e6a8bfea3cdfdee288ac00000000";
        String result = client.sendRawTransaction(signHash);
        System.out.println(result);
    }

    @Test
    public void txDetail() {
//        BitcoindRpcClient.RawTransaction rawTransaction = client.decodeRawTransaction(hex);
        BitcoindRpcClient.RawTransaction tx = client.getRawTransaction("abde5e83fc1973fd042c56c8cb41b6c739f3e50678d1fa2f99f0a409e4aa80c7");
        System.out.println(CommonUtil.toJson(tx));
    }

    /**
     * 通过eckey获得wallet文件
     * @throws IOException
     */
    @Test
    public void keyFile() throws IOException {
        MainNetParams params = MainNetParams.get();
        ECKey ecKey = ECKey.fromPrivate(Numeric.toBigInt(""));

        Wallet wallet = new Wallet(params);
        wallet.importKey(ecKey);
        wallet.encrypt("shenbin");
        wallet.saveToFile(new File("/Users/Chinhin/Desktop/btcHost.wallet"));
    }

    /**
     * 通过wallet获得eckey
     * @throws UnreadableWalletException
     */
    @Test
    public void verifyFile() throws UnreadableWalletException {
        MainNetParams params = MainNetParams.get();
        Wallet wallet = Wallet.loadFromFile(new File("/Users/Chinhin/Desktop/temp.wallet"));
        wallet.decrypt("shenbin");
        List<ECKey> importedKeys = wallet.getImportedKeys();
        System.out.println(importedKeys.size());
        ECKey ecKey = importedKeys.get(0);
        System.out.println(ecKey.toAddress(params));
        System.out.println(ecKey.getPrivateKeyAsHex());
    }

    @Test
    public void newWallet() {
        MainNetParams params = MainNetParams.get();
        ECKey ecKey = new ECKey();
        System.out.println(ecKey.toAddress(params));
        System.out.println(ecKey.getPrivateKeyAsHex());
    }

}