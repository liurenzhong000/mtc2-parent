package io.mtc.facade.bitcoin.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 备注信息
 *
 * @author Chinhin
 * 2019/1/1
 */
@Setter
@Getter
public class BtcTxRemark implements Serializable {

    @Id
    private String txHash;

    private String remark;

}
