package io.mtc.server.zuul.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * url的工具类
 *
 * @author Chinhin
 * 2018/12/11
 */
public class Urls {

    //,"/user/fund/userWalletAddress**"
    // 不需要token验证的请求
    public static final String[] UNCHECK_TOKEN_URL = new String[]{
            "/user/user/sendCode*"
            ,"/user/user/validCode*"
            ,"/user/user/register*"
            ,"/user/user/findLoginPwd*"
            ,"/user/user/login*"
            ,"/user/user/exist*"
            ,"/user/user/upload*"
            ,"/user/createCurrency/categories*"
            ,"/user/createCurrency/list*"

            ,"/user/redEnvelope/guestGrabbedHistory*"
            ,"/user/redEnvelope/guestOpen*"
            ,"/user/redEnvelope/popDetail*"

            ,"/user/v2/api-docs*"
            ,"/user/swagger*"
            ,"/user/webjars*"
            ,"/user/fund/withdrawAIP*"
            ,"/user/eos/eosApi/**"

            ,"/user/btcApi**"
    };

    public static boolean isUncheckUri(String requestURI) {
        return PatternMatchUtils.simpleMatch(Urls.UNCHECK_TOKEN_URL, requestURI);
    }

    /**
     * 是否会传密码但是不会用token验证的请求
     * @param requestURI 请求地址
     * @return true表示是，false表示否
     */
    public static boolean isPwdUri(String requestURI) {
        return PatternMatchUtils.simpleMatch(new String[] {
            "/user/user/register*"
            ,"/user/user/findLoginPwd*"
            ,"/user/user/login*"
        }, requestURI);
    }

    public static String getBody(HttpServletRequest request) {
        InputStream in;
        try {
            in = request.getInputStream();
            return StreamUtils.copyToString(in, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
