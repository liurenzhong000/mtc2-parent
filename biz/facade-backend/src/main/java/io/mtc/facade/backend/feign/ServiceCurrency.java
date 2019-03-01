package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 币种的feign消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-currency")
public interface ServiceCurrency {

    @PutMapping("/currency")
    String insert(@RequestParam("currencyJson") String currencyJson);

    @GetMapping("/currency")
    String select(@RequestParam(value = "name", required = false) String name,
                  @RequestParam(value = "isEnable", required = false) Boolean isEnable,
                  @RequestParam(value = "baseType", required = false) Integer baseType,
                  @RequestParam(value = "pageModelStr") String pageModelStr);

    @PostMapping("/currency")
    String update(@RequestParam("currencyJson") String currencyJson);

    @PostMapping("/currency/changeStat/{id}")
    String updateStat(@PathVariable("id") Long id, @RequestParam("type") int type);

    @DeleteMapping("/currency/{id}")
    String del(@RequestParam("id") @PathVariable("id") Long id);

    /* ------------------------------ 发币分类 ------------------------------ */
    @PutMapping("/category")
    String insertCategory(@RequestParam("categoryJson") String categoryJson);

    @DeleteMapping("/category/{id}")
    String delCategory(@RequestParam("id") @PathVariable("id") Long id);

    @PostMapping("/category")
    String updateCategory(@RequestParam("categoryJson") String categoryJson);

    @GetMapping("/category")
    String selectCategory(@RequestParam(value = "name", required = false) String name,
                  @RequestParam(value = "pageModelStr") String pageModelStr);

    /* ------------------------------ 发币 ------------------------------ */
    @GetMapping("/createCurrency/backendQuery")
    String backendQueryCreated(@RequestParam(value = "symbol", required = false) String symbol,
                        @RequestParam(value = "ownerAddress", required = false) String ownerAddress,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "pageModelStr") String pageModelStr);

}
