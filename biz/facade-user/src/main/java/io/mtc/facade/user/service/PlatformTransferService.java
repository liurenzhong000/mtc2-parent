package io.mtc.facade.user.service;

import io.mtc.common.dto.EthTransObj;
import io.mtc.facade.user.constants.PlatformTransferStatus;
import io.mtc.facade.user.entity.PlatformTransfer;
import io.mtc.facade.user.repository.PlatformTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: hyp
 * @Date: 2019/3/10 11:06
 * @Description: 平台内相关记录
 */
@Slf4j
@Service
public class PlatformTransferService {

    @Resource
    private PlatformTransferRepository platformTransferRepository;

    public void completeFeeToUser(EthTransObj transInfo) {
        Long id = transInfo.getTxId();
        PlatformTransfer platformTransfer = platformTransferRepository.findById(id).get();
        //已经处理确认过了或者记录不存在
        if (platformTransfer == null || platformTransfer.getStatus().equals(PlatformTransferStatus.AFFIRM)){
            return;
        }
        // 操作成功
        if (transInfo.getStatus() == EthTransObj.Status.SUCCESS.ordinal()) {
            platformTransfer.setStatus(PlatformTransferStatus.AFFIRM);
        } else if (transInfo.getStatus() == EthTransObj.Status.FAIL.ordinal()) {
            platformTransfer.setStatus(PlatformTransferStatus.FAIL);
        }
        platformTransferRepository.save(platformTransfer);
    }

    public void completeUserToMain(EthTransObj transInfo) {
        completeFeeToUser(transInfo);
    }
}
