package io.mtc.service.trans.eth.feign;

import io.mtc.common.dto.EthereumRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * 节点调用的服务
 *
 * 性能比不上直接连接geth节点
 *
 * @author Chinhin
 * 2018/6/25
 */
@FeignClient("service-endpoint-eth")
public interface ServiceEndPointEth {

    @PostMapping("/ethApi")
    String ethApi(@RequestBody EthereumRequest ethereumDTO);

    @GetMapping("/getReceipt/{txHash}")
    TransactionReceipt getReceipt(@PathVariable("txHash") String txHash);

    @GetMapping("/getBlock/{blockNum}")
    EthBlock getBlock(@PathVariable("blockNum") int blockNum);

}
