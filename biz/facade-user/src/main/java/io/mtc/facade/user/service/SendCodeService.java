package io.mtc.facade.user.service;

import io.mtc.common.constants.MTCError;
import io.mtc.common.sms.util.MxtSmsUtil;
import io.mtc.common.sms.util.SmsUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.CodeRecord;
import io.mtc.facade.user.entity.EmailCodeRecord;
import io.mtc.facade.user.repository.CodeRecordRepository;
import io.mtc.facade.user.repository.EmailCodeRecordRepository;
import io.mtc.facade.user.util.AliyunMailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 发送短信验证码
 *
 * @author Chinhin
 * 2018/7/23
 */
@Service
public class SendCodeService {

    @Resource
    private CodeRecordRepository codeRecordRepository;

    @Resource
    private EmailCodeRecordRepository emailCodeRecordRepository;

    @Value("${sendRealVerifyCode}")
    private boolean sendRealVerifyCode;

    /**
     * 发送短信
     * @param phoneNum 手机号码
     * @param langCode 语言编号(1:英文, 2:中文, 3:韩文)
     */
    public Object sendCode(String phoneNum, int langCode) {
        if (StringUtil.isBlank(phoneNum)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 间隔时间内已经发送过短信
        CodeRecord record = codeRecordRepository.findByPhoneAndSendTimeGreaterThanEqual(
                phoneNum, DateUtil.plusSeconds(new Date(), -30));
        if (record != null) {
            return ResultUtil.errorObj(MTCError.SEND_CODE_WAIT);
        }
        String code;
        // 生成验证码并发送短信
        if (sendRealVerifyCode) {
            code = StringUtil.randomNumber(6);
            Object resultObj = MxtSmsUtil.simpleSend(phoneNum, code, langCode);
            // 发送失败
            if (resultObj != null) {
                return resultObj;
            }
        } else {
            code = "666666";
        }
        // 检查后增加或更新记录
        CodeRecord smsRecord = codeRecordRepository.findByPhone(phoneNum);
        if (smsRecord == null) {
            smsRecord = new CodeRecord();
            smsRecord.setPhone(phoneNum);
        }
        smsRecord.setCode(code);
        smsRecord.setSendTime(new Date());
        codeRecordRepository.save(smsRecord);
        return ResultUtil.successObj();
    }

    /**
     * 检查验证码
     * @param phoneNum 手机号
     * @param code 验证码
     * @return 是否正确
     */
    public boolean check(String phoneNum, String code) {
        CodeRecord record = codeRecordRepository.findByPhoneAndCodeAndSendTimeGreaterThanEqual(
                phoneNum, code, DateUtil.plusSeconds(new Date(), -300));
        return record != null;
    }

    /**
     * 检查验证码，并删除
     * @param phoneNum 手机号
     * @param code 验证码
     * @return 是否正确
     */
    public boolean checkAndDel(String phoneNum, String code) {
        CodeRecord record = codeRecordRepository.findByPhoneAndCodeAndSendTimeGreaterThanEqual(
                phoneNum, code, DateUtil.plusSeconds(new Date(), -300));
        if (record != null) {
            codeRecordRepository.delete(record);
        }
        return record != null;
    }

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @param langCode 语言编号(1:英文, 2:中文, 3:韩文)
     * @return 结果
     */
    public Object emailSendCode(String email, int langCode) {
        if (StringUtil.isBlank(email) || !StringUtil.checkEmail(email)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        String code;
        // 生成验证码
        if (sendRealVerifyCode) {
            code = StringUtil.randomNumber(6);
        } else {
            code = "666666";
        }

        if (langCode == 2) {
            AliyunMailUtil.sendMail(email, "BHB-WALLET 验证码", "您的验证码是" + code + "，1分钟内有效。");
        } else {
            AliyunMailUtil.sendMail(email, "BHB-WALLET Verify Code", "Your verify code " + code + ", the code is valid within 1 minutes.");
        }

        // 检查后增加或更新记录
        EmailCodeRecord record = emailCodeRecordRepository.findByAddress(email);
        if (record == null) {
            record = new EmailCodeRecord();
            record.setAddress(email);
        }
        record.setCode(code);
        record.setSendTime(new Date());
        emailCodeRecordRepository.save(record);
        return ResultUtil.successObj();
    }

    /**
     * 检查邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否正确
     */
    public boolean emailCheck(String email, String code) {
        EmailCodeRecord record = emailCodeRecordRepository.findByAddressAndCodeAndSendTimeGreaterThanEqual(
                email, code, DateUtil.plusSeconds(new Date(), -300));
        return record != null;
    }


    /**
     * 检查邮箱验证码，并删除
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否正确
     */
    public boolean emailCheckAndDel(String email, String code) {
        EmailCodeRecord record = emailCodeRecordRepository.findByAddressAndCodeAndSendTimeGreaterThanEqual(
                email, code, DateUtil.plusSeconds(new Date(), -300));
        if (record != null) {
            emailCodeRecordRepository.delete(record);
        }
        return record != null;
    }

}
