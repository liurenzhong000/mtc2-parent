package io.mtc.common.web3j.util;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class MeshTransactionData {

    public BigInteger nonce;
    public BigInteger gasPrice;
    public BigInteger gasLimit;
    public String fromAddress;
    public String toAddress;
    public String contractAddress;
    public BigInteger value;
    public String input;
    public String txData;
    public String txHash;

    public static MeshTransactionData from(String strData) {

        if (strData == null || strData.trim().equals(""))
            return null;

        if (strData.startsWith("0x") || strData.startsWith("0X")) {
            strData = strData.substring(2);
        }

        try {
            byte[] data = Hex.decode(strData);
            RlpList rlpList = RlpDecoder.decode(data);
            RlpList transaction = (RlpList) rlpList.getValues().get(0);
            MeshTransactionData transactionData = new MeshTransactionData();
            {
                //获取nonce值
                RlpString item = (RlpString) transaction.getValues().get(0);
                try {
                    transactionData.nonce = new BigInteger(1, item.getBytes());
                } catch (Exception e) {
                    transactionData.nonce = BigInteger.ZERO;
                }

            }

            {
                //获取gasPrice值
                RlpString item = (RlpString) transaction.getValues().get(1);
                try {
                    transactionData.gasPrice = new BigInteger(1, item.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    transactionData.gasPrice = BigInteger.ZERO;
                }
            }

            {
                //gasLimit
                RlpString item = (RlpString) transaction.getValues().get(2);
                BigInteger gasLimit = new BigInteger(1, item.getBytes());
                transactionData.gasLimit = gasLimit;
            }

            {
                //toAddress
                RlpString item = (RlpString) transaction.getValues().get(3);
                String toAddress = Hex.toHexString(item.getBytes());
                transactionData.toAddress = "0x" + toAddress;
            }

            {
                //value
                RlpString item = (RlpString) transaction.getValues().get(4);
                try {
                    transactionData.value = new BigInteger(1, item.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    transactionData.value = BigInteger.ZERO;
                }
            }

            {
                // data
                RlpString item = (RlpString) transaction.getValues().get(5);
                byte[] itemData = item.getBytes();
                if (itemData == null || itemData.length == 0) {
                    transactionData.input = "0x";
                } else {
                    transactionData.input = "0x" + Numeric.toHexStringNoPrefix(itemData);
                }
                try {
                    if (itemData != null && itemData.length >= 24) {
                        if (itemData[0] == (byte)0xA9 && itemData[1] == (byte)0x05 && itemData[2] == (byte)0x9C && itemData[3] == (byte)0xBB) {
                            byte[] tokenToData = new byte[20];
                            byte[] tokenValueData = new byte[32];
                            System.arraycopy(itemData, 16, tokenToData, 0, tokenToData.length);
                            System.arraycopy(itemData, 36, tokenValueData, 0, tokenValueData.length);
                            transactionData.contractAddress = transactionData.toAddress;
                            transactionData.toAddress = "0x" + Numeric.toHexStringNoPrefixZeroPadded(new BigInteger(1, tokenToData), 40);
                            transactionData.value = new BigInteger(1, tokenValueData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            {
                RlpString item = (RlpString) transaction.getValues().get(6);
                byte[] itemData = item.getBytes();

                byte v = 0;
                if (itemData.length == 1) {
                    v = itemData[0];
                }

                RlpString r_item = (RlpString) transaction.getValues().get(7);
                RlpString s_item = (RlpString) transaction.getValues().get(8);

                byte[] r = Numeric.toBytesPadded(new BigInteger(r_item.getBytes()), 32);
                byte[] s = Numeric.toBytesPadded(new BigInteger(s_item.getBytes()), 32);

                Sign.SignatureData signature = new Sign.SignatureData(v, r, s);

                if (transactionData.contractAddress == null || transactionData.contractAddress.equals("")) {
                    transactionData.contractAddress="0";
                    SignedRawTransaction srt = new SignedRawTransaction(transactionData.nonce, transactionData.gasPrice, transactionData.gasLimit, transactionData.toAddress, transactionData.value, transactionData.input, signature);
                    transactionData.fromAddress = srt.getFrom();
                } else {
                    SignedRawTransaction srt = new SignedRawTransaction(transactionData.nonce, transactionData.gasPrice, transactionData.gasLimit, transactionData.contractAddress, BigInteger.ZERO, transactionData.input, signature);
                    transactionData.fromAddress = srt.getFrom();
                }

            }

            transactionData.txData = strData;
            transactionData.txHash = "0x" + Hex.toHexString(Hash.sha3(data));

            return transactionData;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "MeshTransactionData{" +
                "nonce=" + nonce +
                ", gasPrice=" + gasPrice +
                ", gasLimit=" + gasLimit +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", value=" + value +
                ", data='" + input + '\'' +
                ", txData='" + txData + '\'' +
                ", txHash='" + txHash + '\'' +
                '}';
    }
}


