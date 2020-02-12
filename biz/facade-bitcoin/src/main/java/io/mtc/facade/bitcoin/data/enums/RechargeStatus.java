package io.mtc.facade.bitcoin.data.enums;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 10:14
 * @Description: 充值状态
 */
public enum RechargeStatus {
    INIT(1, "扫描")
    ,SEND(2, "已发送MQ");

    private final Integer key;
    private final String value;

    RechargeStatus(Integer key, String value) {
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
