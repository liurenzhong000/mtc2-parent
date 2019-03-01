package io.mtc.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 以太坊托管地址交易
 *
 * @author Chinhin
 * 2019-01-24
 */
@Getter
@Setter
public class EthHostWalletAddressTrans implements Serializable {

    BigInteger nonce;

    String walletAddress;

    String tokenAddress;

    // 收入数量
    BigInteger income;

    String fromAddress;

    String txHash;

}
