package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.UserWallet;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * DAO
 *
 * @author Chinhin
 * 2018/12/13
 */
public interface UserWalletRepository extends PagingAndSortingRepository<UserWallet, Long> {

    UserWallet findByWalletAddress(String walletAddress);

    List<UserWallet> findAllByCurrencyType(Integer currencyType);

}
