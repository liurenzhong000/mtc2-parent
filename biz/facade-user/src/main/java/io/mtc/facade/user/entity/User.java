package io.mtc.facade.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.constants.Constants;
import io.mtc.common.jpa.entity.BaseEntity;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.loan.LoanBonus;
import io.mtc.facade.user.entity.loan.LoanRecord;
import io.mtc.facade.user.entity.wheel.WheelRecord;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 用户表
 *
 * @author Chinhin
 * 2018/7/23
 */
@Getter @Setter
@Entity
public class User extends BaseEntity {

    @Column(columnDefinition = "varchar(50) COMMENT '邮箱号'", unique = true)
    private String email;

    @Column(columnDefinition = "varchar(50) COMMENT '手机号'", unique = true)
    private String phone;

    @JSONField(serialize = false)
    @Column(columnDefinition = "varchar(100) COMMENT '登录密码'", nullable = false)
    private String loginPassword;

    @JSONField(serialize = false)
    @Column(columnDefinition = "varchar(100) COMMENT '资金密码'")
    private String fundPassword = Constants.EMPTY;

    @Column(columnDefinition = "varchar(200) COMMENT '头像'")
    private String photo = Constants.EMPTY;

    @Column(columnDefinition = "varchar(50) COMMENT '昵称'")
    private String nick = Constants.EMPTY;

    @JSONField(serialize = false)
    @Column(columnDefinition = "varchar(100) COMMENT 'token'")
    private String token = Constants.EMPTY;

    // 不能提现时间
    private Date canWithdrawTime;

    @Column(columnDefinition = "varchar(50) COMMENT '推广人(手机号/邮箱)'")
    private String promoter;

    @Column(columnDefinition = "int COMMENT '状态'")
    private Integer wheelNum = 0;

    @Column(columnDefinition = "int COMMENT '今天通过转账获得的抽奖次数'")
    private Integer todayGetWheelNumByTransfer = 0;

    @Column(columnDefinition = "int COMMENT '今天通过分享获得的抽奖次数'")
    private Integer todayGetWheelNumByShare = 0;

    @Column(columnDefinition = "varchar(200) COMMENT '融云的token'")
    private String rongyunToken = Constants.EMPTY;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserKeystore> keystores;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserBalance> balances;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserWallet> wallets;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RedEnvelope> sendRedEnvelopes;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceivedRedEnvelope> receivedRedEnvelopes;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contact> contacts;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanRecord> loanRecords;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanBonus> loanBonuses;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WheelRecord> wheelRecords;

    public String getUserName() {
        if (StringUtil.isNotEmpty(nick)) {
            return nick;
        } if (StringUtil.isNotEmpty(phone)) {
            return StringUtil.snow(phone);
        } else {
            return StringUtil.snow(email);
        }
    }

    /**
     * 根据coin类型获取对应的分配的钱包地址
     * @param currencyType coin类型
     * @return 没有的情况下返回null
     */
    public String getWalletAddressByCurrencyType(Integer currencyType) {
        for (UserWallet it : getWallets()) {
            if (it.getCurrencyType().equals(currencyType)) {
                return it.getWalletAddress();
            }
        }
        return null;
    }

}
