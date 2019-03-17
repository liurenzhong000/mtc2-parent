package io.mtc.facade.user.service;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import io.mtc.common.dto.OmniHostWalletAddressTrans;
import io.mtc.common.mq.aliyun.MsgHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 14:38
 * @Description: omni钱包本地托管钱包 充值
 */
@Slf4j
@Service
@Transactional
public class OmniRechargeService implements MsgHandler {

    @Autowired
    private BalanceService balanceService;

    @Override
    public Action doConsume(String json) {
        log.info("获取到USDT充值消息 - json={}", json);
        OmniHostWalletAddressTrans trans = JSON.parseObject(json, OmniHostWalletAddressTrans.class);
        if (trans.getCurrencyType().equals(5) && trans.getPropertyId().equals("31")) {
            balanceService.usdtWalletDesposit(trans.getTxHash(), trans.getWalletAddress(), trans.getFromAddress(), trans.getAmount());
        }
        return Action.CommitMessage;
    }
}
