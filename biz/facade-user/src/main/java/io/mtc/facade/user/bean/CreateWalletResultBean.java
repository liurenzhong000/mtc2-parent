package io.mtc.facade.user.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 创建钱包结果的bean
 *
 * @author Chinhin
 * 2019-01-24
 */
@Getter
@Setter
@AllArgsConstructor
public class CreateWalletResultBean implements Serializable {

    private String encryptPrivateKey;

    private String address;

}
