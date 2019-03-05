package io.mtc.facade.user.util.wallet;

import io.mtc.common.util.AesCBC;
import io.mtc.facade.user.bean.CreateWalletResultBean;
import io.mtc.facade.user.entity.User;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

/**
 * 以太坊生成钱包地址
 *
 * @author Chinhin
 * 2019-01-24
 */
public class EthCreateWalletUtil {

    //不能改
    private static final String ETH_WALLET_PWD = "#@ce454#W";

    public static CreateWalletResultBean createWallet(User user) throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        WalletFile walletFile = Wallet.createLight(ETH_WALLET_PWD, ecKeyPair);
        String address = "0x" + walletFile.getAddress();
        String privateKey = ecKeyPair.getPrivateKey().toString(16);
        String encryptPrivateKey = AesCBC.getInstance().simpleEncrypt(privateKey,
                AesCBC.makeKey(user.getId() + ETH_WALLET_PWD + 1));
        return new CreateWalletResultBean(encryptPrivateKey, address);
    }

    public static void main(String[] args) throws Exception {
        User user = new User();
        user.setId(1L);
        CreateWalletResultBean resultBean = createWallet(user);
        System.out.println(resultBean.getAddress());
        System.out.println(resultBean.getEncryptPrivateKey());
        System.out.println(AesCBC.getInstance().simpleDecrypt(resultBean.getEncryptPrivateKey(), AesCBC.makeKey(user.getId() + ETH_WALLET_PWD + 1)));
    }

}
