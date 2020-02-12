package io.mtc.service.endpoint.eth.util.remoteLinux;

import io.mtc.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 执行linux命令工具类
 *
 * 注意设置shell脚本执行权限
 *
 * @author Chinhin
 * 2018/7/18
 */
@Slf4j
public class LinuxExecuteUtil {

    /**
     * 执行linux的命令
     * @param args 执行参数, main见例子
     */
    public static void execute(String... args) {
        log.info("linux execute {}", CommonUtil.toJson(args));
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec(args);
            pro.waitFor();
        } catch (Exception e) {
            errorHandler(pro);
            e.printStackTrace();
        } finally {
            log.info("linux execute successful");
            if (pro != null) {
                pro.destroy();
            }
        }
    }

    /**
     * 重新加载 nginx 错误时的处理
     */
    private static void errorHandler(Process pro) {
        log.error(">> Occur some error when executing:");
        if (pro != null) {
            InputStream in = null;
            try {
                in = pro.getInputStream();
                BufferedReader read = new BufferedReader(new InputStreamReader(in));
                String result = read.readLine();
                log.error(result);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        execute("sh", "/Users/Chinhin/Desktop/MTC2/zuul/bootstrap.sh", "start");
    }

}
