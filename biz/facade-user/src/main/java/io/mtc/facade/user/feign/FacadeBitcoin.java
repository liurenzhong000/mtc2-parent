package io.mtc.facade.user.feign;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.dto.RequestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

/**
 * Omni交易提供者
 *
 * @author Chinhin
 * 2018/6/21
 */
@FeignClient("facade-bitcoin")
public interface FacadeBitcoin {

    @GetMapping("/{bitcoinType}/tx/{txHash}")
    Object txDetail(@PathVariable("bitcoinType") BitcoinTypeEnum bitcoinType, @PathVariable("txHash") String txHash);

    @PostMapping("/tx/{bitcoinType}/withdraw")
    RequestResult withdraw(@PathVariable("bitcoinType") BitcoinTypeEnum bitcoinType,
                           @RequestParam("targetAddress") String targetAddress,
                           @RequestParam("billId") Long billId,
                           @RequestParam("amount") String amount);


    @GetMapping("/{bitcoinType}/newAddress")
    Object getNewAddress(@PathVariable("bitcoinType") BitcoinTypeEnum bitcoinType);
}
