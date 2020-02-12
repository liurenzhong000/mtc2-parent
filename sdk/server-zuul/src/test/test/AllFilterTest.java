package test;

import io.mtc.server.zuul.util.EncryptUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * uri过滤测试
 *
 * @author Chinhin
 * 2018/7/17
 */
public class AllFilterTest {

    @Test
    public void uriCheck() {
        String url = "/currency/0x95d9051e6ece37dd06eefe7ee619290420c9d8ff"; // 传递过来的
        long l = 1531812548349L; // 传递过来的

        String encrypt = EncryptUtil.encrypt(l, url);
        Assert.assertEquals("f956721fac102e83acd31067fa89d90f", encrypt);
    }

    @Test
    public void hashCheck() {
        System.out.println("111".hashCode());
    }
}