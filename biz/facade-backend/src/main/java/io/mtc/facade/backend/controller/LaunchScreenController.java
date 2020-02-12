package io.mtc.facade.backend.controller;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 加载页控制器
 *
 * @author Chinhin
 * 2018/8/13
 */
@Slf4j
@RestController
@Transactional(readOnly = true)
@RequestMapping("/launchScreen")
public class LaunchScreenController {

    @Resource
    private RedisUtil redisUtil;

    @PreAuthorize("hasAuthority('launchScreen:update')")
    @GetMapping
    public String select() {
        return ResultUtil.success(redisUtil.hget(RedisKeys.LAUNCH_SCREEN));
    }

    @PreAuthorize("hasAuthority('launchScreen:update')")
    @PostMapping
    public String update(String file, String url, Integer type) {
        redisUtil.hsetString(RedisKeys.LAUNCH_SCREEN, "file", file);
        redisUtil.hsetString(RedisKeys.LAUNCH_SCREEN, "url", url);
        redisUtil.hsetString(RedisKeys.LAUNCH_SCREEN, "type", type.toString());
        return ResultUtil.success();
    }

}
