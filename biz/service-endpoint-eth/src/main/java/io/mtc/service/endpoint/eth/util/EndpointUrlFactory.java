package io.mtc.service.endpoint.eth.util;

import io.mtc.common.constants.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取节点url
 *
 * @author Chinhin
 * 2018/7/17
 */
@Slf4j
public class EndpointUrlFactory {

    private static final String[] endpoints = new String[]{
            "https://mainnet.infura.io/v3/9baf1d3c67464948a2ba8ccb7a760079", //正式服infura轻节点
            "http://172.21.202.220:7777" //正式服完整节点1
//            "http://172.17.112.63:7000", // 测试服节点1
//            "http://172.21.131.72:7000", // 节点1
//            "http://172.21.131.95:7000", // 节点2
//            "http://172.21.50.90:7000", // 节点3
//            "http://172.21.50.91:7000", // 节点4
//            "http://172.21.50.92:7000" // 节点5
    };

    private static final String[] ips = new String[] {
            "127.0.0.1",
            "172.21.202.220"
//            "39.106.178.73",
//            "47.74.179.117",
//            "47.74.154.21",
//            "47.74.188.184",
//            "47.74.155.232",
//            "47.88.230.226"
    };

    private static boolean logged = false;

    /**
     * 获得节点的url
     * @param index 需要，0表示公网，1~5表示节点5
     * @return 节点url
     */
    public static String getEndpointAtIndex(int index) {
//        "http://47.74.146.41:7777"
//        "http://47.74.179.117:7000", // 正式服节点1外网
//        "https://mainnet.infura.io/DwsTJXLCQR2aMhi7QTLR",
//        "https://mainnet.infura.io/cg4ZXDHrr1fuIhKx1aYF",
//        "https://mainnet.infura.io/zJTHNnZMHBweTR0eHiSe",
//        "https://mainnet.infura.io/37b54iRYVgziDT7M3IJC",
//        "https://mainnet.infura.io/lyQuA5LyD17TUZSLaEHk"
        if (index == -1) {
//            return "https://mainnet.infura.io/zJTHNnZMHBweTR0eHiSe";
            return "https://mainnet.infura.io/v3/060da37455f743aeae35388515b790b6";//测试服1
        }
        if (!logged) {
            logged = true;
            log.info("连接节点{}", index);
        }
        return endpoints[index];
    }

    /**
     * 获取节点的实例ip
     * @param index 序号
     * @return 节点ip
     */
    public static String getIp(int index) {
        if (index < 0) {
            return Constants.EMPTY;
        }
        return ips[index];
    }

}
