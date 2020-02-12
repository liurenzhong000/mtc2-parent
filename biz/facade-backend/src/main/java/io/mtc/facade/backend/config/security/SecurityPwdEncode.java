package io.mtc.facade.backend.config.security;

import io.mtc.common.util.CodecUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * security 加密
 *
 * @author Chinhin
 * 2018/6/11
 */
@Component
public class SecurityPwdEncode implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return CodecUtil.digestStrSHA1(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(CodecUtil.digestStrSHA1(rawPassword.toString()));
    }
}
