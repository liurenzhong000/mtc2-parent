package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 设置相关的消费
 *
 * @author Chinhin
 * 2018/6/20
 */
@FeignClient("service-setting")
public interface ServiceSetting {

    @PutMapping("/custom")
    String insert(@RequestParam("title") String title,
                  @RequestParam("content") String content,
                  @RequestParam("linkTag") String linkTag);

    @DeleteMapping("/custom/{id}")
    String delete(@RequestParam("id") @PathVariable("id") Long id);

    @PostMapping("/custom")
    String update(@RequestParam("id") Long id,
                  @RequestParam("title") String title,
                  @RequestParam("content") String content,
                  @RequestParam("linkTag") String linkTag);

    @GetMapping("/custom")
    String select(@RequestParam(value = "title", required = false) String title,
                  @RequestParam(value = "linkTag", required = false) String linkTag,
                  @RequestParam(value = "pageModelStr") String pageModelStr);

    @GetMapping("/custom/{linkTag}")
    String detail(@RequestParam("linkTag") @PathVariable("linkTag") String linkTag);

}
