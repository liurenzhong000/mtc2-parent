package io.mtc.facade.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 交易记录相关
 *
 * @author Chinhin
 * 2018/6/27
 */
@FeignClient("service-trans-eth")
public interface ServiceTransEth {

    @GetMapping("/list")
    String list(@RequestParam("walletAddress") String walletAddress,
                @RequestParam(value = "contractAddress", required = false) String contractAddress,
                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                @RequestParam(value = "order", required = false) String order,
                @RequestParam(value = "sort", required = false) String sort);

    @GetMapping("/detail/{txHash}")
    String detail(@RequestParam("txHash") @PathVariable("txHash") String txHash);
}
