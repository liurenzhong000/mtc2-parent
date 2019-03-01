package io.mtc.facade.user.constants;

/**
 * json错误
 *
 * @author Chinhin
 * 2018/6/22
 */
public enum BillStatus {

    PENDING(1, "排队中")
    ,PROCESSING(2, "处理中")
    ,SUCCESS(3, "成功")
    ,FAILURE(4, "失败")
    ;

    private final Integer key;
    private final String value;

    BillStatus(Integer key, String value) {
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
