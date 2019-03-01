package io.mtc.service.endpoint.eth.util;

import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import org.web3j.protocol.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 与节点通信的工具类
 *
 * @author Chinhin
 * 2018/6/21
 */
public class GethUtil {

    /**
     * 对节点的post请求
     * @param nodeUrl 节点url
     * @param method 方法
     * @param params 参数
     * @return 结果字符串
     */
    public static String request(String nodeUrl, String method, Object... params) {
        Map<String, Object> body = new HashMap<>();
        body.put("jsonrpc", "2.0");
        body.put("method", method);
        if (params == null || (params.length == 1 && StringUtil.isBlank(params[0].toString()))) {
            params = new Object[0];
        }
        body.put("params", params);
        body.put("id", 1);
        return HttpUtil.post(nodeUrl, CommonUtil.toJson(body));
    }

    /**
     * 对节点的post请求
     * @param nodeUrl 节点url
     * @param method 方法
     * @param tClass 类型
     * @param params 参数
     * @param <T> 类型
     * @return 结果对象
     */
    public static <T extends Response> T request4obj(String nodeUrl, String method, Class<T> tClass, Object... params) {
        String result = request(nodeUrl, method, params);
        return CommonUtil.fromJson(result, tClass);
    }

}
