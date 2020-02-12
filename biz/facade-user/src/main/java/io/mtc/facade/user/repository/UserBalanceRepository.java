package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserBalance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
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

    @Query(value = "select new UserBalance(ub.id,ub.version,ub.currencyType,ub.currencyAddress,ub.balance,ub.freezingAmount,u.id)from UserBalance as ub inner join User as u on u.id = ub.user.id where ub.currencyAddress=:currencyAddress and ub.currencyType=:currencyType")
    List<UserBalance> findByCurrencyAddressAndCurrencyType(@Param("currencyAddress") String currencyAddress, @Param("currencyType") Integer currencyType);

    @Query(value = "select new UserBalance(ub.id,ub.version,ub.currencyType,ub.currencyAddress,ub.balance,ub.freezingAmount,u.id)from UserBalance as ub inner join User as u on u.id = ub.user.id where ub.currencyAddress=:currencyAddress and ub.currencyType=:currencyType and ub.balance>=:balance")
    List<UserBalance> findByCurrencyAddressAndCurrencyTypeAndBalanceGreaterThanEqual(@Param("currencyAddress") String currencyAddress, @Param("currencyType") Integer currencyType, @Param("balance") BigInteger balance);
}
