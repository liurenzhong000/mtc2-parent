package io.mtc.facade.user.entity.loan;

import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 借款功能配置
 *
 * @author Chinhin
 * 2018/10/10
 */
@Getter @Setter
@Entity
public class LoanConfig extends BaseEntity {

    @Column(columnDefinition = "varchar(500) COMMENT '抵押币种'", nullable = false)
    private String mortgageToken;

    @Column(columnDefinition = "varchar(500) COMMENT '借入币种'", nullable = false)
    private String borrowToken;

    @Column(columnDefinition = "varchar(500) COMMENT '借款期限'", nullable = false)
    private String borrowTime;

    @Column(columnDefinition = "varchar(500) COMMENT '借款利率(%)'", nullable = false)
    private String borrowRate;

}
