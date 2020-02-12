package io.mtc.common.util;

import io.mtc.common.constants.MTCError;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回客户端json共通
 *
 * @author Chinhin
 * 2018/6/8
 */
public class ResultUtil {

    /**
     * 返回错误信息
     * @param errorInfo 错误消息提示
     * @return 结果
     */
    public static String error(String errorInfo){
        return error(errorInfo, 0);
    }

    /**
     * 返回带有错误编号的错误信息(APP端必须采用)
     * @param errorInfo 错误信息
     * @param code 错误编号
     * @return 结果
     */
    public static String error(String errorInfo, int code) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", 500);
        result.put("timestamp", new Date().getTime());
        result.put("errorCode", code);
        result.put("error", errorInfo);
        return CommonUtil.toJson(result);
    }

    public static Map<String, Object> errorObj(String errorInfo, int code) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", 500);
        result.put("timestamp", new Date().getTime());
        result.put("errorCode", code);
        result.put("error", errorInfo);
        return result;
    }

    /**
     * 返回错误消息
     * @param error 错误枚举
     * @return 结果
     */
    public static String error(MTCError error) {
        return error(error.getValue(), error.getKey());
    }

    public static Map<String, Object> errorObj(MTCError error) {
        return errorObj(error.getValue(), error.getKey());
    }

    /**
     * 返回成功信息
     * @param successInfo 成功对象
     * @return 结果
     */
    public static String success(Object successInfo){
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", 200);
        result.put("timestamp", new Date().getTime());
        result.put("result", successInfo);
        return CommonUtil.toJson(result);
    }

    /**
     * 没有成功信息
     * @return 结果
     */
    public static String success() {
        return success("OK");
    }

    public static Object successObj() {
        return successObj("OK");
    }

    public static Object successObj(Object successInfo) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", 200);
        result.put("timestamp", new Date().getTime());
        result.put("result", successInfo);
        return result;
    }

}
