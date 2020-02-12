package io.mtc.facade.bitcoin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 托管用户的消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("facade-user")
public interface FacadeUser {

    @GetMapping("/api/bill/{id}")
    String billDetail(@PathVariable("id") Long id);

}
