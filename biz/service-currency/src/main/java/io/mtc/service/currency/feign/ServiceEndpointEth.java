package io.mtc.service.currency.feign;

import io.mtc.common.dto.EthTransObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;

/**
 * 以太坊交易提供者
 *
 * @author Chinhin
 * 2018/6/21
 */
@FeignClient("service-endpoint-eth")
public interface ServiceEndpointEth {

    @GetMapping("/getGasPrice")
    BigInteger gasPrice();

    @PostMapping("/packageTrans")
    String packageTrans(@RequestBody EthTransObj ethTransObj);

    @GetMapping("/getGasPriceAndNonce")
    BigInteger[] getGasPriceAndNonce(@RequestParam(value = "walletAddress") String walletAddress);

}
