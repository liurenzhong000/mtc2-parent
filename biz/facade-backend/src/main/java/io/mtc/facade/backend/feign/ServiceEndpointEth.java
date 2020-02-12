package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/balance")
    BigInteger balance(@RequestParam("walletAddress") String walletAddress,
                       @RequestParam("contractAddress") String contractAddress);

}
