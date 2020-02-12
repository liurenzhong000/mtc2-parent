package io.mtc.service.endpoint.eth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * web3j工厂类
 *
 * @author Chinhin
 * 2018/7/5
 */
@Slf4j
@Scope("singleton")
@Component
public class Web3jPool {

    @Value("${endpoint.index}")
    private int endpointIndex;

    @Value("${web3j.poolSize}")
    private int poolSize;

    public int getEndpointIndex() {
        return endpointIndex;
    }

    private static LinkedList<Web3j> list = new LinkedList<>();

    private static AtomicBoolean isResting = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        try {
            synchronized(this) {
                if (!isResting.get()) {
                    return;
                }
                // 初始化15个链接
                for (int i = 0; i < poolSize; i++) {
                    Web3j conn = Web3j.build(new HttpService(EndpointUrlFactory.getEndpointAtIndex(endpointIndex)));
                    list.add(conn);
                }
                this.notifyAll();
                isResting.set(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void rest() {
        isResting.set(true);
        while (list.size() > 0) {
            Web3j web3j = list.removeFirst();
            web3j.shutdown();
        }
    }

    public Web3j getNewConnection() {
        return Web3j.build(new HttpService(EndpointUrlFactory.getEndpointAtIndex(endpointIndex)));
    }

    public Web3j getConnection() throws RuntimeException, InterruptedException {
        synchronized (this) {
            // 获取连接超时秒数
            long remaining = 2000;
            long future = System.currentTimeMillis() + remaining;

            while (list.size() == 0 && remaining > 0) {
                this.wait(remaining);
                remaining = future - System.currentTimeMillis();
            }
            Web3j result = null;
            if (list.size() > 0) {
                result = list.removeFirst();
            }
            return result;
        }
    }

    public synchronized void close(Web3j conn) {
        list.add(conn);
        this.notifyAll();
    }

}
