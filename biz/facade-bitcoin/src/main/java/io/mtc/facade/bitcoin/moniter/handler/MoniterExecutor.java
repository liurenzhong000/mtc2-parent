package io.mtc.facade.bitcoin.moniter.handler;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import io.mtc.common.constants.BitcoinTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 监控器执行类
 *
 * @author Chinhin
 * 2019-01-15
 */
@Slf4j
public class MoniterExecutor {

    private static WebSocketClient client;
    private String url;
    private BitcoinTypeEnum bitcoinType;

    public MoniterExecutor(String url, BitcoinTypeEnum bitcoinType) {
        this.url = url;
        this.bitcoinType = bitcoinType;
    }

    public boolean start() {
        if (client != null) {
            client.close();
            client = null;
        }
        try {
            client = new WebSocketClient(new URI(url), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("[ {} 交易监控] 已启动", bitcoinType.name());
                }
                @Override
                public void onMessage(String s) {
                    msgHandler(s);
                }
                @Override
                public void onClose(int i, String s, boolean b) {
                    log.info("[ {} 交易监控] 已关闭", bitcoinType.name());
                    // 重试连接
                    retry();
                }
                @Override
                public void onError(Exception e) {
                    log.info("[ {} 交易监控] 发生错误", bitcoinType.name());
                    e.printStackTrace();
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        client.connect();
        return true;
    }

    /**
     * 停止监控
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    private void retry() {
        log.info("[ {} 交易监控] 尝试重启", bitcoinType.name());
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(aBoolean -> Objects.equals(aBoolean, false))
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(10, TimeUnit.SECONDS)) // 10秒后重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)) // 重试3次后停止
                .build();
        try {
            retryer.call(this::start);
        } catch (Exception e) {
            log.info("[ {} 交易监控] 重启遇到了错误", bitcoinType.name());
            e.printStackTrace();
        }
    }

    /**
     * 处理消息
     *
     * @param args 消息
     */
    private void msgHandler(String args) {
        log.info(args);
    }

}
