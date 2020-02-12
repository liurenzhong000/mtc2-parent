package io.mtc.facade.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 币种的feign消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-notification")
public interface ServiceNotification {

    @GetMapping("/notification/history")
    String select(@RequestParam(value = "address", required = false) String address,
                  @RequestParam(value = "type", required = false) Integer type,
                  @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                  @RequestParam(value = "pageSize", required = false) Integer pageSize,
                  @RequestParam(value = "order", required = false) String order,
                  @RequestParam(value = "sort", required = false) String sort);

}