package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserKeystore;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * UserKeystore Dao
 *
 * @author Chinhin
 * 2018/7/25
 */
public interface UserKeystoreRepository  extends PagingAndSortingRepository<UserKeystore, Long> {

    UserKeystore findByWalletAddressAndUser(String walletAddress, User user);

}
