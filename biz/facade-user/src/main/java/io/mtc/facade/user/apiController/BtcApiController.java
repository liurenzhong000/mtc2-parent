package io.mtc.facade.user.apiController;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.UserWallet;
import io.mtc.facade.user.feign.FacadeBitcoin;
import io.mtc.facade.user.repository.UserWalletRepository;
import io.mtc.facade.user.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.*;

/**
 * 与yex交互用的btc相关接口
 *
 * @author Chinhin
 * 2019-01-25
 */
@Slf4j
@ApiIgnore
@Transactional(readOnly = true)
@RequestMapping("/btcApi")
@RestController
public class BtcApiController {

    @Resource
    private FacadeBitcoin facadeBitcoin;

    @Resource
    private BalanceService balanceService;

    @Resource
    private UserWalletRepository userWalletRepository;

    @Transactional
    @PostMapping("/transactionNotify")
    public Object transactionNotify(String txHash, String walletAddress) {
        log.info("收到转账信息 钱包地址：{} 交易hash：{}", walletAddress, txHash);
        UserWallet userWallet = userWalletRepository.findByWalletAddress(walletAddress);
        if (userWallet == null) {
            return ResultUtil.successObj("无效的钱包地址");
        }

        HashMap result = (HashMap) facadeBitcoin.txDetail(BitcoinTypeEnum.BTC, txHash);
        HashMap resultMap = (HashMap) result.get("result");
        Integer status = (Integer) result.get("status");
        if (status != 200) {
            return ResultUtil.errorObj("获取交易详情发生错误", 500);
        }

        // 钱包地址的check
        List<Map<String, Object>> vin = (List<Map<String, Object>>) resultMap.get("vin");
        Coin amount = Coin.ZERO;
        Set<String> fromAddresses = new HashSet<>();
        for (Map<String, Object> temp : vin) {
            if (temp.get("addr").toString().equals(walletAddress)) {
                amount = amount.subtract(Coin.parseCoin(temp.get("value").toString()));
            } else {
                fromAddresses.add((String) temp.get("addr"));
            }
        }

        List<Map<String, Object>> vout = (List<Map<String, Object>>) resultMap.get("vout");
        for (Map<String, Object> temp : vout) {
            if (temp.get("addr").toString().equals(walletAddress)) {
                amount = amount.add(Coin.parseCoin(temp.get("value").toString()));
            }
        }
        if (amount.isZero()) {
            return ResultUtil.errorObj("该交易未对钱包余额造成变化", 500);
        } else if (amount.isPositive()) { // 大于0， 表示转入
            balanceService.btcWalletDesposit(resultMap, walletAddress, StringUtil.set2str(fromAddresses, 150), amount);
        } else { // 小于0 表示转出
            return ResultUtil.errorObj("提现暂不支持", 500);
        }
        return ResultUtil.successObj();
    }

}
