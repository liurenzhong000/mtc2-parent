package io.mtc.facade.user.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 创建eos手续费
 *
 * @author Chinhin
 * 2018/9/11
 */
@Setter
@Getter
public class EosFee implements Serializable {

    // 总共需要多少美元
    private BigDecimal money;
    // 总共需要多少人民币
    private BigDecimal moneyCny;

    // 需要多少个eos
    private BigInteger needEos;
    // 需要多少个mtc
    private BigInteger needMtc;
    // 需要多少个eth
    private BigInteger needEth;

    /* 用户持有币信息 */
    private BigInteger eosBalance = BigInteger.ZERO;
    private BigInteger mtcBalance = BigInteger.ZERO;
    private BigInteger ethBalance = BigInteger.ZERO;

}
