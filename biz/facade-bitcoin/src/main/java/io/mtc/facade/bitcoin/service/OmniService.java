package io.mtc.facade.bitcoin.service;

import io.mtc.common.dto.CurrencyBean;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.bitcoin.util.FacadeBtcRpcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: hyp
 * @Date: 2019/3/17 10:21
 * @Description:
 */
@Service
@Slf4j
public class OmniService {

    @Value("${usdt-deposit-address}")
    private String usdtDepositAddress;

    @Autowired
    private FacadeService facadeService;

    //  USDT汇总
    public void usdtUserToMainAddress() {
        // 获取支持托管的omni币种
        List<CurrencyBean> currencyList = facadeService.getOmniCurrencyList();
        if (currencyList == null) {
            log.warn("USDT汇总 job - 未从redis中获取到支持omni的币种数据");
            return;
        }

        //  从redis中获取获取用户托管钱包集合
        Map<String, String> hostUserWalletMap = facadeService.getOmniHostUserWalletMap();
        if (hostUserWalletMap == null) {
            log.warn("USDT汇总 job - 未从redis中获取到本地托管用户数据");
            return;
        }
        Set<String> walletAddressSet = hostUserWalletMap.keySet();
        currencyList.forEach(currencyBean -> {
            for (String userAddress : walletAddressSet) {
                BigDecimal usdtBalance = FacadeBtcRpcUtil.omniBalance(userAddress, Integer.parseInt(currencyBean.getAddress()));
                if (usdtBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                //  余额达到汇总要求，进行汇总
                if (usdtBalance.compareTo(CommonUtil.getFormatAmount(currencyBean.getOutQtyToMainAddress())) >= 0) {
                    BigDecimal btcBalance = FacadeBtcRpcUtil.getBtcBalanceByAddress(userAddress);
                    if (btcBalance.compareTo(new BigDecimal("0.00000546")) < 0) {
                        log.error("USDT汇总 - 用户地址={} - 用户标记地址的交易粉尘btc余额不足 btcBalance={}", userAddress, btcBalance);
                        continue;
                    }
                    String txHash = FacadeBtcRpcUtil.omniFundedSend(userAddress, usdtDepositAddress,
                            Integer.parseInt(currencyBean.getAddress()), usdtBalance.toPlainString(), usdtDepositAddress);
                    log.info("执行汇总 - 用户地址={} - 汇总金额={} - txHash={}", userAddress, usdtBalance.toPlainString(), txHash);
                }
            }
        });

    }
}
