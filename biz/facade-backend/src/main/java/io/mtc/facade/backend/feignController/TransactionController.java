package io.mtc.facade.backend.feignController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.feign.ServiceTransEth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 交易记录控制器
 *
 * @author Chinhin
 * 2018/6/28
 */
@Slf4j
@RestController
@Transactional(readOnly = true)
@RequestMapping("/trans")
public class TransactionController {

    @Resource
    private ServiceTransEth serviceTransEth;

    @Resource
    private RedisUtil redisUtil;

    @PreAuthorize("hasAuthority('trans:select')")
    @GetMapping
    public String select(String walletAddress, String contractAddress, String txHash,
//                         Boolean isMadeBySchedule, Boolean isPlatformUser, Integer status, Integer blockNum,
                         @ModelAttribute PagingModel pageModel) {
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setSort("times");
            pageModel.setOrder("DESC");
        }
        if (StringUtil.isNotBlank(walletAddress)) {
            walletAddress = walletAddress.toLowerCase();
        }
        if (StringUtil.isNotBlank(contractAddress)) {
            contractAddress = contractAddress.toLowerCase();
        }
        if (StringUtil.isNotBlank(txHash)) {
            txHash = txHash.toLowerCase();
        }
        return serviceTransEth.listByCondition(walletAddress, contractAddress, txHash,
                pageModel.getPageNumber(), pageModel.getPageSize(), pageModel.getOrder(), pageModel.getSort());
//        return serviceTransEth.listByCondition(walletAddress, blockNum, contractAddress, isMadeBySchedule, isPlatformUser, status,
//                pageModel.getPageNumber(), pageModel.getPageSize(), pageModel.getOrder(), pageModel.getSort());
    }

    /**
     * 删除某个交易记录
     * @param txHash 交易记录hash
     * @return 删除结果
     */
    @DeleteMapping("/{txHash}")
    public String delete(@PathVariable("txHash") String txHash) {
        if (StringUtil.isNotBlank(txHash)) {
            txHash = txHash.toLowerCase();
        }
        return serviceTransEth.delete(txHash);
    }

    /**
     * 重新加载某个交易记录
     * @param txHash 交易记录hash
     * @return 结果
     */
    @PostMapping("/reload/{txHash}")
    public String reload(@PathVariable("txHash") String txHash) {
        if (StringUtil.isNotBlank(txHash)) {
            txHash = txHash.toLowerCase();
        }
        return serviceTransEth.reload(txHash);
    }

    /**
     * 重置扫描，将重新获取所有的交易记录直到目标日期前
     * @return 结果
     */
    @PostMapping("/reloadTransaction")
    public String reloadTransaction() {
        // 清空缓存
        redisUtil.delete(RedisKeys.SCANNED_CONTINUITY_MAX);
        redisUtil.delete(RedisKeys.SCANNED_CONTINUITY_MIN);
        redisUtil.delete(RedisKeys.SCAN_LOWER_LIMIT);

        Set<String> scannedPieces = redisUtil.getKeysBeginWith(RedisKeys.SCANNED_PIECE_PREFIX);
        scannedPieces.forEach(it -> redisUtil.delete(it));

        // 清空数据库
        serviceTransEth.cleanAll();
        return ResultUtil.success("重置成功");
    }

    /**
     * 更新下限, 如果配置文件更新了下限，必须调用次此方法才生效
     * @return 结果
     */
    @PostMapping("/updateLimit")
    public String updateLimit() {
        redisUtil.set(RedisKeys.SCAN_LOWER_LIMIT, 0);
        return ResultUtil.success("更新下限成功");
    }

    /**
     * 清空某个钱包地址的所有余额缓存
     * @param walletAddress 钱包地址
     * @return 结果
     */
    @PostMapping("/clearBalanceCache/{walletAddress}")
    public String clearBalanceCache(@PathVariable String walletAddress) {
        Set<String> balanceKeys = redisUtil.getKeysBeginWith(
                RedisKeys.ETH_CONTRACT_BALANCE_PREFIX + walletAddress.toLowerCase());

        balanceKeys.forEach(it -> {
            redisUtil.delete(it);
            redisUtil.delete(RedisKeys.SET_BALANCE_BLOCK_PREFIX + it);
        });
        return ResultUtil.success("清空成功");
    }

    /**
     * 清空所有钱包的所有余额缓存
     * @return 结果
     */
    @PostMapping("/clearAllBalanceCache")
    public String clearAllBalanceCache() {
        Set<String> balanceKeys = redisUtil.getKeysBeginWith(RedisKeys.ETH_CONTRACT_BALANCE_PREFIX);
        balanceKeys.forEach(it -> {
            redisUtil.delete(it);
            redisUtil.delete(RedisKeys.SET_BALANCE_BLOCK_PREFIX + it);
        });
        return ResultUtil.success("清空成功");
    }

}