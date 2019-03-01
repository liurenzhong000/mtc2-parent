//package io.mtc.service.endpoint.eth.service;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.Resource;
//import java.math.BigInteger;
//
///**
// * 测试交易记录
// *
// * @author Chinhin
// * 2018/6/25
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class TransactionServiceTest {
//
//    private static final String ethTrans = "0x5ef009d7ffef62db45512bf1e3fce18a95b55afa03fd8cd4712f3dc770355ac3";
//    private static final String contractTrans = "0xaea043fe1f92e6aaff9b7cf89357ec062f089aef680db5e3c15bddd38ee62e63";
//
//    @Resource
//    private TransactionService transactionService;
//
//    @Test
//    public void transactionHandler() {
//        transactionService.transactionHandler(contractTrans, 1, BigInteger.valueOf(20), null);
//    }
//
//}