package io.mtc.facade.user.util;

import io.mtc.common.constants.Constants;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.StringUtil;

import java.util.Date;
import java.util.Random;

/**
 * 用户默认属性
 *
 * @author Chinhin
 * 2018/8/30
 */
public class UserDefaultUtil {

    private static String[] DEFAULT_HEADS = new String[] {
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_0.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_1.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_2.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_3.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_4.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_5.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_6.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_7.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_8.png",
            Constants.ALI_OSS_URI + "defaultHead/accountphoto_9.png",
    };

    //TODO 修改推荐人
    private static String[] DEFAULT_PROMOTER = new String[] {
            // 王桂森
            "8613541298146",
            // 李果
            "8618623118527",
            // 张
            "8617053124400"
    };

    private static final Random random = new Random();

    public static String getRandomHead() {
        Random random = new Random();
        int i = random.nextInt(10);
        return DEFAULT_HEADS[i];
    }

    public static String getNick() {
        StringBuilder stringBuilder = new StringBuilder("ID_");
        stringBuilder.append(DateUtil.formatDate(new Date(), "yyyyMMddSSS"));
        return stringBuilder.toString();
    }

    public static String defaultPromoter() {
        int i = random.nextInt(100);
        if (i < 50) {
            return DEFAULT_PROMOTER[0];
        } else if (i < 75) {
            return DEFAULT_PROMOTER[1];
        } else {
            return DEFAULT_PROMOTER[2];
        }
    }

}
