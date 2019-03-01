package io.mtc.service.trans.eth.controller;

import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.mongo.dto.TransactionRecord;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.trans.eth.mongoRepository.TransactionRepository;
import io.mtc.service.trans.eth.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 交易记录控制器
 *
 * @author Chinhin
 * 2018/6/27
 */
@Slf4j
@RestController
public class TransactionController {

    @Resource
    private TransactionRepository transactionRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private TransactionService transactionService;

    @GetMapping("/detail/{txHash}")
    public String detail(@PathVariable String txHash) {
        return ResultUtil.success(transactionRepository.findById(txHash).orElse(null));
    }

    @GetMapping("/list")
    public String list(String walletAddress, String contractAddress, @ModelAttribute PagingModel pageModel) {

        if (StringUtil.isBlank(walletAddress)) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        walletAddress = walletAddress.toLowerCase();
        // 设置默认排序
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setOrder("DESC");
            pageModel.setSort("times");
        }
        Query query = new Query();
        if (StringUtil.isNotBlank(walletAddress)) {
            Criteria tempC = new Criteria().orOperator(
                    Criteria.where("from").is(walletAddress),
                    Criteria.where("to").is(walletAddress)
            );
            query.addCriteria(tempC);
        }
        if (StringUtil.isNotBlank(contractAddress)) {
            contractAddress = contractAddress.toLowerCase();
            Criteria tempC = Criteria.where("contractAddress").is(contractAddress);
            query.addCriteria(tempC);
        }
        long count = mongoTemplate.count(query, TransactionRecord.class);
        List<TransactionRecord> transactions = mongoTemplate.find(query.with(pageModel.make()), TransactionRecord.class);
        Page<TransactionRecord> pageList = new PageImpl<>(transactions, pageModel.make(), count);
        return PagingResultUtil.list(pageList);
    }

    @GetMapping("/listByCondition")
    public String listByCondition(String walletAddress, String contractAddress, String txHash,
                                  @ModelAttribute PagingModel pageModel) {
        Query query = new Query();
        if (StringUtil.isNotBlank(walletAddress)) {
            Criteria tempC = new Criteria().orOperator(
                    Criteria.where("from").is(walletAddress),
                    Criteria.where("to").is(walletAddress)
            );
            query.addCriteria(tempC);
        }
        if (StringUtil.isNotBlank(txHash)) {
            Criteria tempC = Criteria.where("hash").is(txHash);
            query.addCriteria(tempC);
        }
//        if (blockNum != null) {
//            Criteria tempC = Criteria.where("blockNumber").is(BigInteger.valueOf(blockNum));
//            query.addCriteria(tempC);
//        }
        if (StringUtil.isNotBlank(contractAddress)) {
            Criteria tempC = Criteria.where("contractAddress").is(contractAddress);
            query.addCriteria(tempC);
        }
//        if (isMadeBySchedule != null) {
//            Criteria tempC = Criteria.where("isMadeBySchedule").is(isMadeBySchedule);
//            query.addCriteria(tempC);
//        }
//        if (isPlatformUser != null) {
//            Criteria tempC = Criteria.where("isPlatformUser").is(isPlatformUser);
//            query.addCriteria(tempC);
//        }
//        if (status != null) {
//            Criteria tempC = Criteria.where("status").is(status);
//            query.addCriteria(tempC);
//        }

        long count = mongoTemplate.count(query, TransactionRecord.class);
        List<TransactionRecord> transactions = mongoTemplate.find(query.with(pageModel.make()), TransactionRecord.class);
        Page<TransactionRecord> pageList = new PageImpl<>(transactions, pageModel.make(), count);
        return PagingResultUtil.list(pageList);
    }

    @DeleteMapping("/{txHash}")
    public String delete(@PathVariable String txHash) {
        transactionRepository.deleteById(txHash);
        return ResultUtil.success("删除成功");
    }

    @PostMapping("/reload/{txHash}")
    public String reload(@PathVariable String txHash) {
        boolean result = transactionService.txCheck(txHash);
        if (result) {
            return ResultUtil.success(transactionRepository.findById(txHash));
        } else {
            return ResultUtil.error("重新加载错误，请确认交易hash正确");
        }
    }

    @DeleteMapping("/cleanAll")
    public String cleanAll() {
        mongoTemplate.dropCollection(TransactionRecord.class);
        return ResultUtil.success("清空数据完成");
    }

}
