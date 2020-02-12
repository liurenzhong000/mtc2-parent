package io.mtc.facade.user.constants;

/**
 * json错误
 *
 * @author Chinhin
 * 2018/6/22
 */
public enum BillType {

    DEPOSIT(1, "充值")
    ,WITHDRAW(2, "提现")
    ,SEND_RED_ENVELOPE(3, "发红包")
    ,GRAB_RED_ENVELOPE(4, "收红包")
    ,TRANSFER_FROM(5, "转账-转出")
    ,TRANSFER_TO(6, "转账-收款")
    ,CREATE_CONTRACT(7, "创建代币")
    ,CREATE_EOS(8, "创建EOS账户")
    ,TRANSFER_PAY(100, "云矿账户支付")
    ,TRANSFER_WITHDRAW(101, "云矿账户提现")
    ,TRANSFER_INCOME(102, "云矿商家收款")
    ,DIVIDEND(201, "分红")
    ;

    private final Integer key;
    private final String value;

    BillType(Integer key, String value) {
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
