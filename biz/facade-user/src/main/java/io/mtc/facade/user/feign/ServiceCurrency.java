package io.mtc.facade.user.feign;

import io.mtc.common.dto.CreateCurrencyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;

/**
 * 币种的feign消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-currency")
public interface ServiceCurrency {

    @GetMapping("/currency/ether2currency")
    BigInteger ether2currency(@RequestParam(value = "currencyAddress") String currencyAddress,
                              @RequestParam(value = "etherNumber") BigInteger etherNumber);

    @GetMapping("/currency/redPacketEnableCurrency")
    String redPacketEnableCurrency();

    @GetMapping("/currency/hostEnableCurrency")
    String hostEnableCurrency();

    @GetMapping("/category/all")
    String allCategory();

    @PutMapping("/createCurrency")
    String createCurrency(@RequestBody CreateCurrencyDTO createCurrencyDTO);

    @GetMapping("/createCurrency")
    String select(@RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "categoryId", required = false) Long categoryId,
                  @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                  @RequestParam(value = "pageSize", required = false) Integer pageSize,
                  @RequestParam(value = "order", required = false) String order,
                  @RequestParam(value = "sort", required = false) String sort);

}
