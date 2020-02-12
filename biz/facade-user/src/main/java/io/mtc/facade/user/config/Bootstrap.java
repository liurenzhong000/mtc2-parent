package io.mtc.facade.user.config;

import io.mtc.facade.user.service.BalanceService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 系统启动初始化
 *
 * @author Chinhin
 * 2018/6/11
 */
@Component
public class Bootstrap {

    @Resource
    private BalanceService balanceService;

    @PostConstruct
    public void init() {
        balanceService.initMonitorAddress();
    }

}
