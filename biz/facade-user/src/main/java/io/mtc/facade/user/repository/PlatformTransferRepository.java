package io.mtc.facade.user.repository;

import io.mtc.facade.user.constants.PlatformTransferStatus;
import io.mtc.facade.user.entity.PlatformTransfer;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 17:05
 * @Description: 平台记录：转入手续费，汇总等
 */
public interface PlatformTransferRepository extends PagingAndSortingRepository<PlatformTransfer, Long> {

    List<PlatformTransfer> findByFromAddressAndStatus(String walletAddress, PlatformTransferStatus status);

}
