package io.mtc.facade.api.feign;

import io.mtc.common.dto.EthereumRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/ethApi")
    String ethApi(@RequestBody EthereumRequest ethereumDTO);

    @PostMapping("/contractApi")
    String contractApi(@RequestBody EthereumRequest ethereumDTO);

    @GetMapping("/getTransactionCount/{address}")
    String getTransactionCount(@RequestParam("address") @PathVariable("address") String address);

    @GetMapping("/getSymbolByAddress/{address}")
    String getSymbolByAddress(@RequestParam("address") @PathVariable("address") String address);

    @GetMapping("/getNameByAddress/{address}")
    String getNameByAddress(@RequestParam("address") @PathVariable("address") String address);

    @GetMapping("/balanceDecimals")
    Integer getBalanceDecimals(String contractAddress);

}
