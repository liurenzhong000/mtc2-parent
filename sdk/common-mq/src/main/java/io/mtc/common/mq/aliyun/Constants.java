package io.mtc.common.mq.aliyun;

import lombok.Getter;
import lombok.Setter;

/**
 * 阿里云MQ常量
 *
 * @author Chinhin
 * 2018/7/28
 */
public class Constants {

    public static final String AccessKey = "LTAIs4hPmgH7NkwR";
    public static final String SecretKey = "8RtkSX8hNzG4RDRmZW2fZaIUWQdxe4";

    // prod，test
    @Setter
    private static String env = "prod";
    private static final String DEV_ONSAddr = "http://MQ_INST_1064822407388817_BaYMB4VE.mq-internet-access.mq-internet.aliyuncs.com:80";
    private static final String PROD_ONSADDR = "http://ap-southeastaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal";
    private static final String TEST_ONSADDR = "http://MQ_INST_1064822407388817_BaYMB4VE.mq-internet-access.mq-internet.aliyuncs.com:80";

    public static String getONSAddr() {
        if ("dev".equals(env)) {
            return DEV_ONSAddr;
        } else if ("test".equals(env)) {
            return TEST_ONSADDR;
        } else {
            return PROD_ONSADDR;
        }
    }

    public enum Tag {
        // 交易推送通知
        ETH_TRANS_NOTIFI("PID_bhb_biz_trans", "CID_BHB_ETH_TRANS_NOTIFI", "GID_ETH_TRANS_NOTIFI")
        // 平台业务交易完成（充值、提现、转入手续费）
        ,ETH_BIZ_TRANS_COMPLETE("PID_bhb_biz_trans", "CID_BHB_ETH_BIZ_TRANS_COMPLETE", "GID_ETH_BIZ_TRANS_COMPLETE")
        // 平台业务交易发起（充值、提现、转入手续费）
        ,ETH_BIZ_TRANS_PENDING("PID_bhb_biz_trans", "CID_BHB_ETH_BIZ_TRANS_PENDING", "GID_ETH_BIZ_TRANS_PENDING")
        // 发币完成
        ,ETH_BIZ_CREATION_COMPLETE("PID_bhb_biz_trans", "CID_BHB_ETH_BIZ_CREATION_COMPLETE", "GID_ETH_BIZ_CREATION_COMPLETE")
        // 监控到有向平台托管用户的钱包地址充值
        ,ETH_BIZ_HOST_WALLET_TRANS("GID_ETH_BIZ_HOST_WALLET_TRANS", "GID_ETH_BIZ_HOST_WALLET_TRANS", "GID_ETH_BIZ_HOST_WALLET_TRANS")
        ,ETH_BIZ_HOST_WALLET_TRANS_PROD("PID_bhb_biz_trans", "CID_BHB_ETH_BIZ_HOST_WALLET_TRANS_PROD", "GID_ETH_BIZ_HOST_WALLET_TRANS_PROD")
        ;

        @Getter
        private final String consumerId;
        @Getter
        private final String producerId;
        @Getter
        private final String groupId;

        Tag(String producerId, String consumerId, String groupId) {
            this.producerId = producerId;
            this.consumerId = consumerId;
            this.groupId = groupId;
        }
    }

    // 需要与控制台一致
    public enum Topic {
        MTC_BIZ_TRANS("bhb_biz_trans")
        ;

        @Getter
        private final String name;

        Topic(String name) {
            this.name = name;
        }

    }

}