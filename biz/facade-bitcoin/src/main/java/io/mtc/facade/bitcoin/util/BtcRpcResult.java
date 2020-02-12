package io.mtc.facade.bitcoin.util;

import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: hyp
 * @Date: 2019/3/14 16:16
 * @Description:
 */
@Setter
@Getter
public class BtcRpcResult {

    private boolean success = false;
    private String code = "0";
    private String msg = "OK";
    private String result;

    public BtcRpcResult(){}
    public BtcRpcResult(String result) {
        this.success = true;
        this.result = result;
    }
}
