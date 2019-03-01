package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * userBalance's Dao
 *
 * @author Chinhin
 * 2018/7/27
 */
public interface UserBalanceRepository extends PagingAndSortingRepository<UserBalance, Long> {

    UserBalance findByUserAndCurrencyAddressAndCurrencyType(User user, String currencyAddress, Integer currencyType);

    List<UserBalance> findAllByUser(User user);

}
