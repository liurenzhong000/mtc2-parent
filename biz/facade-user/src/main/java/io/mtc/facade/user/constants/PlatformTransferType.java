package io.mtc.facade.user.constants;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 16:53
 * @Description: 平台转账类型
 */
public enum PlatformTransferType {
    FEE(1, "申请手续费")
    ,TO_MAIN(2, "提币到主地址")
    ,TO_COLD(3, "转到冷钱包")
    ;

    private final Integer key;
    private final String value;

    PlatformTransferType(Integer key, String value) {
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
