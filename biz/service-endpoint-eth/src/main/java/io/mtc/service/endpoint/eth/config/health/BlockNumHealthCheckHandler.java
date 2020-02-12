package io.mtc.service.endpoint.eth.config.health;

import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 健康监测处理器
 * 将健康状态传给eureka注册中心
 *
 * @author Chinhin
 * 2018/6/26
 */
@Component
public class BlockNumHealthCheckHandler implements HealthCheckHandler {

    @Resource
    private BlockNumHealthIndicator blockNumHealthIndicator;

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus instanceStatus) {
        Status status = blockNumHealthIndicator.health().getStatus();
        if (status.equals(Status.UP)) {
            return InstanceInfo.InstanceStatus.UP;
        } else {
            return InstanceInfo.InstanceStatus.DOWN;
        }
    }
}
