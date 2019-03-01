package io.mtc.service.endpoint.eth.util.remoteLinux;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import io.mtc.common.util.StringUtil;
import io.mtc.service.endpoint.eth.util.EndpointUrlFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 重启geth工具类
 *
 * 1.放置/alidata1/geth.sh
 * 2.设置geth.sh具有可执行权限
 *
 * @author Chinhin
 * 2018/7/18
 */
@Slf4j
public class RemoteLinuxUtil {

    private static final String RESTART_GETH_COMMAND = "/alidata1/geth.sh";

    /**
     * 重启geth
     * @param index 实例号
     */
    public static void rebootGeth(int index) {

        Connection conn = null;
        Session session = null;
        try {
            String ip = EndpointUrlFactory.getIp(index);
            if (StringUtil.isEmpty(ip)) {
                return;
            }
            conn = new Connection(ip);
            conn.connect();
            boolean isAuthenticated = auth(index, conn);
            if (!isAuthenticated) {
                throw new IOException("Authentication failed.");
            }
            session = conn.openSession();
            session.execCommand(RESTART_GETH_COMMAND);
            InputStream stdout = new StreamGobbler(session.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
                log.info("exec shell: {}", line);
            }
            session.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (session != null) {
                session.close();
            }
        }
    }

    private static boolean auth(int index, Connection conn) throws IOException {
        boolean isAuthenticated;
        if (index > 2) {
            String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEpAIBAAKCAQEAjj933pLn+szRdzZtXJQGuqw4nJYdD/+1DEsN5gSMmB3u+F5D\n" +
                    "lGASD9qMSmy8ob4BLnOpdURBlELcJBzpDHJ/4hQj4cxTKaP+b95I5rRS1FHlgDxX\n" +
                    "XPylPFptoYsS/plmTSUfbOfWG6duULkO65bIuNfHveZbRU7yBeFVYC09wvPbM1E3\n" +
                    "D3Gwfp+PTxEpDJsLIIz/N6Hnbk35xPgzavRHdwHuPS7arzhNc1CeSWdMP+lKXHIA\n" +
                    "zNS3y92XifcPLuahBVdrjiVSfSwuTR/Byj6+qKspR3EuHxVe7cIX2SEIvGHqjuq5\n" +
                    "JfCVkuGf+1uIdqA+pSl+PKDXGeASbu1l+GZPeQIDAQABAoIBAD47hjXaD6Op9/ov\n" +
                    "1airGkaREjNveUeGl67JJ0t6sgfbELGYi/heD4jgFIB7EguxFNM9xuWG9ynmFmm2\n" +
                    "PD812L0QEqK2wat2E9rdZQeJ3LMHIWD5BT+pSKEe7te+AJFii1803orG4uU2Wwz1\n" +
                    "6ZEoZqh3vutV3Jh18WyhKLjrEho36/94BmD9ssYGuI5SM+Rap2LmB8w5tdKXesdq\n" +
                    "BLts3oEG1G/VHnL4+0hKFjkAEqxVVAYR0/VahJxSdUjGWZENnDW8tNIq/y8VfiT0\n" +
                    "CV0oeBVQZPBOQZ1/aN0IQ+IO69LZA92uD4kyJmFcYcPXFtooWOkwSefQQL7IVCjY\n" +
                    "pYByAAECgYEA5HAmU10FrbaYaczTY31P5awKN+Vklu1afJ8GcfLfuprBu+bMMllq\n" +
                    "/ngOx9U9Q96rN0T57Hwnwy4BSfmhgmGndo1K3tpCA9k99GBnwD+Zfn6ce2PZ0kuS\n" +
                    "lmoGe12gDOZwIh3IhYMbU7hIJ+PrlqzUiYV/cKSPPI1Pli8d/VQlvm0CgYEAn2kh\n" +
                    "scJisQioTVIlLjcp5gxkHJY53huwgyh5E2ujLQWhuCKV9Wfg5UF+tUiJW2l07bRG\n" +
                    "IfceJvvwt5s69SoKMLmKdJHjuUO+9x38KfWhD5zWuwaQDkruJ7Bv44h6kvZ5Aj1C\n" +
                    "XpI/HkBKAzuARxAWrhwnT6oe9LNXLGGyI0ZT/b0CgYEA4J4qAURxhfsKaNXfcW9R\n" +
                    "dZsxvP6RZxpiJDHWC4tuVDVBk/qABG4KF8eDeRkDEc5L+p+XQ+Jc0r1UxSFxnxtY\n" +
                    "p+iMmw5vZQtisP1uZwniaxAh2+41y2NF8yKz1vAbpTOdRN6WQjnFUA/e22JwT4sH\n" +
                    "vWWIBwG4Wij0/c68pVDtgA0CgYAPMLByROisn/uRqqM6XS1T65halQIcpRzT0rZ+\n" +
                    "4EHjV2cKqdkKUoS+s9gEJE5adJkc0ZHhSLAJ3PYaOOOMWx4veG62HvTJQahf2e6e\n" +
                    "fPaFC1f26HAvvHonNAvlbaroeC4dxYKeimcTVeL3neLiYqkWnD4uvDJI9RH4e7LC\n" +
                    "47+7LQKBgQCfnSmYbQmOAjysr63C00FfW0mnM4hP3y3NZmHkFZ6IvseimzL8YIOo\n" +
                    "rRf2Q9X0GTfmIEiwnu1w+yBwBUlcas4f2PX9/TH8snEhPxQrN8z1kcoy1FHOC9o3\n" +
                    "JwIjTnyN/jwdKFH/n8vagg0i1cGdJo+foBvZvkISHwfFmlefZB3vMg==\n" +
                    "-----END RSA PRIVATE KEY-----";
            isAuthenticated = conn.authenticateWithPublicKey("root", key.toCharArray(), null);
        } else if (index == 0) { // 测试服
            isAuthenticated = conn.authenticateWithPassword("root", "Mtcvphjmyy33j");
        } else {
            isAuthenticated = conn.authenticateWithPassword("root", "Mtcnycjhgdwy5");
        }
        return isAuthenticated;
    }

}