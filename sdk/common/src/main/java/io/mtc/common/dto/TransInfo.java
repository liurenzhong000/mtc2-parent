package io.mtc.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 交易通知表
 *
 * @author Chinhin
 * 2018/7/9
 */
@Getter @Setter
public class TransInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    // 交易hash
    private String txHash;

    // 付款人
    private String from;

    // 收款人
    private String to;

    // 转账个数
    private String amount;

    // 单位
    private String shotName;

    // 交易状态
    private Boolean isSuccess;

    // 交易时间
    private long times;

}