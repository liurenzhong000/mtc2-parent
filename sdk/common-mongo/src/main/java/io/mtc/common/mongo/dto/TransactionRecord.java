package io.mtc.common.mongo.dto;

import io.mtc.common.constants.Constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 交易记录
 *
 * @author Chinhin
 * 2018/6/25
 */
@Setter @Getter
public class TransactionRecord implements Serializable {

    /** 交易HASH */
    @Id
    private String hash;

    /** 源地址 */
    private String from;

    /** 目标地址 */
    private String to;

    /** Input */
    private String input;

    /** 合约地址 eth为0 */
    private String contractAddress;

    /** 代币的简称(单位) */
    private String shortName;

    /** 数额 ehter有值 */
    private String value;

    /** TokenCounts 代币有值 */
    private String tokenCounts;

    /** 交易索引 */
    private BigInteger transactionIndex;

    /** nonce */
    private BigInteger nonce;

    /** GasLimit */
    private BigInteger gas;

    /** GasPrice */
    private BigInteger gasPrice;

    // 实际花费的总fee
    private BigInteger actualCostFee;

    // cumulativeGasUsed 回执更新
    private BigInteger cumulativeGasUsed;

    // 状态 回执更新
    private Integer status;

    // 区块HASH 回执更新
    private String blockHash;

    // 区块高度 回执更新
    private BigInteger blockNumber;

    /* ######################### 附加信息 ↓ ######################### */
    /**
     * 是否是通过计划任务跑的全网数据
     *
     * false表示是通过我们平台发起请求创建的
     */
    private Boolean isMadeBySchedule = true;

    /**
     * 是否是平台用户的交易记录
     */
    private Boolean isPlatformUser = true;

    /** MeshGas */
    private BigInteger meshGas;

    /** 类型: 1:Ether, 2:Mesh */
    private Integer type;

    /** 处理时间 */
    private Long times;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Long createTime;

    /** 交易类型: 1:充值，2:提现 3.发币 4.转入手续费 其他为非平台交易业务
     * @see io.mtc.common.dto.EthTransObj.TxType
     * **/
    private Integer txType = 0;
    /** 对应txType的交易id **/
    private Long txId;

    /**
     * 是否是合约的记录
     * @return true合约交易，false:ether交易
     */
    public Boolean isContract() {
        return !Constants.ETH_ADDRESS.equals(contractAddress);
    }

}
