package io.mtc.facade.user.constants;

import java.util.EnumSet;
import java.util.Iterator;

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
    ,WAIT_AUDIT(5, "待审核")
    ,AUDIT_FAILURE(6, "审核不通过");

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

    public static BillStatus getByKey(Integer key) {
        EnumSet<BillStatus> set = EnumSet.allOf(BillStatus.class);
        Iterator<BillStatus> iter = set.iterator();
        while (iter.hasNext()) {
            BillStatus status = iter.next();
            if (status.key.equals(key)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid value " + key + " for " + BillStatus.class.getName( ) + ", must be < " + set.size());
    }
}
