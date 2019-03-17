package io.mtc.facade.bitcoin.util;

import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: hyp
 * @Date: 2019/3/15 19:44
 * @Description:
 */
@Setter
@Getter
public class OmniTransactionBean {


    /**
     * txid : ca748aad79f8392a012ae24443b9548e55c2f147dcf524cac0a36cf125071ee1
     * fee : 0.00000257
     * sendingaddress : 18HUP17GjV5jo5ZF6F9fxHxmGhtfB1q9YQ
     * referenceaddress : 1NWbqeVR6CQ1L57Urtkf3xBY6itp3YiADx
     * ismine : true
     * version : 0
     * type_int : 0
     * type : Simple Send
     * propertyid : 31
     * divisible : true
     * amount : 20.00000000
     * valid : true
     * blockhash : 00000000000000000021e4eefdc235f342fb7d91bffede43bfdb0a608608c260
     * blocktime : 1552620652
     * positioninblock : 2067
     * block : 567120
     * confirmations : 21
     */

    private String txid;
    private String fee;
    private String sendingaddress;
    private String referenceaddress;
    private boolean ismine;
    private int version;
    private int type_int;
    private String type;
    private int propertyid;
    private boolean divisible;
    private String amount;
    private boolean valid;
    private String blockhash;
    private int blocktime;
    private int positioninblock;
    private int block;
    private int confirmations;

}
