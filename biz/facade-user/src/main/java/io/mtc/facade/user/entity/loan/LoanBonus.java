package io.mtc.facade.user.entity.loan;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.facade.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

/**
 * 贷款提成记录
 *
 * @author Chinhin
 * 2018/10/16
 */
@Getter @Setter
@Entity
public class LoanBonus extends BaseEntity {

    @Column(columnDefinition = "int COMMENT '提成状态：1未发放，2已发放'", nullable = false)
    private Integer bonusStatus = 1;

    @Column(columnDefinition = "decimal(30,0) COMMENT '奖励数量(wei)'")
    private BigInteger bonus;

    @Column(columnDefinition = "datetime COMMENT '发放时间'")
    private Date sendTime;

    @Column(columnDefinition = "varchar(100) COMMENT '奖励所属的借款记录编号'", nullable = false)
    private String sn;

    @Column(columnDefinition = "varchar(50) COMMENT '推广人(手机号/邮箱)'")
    private String promoter;

    @Column(columnDefinition = "varchar(50) COMMENT '被邀请人'")
    private String invitee;

    @JSONField(serialize = false)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "bigint COMMENT '关联用户'")
    private User user;

}