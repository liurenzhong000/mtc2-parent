package io.mtc.facade.user.util.wallet;

import com.alibaba.fastjson.JSONObject;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.AesCBC;
import io.mtc.facade.user.bean.CreateWalletResultBean;

/**
 * 创建BTC的钱包
 *
 * @author Chinhin
 * 2019-01-28
 */
public class BtcCreateWalletUtil {

    private static final String URL = "***";

    public static CreateWalletResultBean getAddressByUserId(Long uid) {
        String result = HttpUtil.get(URL + uid);
        JSONObject jsonObject = JSONObject.parseObject(result);
        int isok = jsonObject.getIntValue("isok");
        if (isok == 0) {
            String encryptPrivateKey = AesCBC.getInstance().simpleEncrypt(jsonObject.getString("priPass"),
                    AesCBC.makeKey(uid + String.valueOf(1)));
            return new CreateWalletResultBean(encryptPrivateKey, jsonObject.getString("address"));
        } else {
            return null;
        }
    }

}
