package io.mtc.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 12:03
 * @Description: omni托管地址交易
 */
@Setter
@Getter
public class OmniHostWalletAddressTrans {

    String walletAddress;

    String propertyId;

    //  基链类型 1:eth, 2:bch, 3:eos，4:btc，5:usdt'
    Integer currencyType;

    // 收入数量
    BigDecimal amount;

    String fromAddress;

    String txHash;
}
