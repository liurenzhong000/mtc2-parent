package io.mtc.service.notification.util;

import io.mtc.common.constants.MessageConstants;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * 消息工具
 *
 * @author Chinhin
 * 2018/7/11
 */
@Component
public class MessageUtil {

    @Resource
    private MessageSource messageSource;

    /**
     * 获取消息字符串
     * @param key 消息的key
     * @param args 参数数组
     * @param langCode 语言code
     * @return 消息
     */
    public String getMessage(String key, @Nullable Object[] args, int langCode) {
        // 默认英文
        Locale locale = Locale.US;
        if (langCode == MessageConstants.LANG_CH) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (langCode == MessageConstants.LANG_KO) {
            locale = Locale.KOREAN;
        }
        return messageSource.getMessage(key, args, locale);
    }

}
