package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * User Dao
 *
 * @author Chinhin
 * 2018/7/23
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    User findByPhone(String phone);

    User findByEmail(String email);

    User findByPhoneAndLoginPassword(String phone, String loginPassword);

    User findByEmailAndLoginPassword(String email, String loginPassword);

    List<User> findByIdIn(Long[] ids);

    /**
     * 每天重置用户的抽奖
     */
    @Modifying
    @Query(value = "update User set wheelNum = 1, todayGetWheelNumByShare = 0, todayGetWheelNumByTransfer = 0")
    void resetUserWheelNum();

}
