package io.mtc.facade.api.controller;

import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 加载页信息
 *
 * @author Chinhin
 * 2018/8/14
 */
@Slf4j
@Api(description="通用", tags = {"加载页"})
@Transactional(readOnly = true)
@RequestMapping("/launchScreen")
@RestController
public class LaunchScreenController {

    @Resource
    private RedisUtil redisUtil;

    @ApiOperation(value="获取首页加载页信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
            "file: 文件和图片全路径\n" +
            "type: 点击跳转类型：1网页\n" +
            "url：点击跳转的网页链接(type为1有效)")
    })
    @GetMapping
    public Object select() {
        return ResultUtil.success(redisUtil.hget(RedisKeys.LAUNCH_SCREEN));
    }

}