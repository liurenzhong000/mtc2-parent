package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 交易记录
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-trans-eth")
public interface ServiceTransEth {

    @GetMapping("/listByCondition")
    String listByCondition(@RequestParam(value = "walletAddress", required = false) String walletAddress,
//                           @RequestParam(value = "blockNum", required = false) Integer blockNum,
                           @RequestParam(value = "contractAddress", required = false) String contractAddress,
                           @RequestParam(value = "txHash", required = false) String txHash,
//                           @RequestParam(value = "isMadeBySchedule", required = false) Boolean isMadeBySchedule,
//                           @RequestParam(value = "isPlatformUser", required = false) Boolean isPlatformUser,
//                           @RequestParam(value = "status", required = false) Integer status,
                           @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                           @RequestParam(value = "pageSize", required = false) Integer pageSize,
                           @RequestParam(value = "order", required = false) String order,
                           @RequestParam(value = "sort", required = false) String sort);

    @DeleteMapping("/{txHash}")
    String delete(@RequestParam("txHash") @PathVariable("txHash") String txHash);

    @PostMapping("/reload/{txHash}")
    String reload(@RequestParam("txHash") @PathVariable("txHash") String txHash);

    @DeleteMapping("/cleanAll")
    String cleanAll();

}
