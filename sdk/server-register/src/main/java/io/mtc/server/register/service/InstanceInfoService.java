package io.mtc.server.register.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContextHolder;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.sms.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时将服务实例信息放入缓存
 *
 * @author Chinhin
 * 2018/7/19
 */
@Slf4j
@Service
public class InstanceInfoService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    private void refreshInstanceInfo() {
        List<Application> applications = EurekaServerContextHolder.getInstance().getServerContext().getRegistry().getSortedApplications();
        // 备用注册中心
        if (applications.size() == 0) {
            return;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Application tempApp : applications) {
            Map<String, Object> tempAppResult = new HashMap<>();
            tempAppResult.put("name", tempApp.getName());
            int downNum = 0;
            int upNum = 0;
            for (InstanceInfo tempInstance : tempApp.getInstances()) {
                switch (tempInstance.getStatus()) {
                    case UP:
                        upNum ++;
                        break;
                    case DOWN:
                        downNum ++;
                        break;
                }
            }
            tempAppResult.put("upNum", upNum);
            tempAppResult.put("downNum", downNum);
            result.add(tempAppResult);
            if ("prod".equals(applicationContext.getEnvironment().getActiveProfiles()[0])) {
                if (upNum == 0) {
                    if ("SERVICE-ENDPOINT-ETH".equals(tempApp.getName())) {
                        SmsUtil.simpleSend("8617623006930", "444444", 2);
                    }
                }
            }
        }
        redisUtil.set(RedisKeys.SERVER_INSTANCE_INFO, result);
    }

}
