package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.UserBalanceSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface UserBalanceSampleRepository extends JpaRepository<UserBalanceSample, Long>, JpaSpecificationExecutor<UserBalanceSample> {

    @Query("select new UserBalanceSample (ubs.id,ubs.balance,ubs.freezingAmount,u.id,ub.id) from UserBalanceSample as ubs inner join UserBalance as ub on ub.id = ubs.userBalance.id inner join User as u on u.id = ub.user.id where ubs.createTime between :startTime and :endTime")
    List<UserBalanceSample> findByDate(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
