//package io.mtc.service.notification.util;
//
//import io.mtc.common.dto.TransInfo;
//import io.mtc.service.notification.service.NotificationService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.Resource;
//
///**
// * test
// *
// * @author Chinhin
// * 2018/7/11
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class MessageUtilTest {
//
//    @Resource
//    private MessageUtil messageUtil;
//
//    @Resource
//    private NotificationService notificationService;
//
//    @Test
//    public void test() {
//        System.out.println(messageUtil.getMessage("shenbin", new Object[]{28}, 1));
//    }
//
//    @Test
//    public void testNotification() {
//        TransInfo transInfo = new TransInfo();
//        transInfo.setShotName("MESH");
//        transInfo.setTimes(System.currentTimeMillis());
//        transInfo.setIsSuccess(true);
//        transInfo.setAmount("3000000000000000000");
//        transInfo.setTxHash("0xc8119c7d18fdd1d74b6296586d57ef54b262e77461aa3701960472a6ed644d8f");
//        transInfo.setFrom("0x62ba9121442c44eac9c99f1619692ed81eb3be62");
//        transInfo.setTo("0x9642474e930559621e8f94bda76ec8a28c04a43e");
//        notificationService.sendTransNotification(transInfo);
//    }
//
//}