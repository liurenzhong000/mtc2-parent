package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.Contact;
import io.mtc.facade.user.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Contact Dao
 *
 * @author Chinhin
 * 2018/9/11
 */
public interface ContactRepository extends PagingAndSortingRepository<Contact, Long>{

    List<Contact> findAllByUserOrderByNameAsc(User user);

}
