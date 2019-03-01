package io.mtc.facade.bitcoin.apiController;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.dto.RequestResult;
import io.mtc.facade.bitcoin.service.TxService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 作为提供者的接口
 *
 * @author Chinhin
 * 2019-01-17
 */
@RequestMapping("/tx")
@RestController
@Slf4j
public class TxController {

    @Resource
    private TxService txService;

    /**
     * 提现接口
     * @param bitcoinType 类型
     * @param targetAddress 目标地址
     * @param billId 账单ID
     * @param amount 金额
     * @return 调用结果
     */
    @PostMapping("/{bitcoinType}/withdraw")
    public RequestResult withdraw(@PathVariable BitcoinTypeEnum bitcoinType, String targetAddress, Long billId, String amount) throws Exception {
        log.info("收到提现请求，bill:{}, targetAddress:{}, amount:{}, type:{}", billId, targetAddress, amount, bitcoinType.name());
        if (bitcoinType == BitcoinTypeEnum.BTC) {
            return txService.btcWithdraw(bitcoinType, targetAddress, Coin.parseCoin(amount), billId);
        } else {
            return null;
        }
    }

}
