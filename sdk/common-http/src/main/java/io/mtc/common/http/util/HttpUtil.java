package io.mtc.common.http.util;

import io.mtc.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 请求工具
 *
 * @author Chinhin
 * 2018/6/21
 */
@Slf4j
public class HttpUtil {

    /**
     * GET请求
     * @param url 请求地址
     * @return 返回字符串结果
     */
    public static String get(String url) {
        // 创建一个请求
        Request request = new Request.Builder().url(url).build();
        // 返回实体
        Response response = null;
        try {
            response = getOkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResponseString(response);
    }

    /**
     * GET请求并返回需要的对象
     * @param url 请求地址
     * @param tClass 类
     * @param <T> 类型
     * @return 结果
     */
    public static <T> T get4obj(String url, Class<T> tClass) {
        String result = get(url);
        if (result == null) {
            return null;
        }
        return CommonUtil.fromJson(result, tClass);
    }

    /**
     * 异步Get请求
     * @param url 链接地址
     * @param callback 回调函数
     */
    public static void asyncGet(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * POST请求
     * @param url 请求地址
     * @param string 参数体
     * @return 结果
     */
    public static String post(String url, String string) {

        MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder().url(url).post(RequestBody.create(MEDIA_TYPE, string))
                .header("accept", "application/json").build();

        Response response = null;
        try {
            response = getOkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResponseString(response);
    }

    public static String post(String url, Map<String, String> form){
        FormBody.Builder builder = new FormBody.Builder();
        form.entrySet().stream().forEach(entry -> {
            builder.add(entry.getKey(), entry.getValue());
        });
        FormBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body)
                .header("accept", "application/json").build();

        Response response = null;
        try {
            response = getOkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResponseString(response);
    }

    /**
     * POST请求
     * @param url 请求地址
     * @param string 参数体
     * @param tClass 类型
     * @param <T> 类型
     * @return 结果
     */
    public static <T> T post4obj(String url, String string, Class<T> tClass) {
        String result = post(url, string);
        return CommonUtil.fromJson(result, tClass);
    }

    /**
     * 异步post请求
     * @param url 请求地址
     * @param string 参数体
     * @param callback 回调函数
     */
    public static void asyncPost(String url, String string, Callback callback) {
        MediaType MEDIA_TYPE = MediaType.parse("text/text; charset=utf-8");
        Request request = new Request.Builder().url(url).post(RequestBody.create(MEDIA_TYPE, string)).build();
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * 创建OkHttpClient对象
     * @return OkHttpClient对象
     */
    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                // 设置链接超时
                .connectTimeout(10, TimeUnit.SECONDS)
                // 设置写数据超时
                .writeTimeout(10, TimeUnit.SECONDS)
                // 设置读数据超时
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private static String getResponseString(Response response) {
        String result = null;
        // 判断是否成功
        if (response != null && response.isSuccessful()) {
            try {
                if (response.body() != null) {
                    result = response.body().string();
                    response.body().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
