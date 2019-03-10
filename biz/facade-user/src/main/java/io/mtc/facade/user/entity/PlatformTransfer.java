package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.constants.PlatformTransferStatus;
import io.mtc.facade.user.constants.PlatformTransferStatusConverter;
import io.mtc.facade.user.constants.PlatformTransferType;
import io.mtc.facade.user.constants.PlatformTransferTypeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 16:31
 * @Description: 平台内的相关记录
 */
@Getter
@Setter
@Entity
public class PlatformTransfer extends BaseEntity {

    @Column(columnDefinition = "int COMMENT '类型 1：申请手续费 2：提币到主地址 3：转到冷钱包'")
    @Convert(converter = PlatformTransferTypeConverter.class)
    private PlatformTransferType type;

    @Column(columnDefinition = "int COMMENT '状态 1：失败 2：已广播 3：已打包 4：已确认'")
    @Convert(converter = PlatformTransferStatusConverter.class)
    private PlatformTransferStatus status;

    @Column(columnDefinition = "varchar(100) COMMENT '币种地址'")
    private String currencyAddress;

    @Column(columnDefinition = "varchar(100) COMMENT '转出地址'", nullable = false)
    private String fromAddress;

    @Column(columnDefinition = "varchar(100) COMMENT '转入地址'", nullable = false)
    private String toAddress;

    @Column(columnDefinition = "decimal(30,0) COMMENT '转账数量(wei) '")
    private BigInteger qty;

    @Column(columnDefinition = "varchar(200) COMMENT '交易hash'", nullable = false, unique = true)
    private String txHash;

    @Column(columnDefinition = "decimal(30,0) COMMENT '交易费(wei) '")
    private BigInteger txFee;

    @Column(columnDefinition = "decimal(30,0) COMMENT '转账使用gasPrice(wei) '")
    private BigInteger gasPrice;

    @Column(columnDefinition = "decimal(30,0) COMMENT '转账使用gasLimit(wei) '")
    private BigInteger gasLimit;

//    @JSONField(serialize = false)
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.ALL}, optional = false)
//    @JoinColumn(name = "balance_id", columnDefinition = "bigint COMMENT '关联余额'")
//    private UserBalance balance;

    @Column(columnDefinition = "timestamp COMMENT '任务过期时间'")
    private Date jobExpireDate;

}
