package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 币种的feign消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("service-notification")
public interface ServiceNotification {

    @GetMapping("/notification")
    String select(@RequestParam(value = "address", required = false) String address,
                  @RequestParam(value = "type", required = false) Integer type,
                  @RequestParam(value = "txHash", required = false) String txHash,
                  @RequestParam(value = "pageModelStr") String pageModelStr);

    @DeleteMapping("/notification/{id}")
    String del(@RequestParam("id") @PathVariable("id") Long id);

    /* -------------------------------  template  ------------------------------- */
    @PutMapping("/notificationTemplate")
    String insertTemplate(@RequestParam("templateJson") String templateJson);

    @DeleteMapping("/notificationTemplate/{id}")
    String delTemplate(@RequestParam("id") @PathVariable("id") Long id);

    @GetMapping("/notificationTemplate")
    String selectTemplate(@RequestParam(value = "title", required = false) String title,
                  @RequestParam(value = "pageModelStr") String pageModelStr);

    @PostMapping("/notificationTemplate")
    String updateTemplate(@RequestParam("templateJson") String templateJson);

    @PostMapping("/notificationTemplate/push/{id}")
    String pushTemplate(@RequestParam("id") @PathVariable("id") Long id);

}