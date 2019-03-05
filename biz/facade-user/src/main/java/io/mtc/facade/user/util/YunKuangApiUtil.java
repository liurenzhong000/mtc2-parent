package io.mtc.facade.user.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.AesCBC;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.user.entity.User;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 和尚的go的服务进行交互的请求工具类
 *
 * @author Chinhin
 * 2018/12/7
 */
public class YunKuangApiUtil {

    private static String AES_KEY = "XXX";//未知服务，剔除（20180304）

    public static boolean login(User user, String url) {
        Map<String, Object> body = new HashMap<>();
        body.put("UID", String.valueOf(user.getId()));
        body.put("token", user.getToken());
        body.put("type", "yunkuang");

        String bodyString = CommonUtil.toJson(body);
        String encryptBody = AesCBC.getInstance().simpleEncrypt(bodyString, AES_KEY);

        String response = HttpUtil.post(url, encryptBody);
        if (response == null) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(response);
        int statusCode = jsonObject.getIntValue("StatusCode");
        return statusCode == 200;
    }

    public static void main(String[] args) {
        User user = new User();
        user.setId(1L);
        user.setToken("kZdkwPXJonxNqx6o");
        boolean login = login(user, "***");
        System.out.println(login);
    }

}
