package io.mtc.facade.user.repository;

import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.constants.BillType;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.UserBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

/**
 * bill's dao
 *
 * @author Chinhin
 * 2018/7/27
 */
public interface BillRepository extends PagingAndSortingRepository<Bill, Long>{

    Page<Bill> findAllByBalance(UserBalance userBalance, Pageable pageable);

    Page<Bill> findAllByBalanceAndCreateTimeBetween(UserBalance userBalance, Date start, Date end, Pageable pageable);

    Set<Bill> findAllByStatusAndTypeAndCurrencyTypeAndWithdrawExpireTimeGreaterThan(BillStatus status, BillType type, Integer currencyType, long processExpireTime);

    Bill findTopByStatusAndTypeAndCurrencyTypeOrderByIdAsc(BillStatus status, BillType type, Integer currencyType);

    Bill findByTxHashAndCurrencyTypeAndType(String txHash, Integer currencyType, BillType type);

    /**
     * 更新过期的账单
     */
    @Modifying
    @Query(value = "update Bill set status = :targetStatus where status = :beforeStatus " +
            "and relatedAddress = :relatedAddress and txNonce <= :nonce")
    void setExpireBillStatus(@Param("targetStatus") BillStatus targetStatus, @Param("beforeStatus") BillStatus beforeStatus,
                       @Param("relatedAddress") String relatedAddress, @Param("nonce") BigInteger nonce);

    /**
     * 更新过期的账单
     */
    @Modifying
    @Query(value = "update Bill set status = :targetStatus where status = :beforeStatus and txNonce <= :nonce")
    void setExpireBillStatus(@Param("targetStatus") BillStatus targetStatus, @Param("beforeStatus") BillStatus beforeStatus,
                             @Param("nonce") BigInteger nonce);
}
