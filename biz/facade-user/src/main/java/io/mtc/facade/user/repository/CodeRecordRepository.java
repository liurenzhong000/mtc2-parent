package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.CodeRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;

/**
 * 短信验证码DAO
 *
 * @author Chinhin
 * 2018/7/23
 */
public interface CodeRecordRepository  extends PagingAndSortingRepository<CodeRecord, Long> {

    CodeRecord findByPhoneAndSendTimeGreaterThanEqual(String phone, Date date);

    CodeRecord findByPhoneAndCodeAndSendTimeGreaterThanEqual(String phone, String code, Date date);

    CodeRecord findByPhone(String phone);

}
