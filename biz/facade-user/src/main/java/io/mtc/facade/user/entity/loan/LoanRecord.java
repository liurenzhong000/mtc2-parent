package io.mtc.facade.user.entity.loan;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 借款记录
 *
 * @author Chinhin
 * 2018/10/10
 */
@Getter @Setter
@Entity
public class LoanRecord extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '抵押币种'", nullable = false)
    private String mortgageToken;

    @Column(columnDefinition = "varchar(100) COMMENT '借入币种'", nullable = false)
    private String borrowToken;

    @Column(columnDefinition = "decimal(30,0) COMMENT '借入数量(wei)'", nullable = false)
    private BigInteger borrowNumber = BigInteger.ZERO;

    @Column(columnDefinition = "decimal(30,0) COMMENT '还款数量(wei)'", nullable = false)
    private BigInteger repayment = BigInteger.ZERO;

    @Column(columnDefinition = "int COMMENT '借款期限（日）'", nullable = false)
    private Integer borrowDayNum;

    @Column(columnDefinition = "decimal(10,2) COMMENT '借款利率'", nullable = false)
    private BigDecimal borrowRate;

    @Column(columnDefinition = "varchar(100) COMMENT '姓名'", nullable = false)
    private String name;

    @Column(columnDefinition = "varchar(100) COMMENT '身份证'", nullable = false)
    private String identifyNum;

    @Column(columnDefinition = "varchar(30) COMMENT '手机号'")
    private String phone;

    @Column(columnDefinition = "varchar(100) COMMENT '微信号'")
    private String wechat;

    @Column(columnDefinition = "varchar(50) COMMENT '推广人(手机号/邮箱)'")
    private String promoter;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

    @Column(columnDefinition = "varchar(100) COMMENT '序列号'", nullable = false)
    private String sn;

    @Column(columnDefinition = "int COMMENT '状态：1审核中，2审核未通过，3借款处理中(审核通过)，4待还币(已发币)，5已完成'", nullable = false)
    private Integer status;

    @Column(columnDefinition = "datetime COMMENT '审核时间'")
    private Date verifyTime;

    @Column(columnDefinition = "datetime COMMENT '放款时间'")
    private Date borrowTime;

    @Column(columnDefinition = "datetime COMMENT '应还款时间'")
    private Date shouldReturnTime;

    @Column(columnDefinition = "datetime COMMENT '还款时间'")
    private Date returnTime;

}
