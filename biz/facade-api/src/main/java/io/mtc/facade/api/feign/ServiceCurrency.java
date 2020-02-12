package io.mtc.facade.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 币种的feign消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-currency")
public interface ServiceCurrency {

    @GetMapping("/currency/appList")
    String appList();

    @GetMapping("/currency/getEthPrice")
    BigDecimal getEthPrice();

    @PutMapping("/currency")
    String apply(@RequestParam("currencyJson") String currencyJson);

}
