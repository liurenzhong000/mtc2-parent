package io.mtc.facade.user.util;

import io.mtc.facade.user.entity.User;
import io.rong.RongCloud;
import io.rong.models.Result;
import io.rong.models.response.TokenResult;
import io.rong.models.user.UserModel;

/**
 * 融云工具类
 *
 * @author Chinhin
 * 2019-01-21
 */
public class RongyunUtil {

    private static final String APP_KEY = "6tnym1br6ppp7";

    private static final String APP_SECRET = "TORtlhEWbow";

    private static final String URL = "http://api-cn.ronghub.com/user/getToken.json";

    public static String getToken(User user) throws Exception {
        RongCloud rongCloud = RongCloud.getInstance(APP_KEY, APP_SECRET);
        io.rong.methods.user.User rongyunUser = rongCloud.user;
        UserModel userModel = new UserModel()
                .setId(String.valueOf(user.getId()))
                .setName(user.getNick())
                .setPortrait(user.getPhoto());
        TokenResult result = rongyunUser.register(userModel);
        return result.getToken();
    }

    public static String updateUserInfo(User user) throws Exception {
        RongCloud rongCloud = RongCloud.getInstance(APP_KEY, APP_SECRET);
        io.rong.methods.user.User rongyunUser = rongCloud.user;
        UserModel userModel = new UserModel()
                .setId(String.valueOf(user.getId()))
                .setName(user.getNick())
                .setPortrait(user.getPhoto());
        Result result = rongyunUser.update(userModel);
        return result.toString();
    }

}
