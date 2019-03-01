package io.mtc.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 以太坊通用请求参数
 *
 * @author Chinhin
 * 2018/6/21
 */
@Setter @Getter
public class EthereumRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jsonrpc;
    private String method;
    private Object[] params;
    private Integer type;
    private BigInteger mesh_gas;
    private String remark;

    private boolean isDeposit; // 是否充值
    private long depositBillId; // 充值的账单id

}
