package io.mtc.facade.user.constants;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 16:53
 * @Description: 平台转账状态
 */
public enum PlatformTransferStatus {
    FAIL(1, "失败")
    ,PACKAGE(2, "已打包")//已打包，准备发mq
    ,SEND(3, "已发送")//已发mq消息到service-endpoint-eth处理
    ,AFFIRM(4, "已确认")//交易已经确认
    ;

    private final Integer key;
    private final String value;

    PlatformTransferStatus(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
