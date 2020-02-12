package io.mtc.facade.bitcoin.util;

/**
 * @Auther: hyp
 * @Date: 2019/3/17 11:41
 * @Description:
 */
public class OmniBalanceBean {
    /**
     * balance : 19.00000000
     * reserved : 0.00000000
     * frozen : 0.00000000
     */

    private String balance;
    private String reserved;
    private String frozen;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getFrozen() {
        return frozen;
    }

    public void setFrozen(String frozen) {
        this.frozen = frozen;
    }
}
