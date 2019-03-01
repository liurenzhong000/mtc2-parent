package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.EmailCodeRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;

/**
 * 邮箱验证码DAO
 *
 * @author Chinhin
 * 2018/7/23
 */
public interface EmailCodeRecordRepository extends PagingAndSortingRepository<EmailCodeRecord, Long> {

    EmailCodeRecord findByAddressAndCodeAndSendTimeGreaterThanEqual(String phone, String code, Date date);

    EmailCodeRecord findByAddress(String phone);

}
