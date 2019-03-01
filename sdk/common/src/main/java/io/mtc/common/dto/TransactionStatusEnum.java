package io.mtc.common.dto;

/**
 * 交易状态
 *
 * @author Chinhin
 * 2018/6/25
 */
public enum TransactionStatusEnum {

    UnConfirmed(0), Success(1), Failure(2);

    private final int value;

    TransactionStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
