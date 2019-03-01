package io.mtc.facade.bitcoin.moniter;

import io.mtc.common.constants.BitcoinTypeEnum;
import io.mtc.facade.bitcoin.moniter.handler.MoniterExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 比特币的交易监控
 *
 * @author Chinhin
 * 2019-01-15
 */
@Slf4j
@Deprecated
//@Component
public class BtcTransactionMonitor {

    @Value("${btc.api.url:http://47.74.154.247/api/}")
    private String url;

    private MoniterExecutor moniterExecutor;

    @PostConstruct
    public void init() {
        moniterExecutor = new MoniterExecutor(url, BitcoinTypeEnum.BTC);
        moniterExecutor.start();
    }

    @PreDestroy
    public void destroy() {
        moniterExecutor.close();
        moniterExecutor = null;
    }

}
