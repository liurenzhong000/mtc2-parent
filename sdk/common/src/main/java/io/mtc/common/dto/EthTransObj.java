package io.mtc.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 中间件传递的 交易对象
 *
 * @author Chinhin
 * 2018/7/27
 */
@Getter @Setter
public class EthTransObj implements Serializable {

    private static final long serialVersionUID = 1L;

    private String signedTransactionData;

    /** 交易类型: 1:充值，2:提现 3:发币 4.转入手续费 其他为非平台交易业务
     * @see TxType
     * **/
    private Integer txType;
    /** 对应txType的交易id **/
    private Long txId;
    /**
     * 状态：1成功，2失败
     * @see Status
     */
    private Integer status;
    // 代币地址
    private String currencyAddress;
    // 转账金额
    private String amount;

    // 主链类型，1eth，2bch，3eos，4btc
    private Integer coinType;

    public enum TxType{
        EMPTY("空，占位"), DEPOSIT("充值"), WITHDRAW("提现"), CREATE_CONTRACT("发币"), FEE_TO("转入手续费");
        String desc;

        TxType(String desc) {
            this.desc = desc;
        }
    }

    public enum Status{
        EMPTY("空，占位"), SUCCESS("成功"), FAIL("失败");
        String desc;

        Status(String desc) {
            this.desc = desc;
        }
    }
}
