package io.mtc.facade.backend.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 托管用户的消费
 *
 * @author Chinhin
 * 2018/6/16
 */
@FeignClient("facade-user")
public interface FacadeUser {

    @GetMapping("/api/user")
    String selectUser(@RequestParam("email") String email,
                      @RequestParam("phone") String phone,
                      @RequestParam("pageModelStr") String pageModelStr);

    @GetMapping("/api/userBalance")
    String userBalance(@RequestParam("uid") Long uid);

    @PostMapping("/api/deposit")
    String deposit(@RequestParam("uid") Long uid,
                   @RequestParam("number") String number,
                   @RequestParam("currencyAddress") String currencyAddress,
                   @RequestParam("type") Integer type,
                   @RequestParam("admin") String admin,
                   @RequestParam("note") String note);

    @GetMapping("/api/bills")
    String bills(@RequestParam("uid") Long uid,
                 @RequestParam("currencyAddress") String currencyAddress,
                 @RequestParam("type") Integer type,
                 @RequestParam("status") Integer status,
                 @RequestParam("pageModelStr") String pageModelStr);

    @PutMapping("/api/bill/{id}")
    String billDetail(@RequestParam("id") @PathVariable("id") Long id,
                 @RequestParam("status") Integer status);

    @GetMapping("/loanApi/config")
    String getLoanConfig();

    @PostMapping("/loanApi/config")
    String updateLoanConfig(@RequestParam("loanConfigJson") String loanConfigJson);

    @GetMapping("/loanApi")
    String loanRecords(@RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "pageModelStr") String pageModelStr);

    @PostMapping("/loanApi")
    String updateLoanRecord(
            @RequestParam("id") Long id,
            @RequestParam("targetStatus") Integer targetStatus,
            @RequestParam(value = "repayment", required = false) String repayment);

    @GetMapping("/loanApi/bonus")
    String bonusRecords(@RequestParam(value = "bonusStatus", required = false) Integer bonusStatus,
            @RequestParam(value = "pageModelStr") String pageModelStr);

    @PostMapping("/loanApi/bonus")
    String bonus(@RequestParam("id") Long id, @RequestParam(value = "bonus") String bonus);

    @PostMapping("/api/updatePromoter")
    String updatePromoter(@RequestParam("uid") Long uid, @RequestParam(value = "promoter") String promoter);

    /**
     * 查询云矿交易信息
     * @param payCurrency 支付币种（简称）
     * @param tradeType 交易类型，与bill的一致
     * @param selectUserType 查询方式：1手机号码，2邮箱，3ID
     * @param userInfo 对应上面的值
     * @param tradeId 交易ID
     */
    @GetMapping("/api/yunkuangTrade")
    String selectYunkuangTrade(@RequestParam(value = "startTime", required = false) String startTime,
                               @RequestParam(value = "endTime", required = false) String endTime,
                               @RequestParam(value = "payCurrency", required = false) String payCurrency,
                               @RequestParam(value = "tradeType", required = false) Integer tradeType,
                               @RequestParam(value = "selectUserType", required = false) Integer selectUserType,
                               @RequestParam(value = "userInfo", required = false) String userInfo,
                               @RequestParam(value = "tradeId", required = false) Long tradeId,
                               @RequestParam(value = "pageModelStr", required = false) String pageModelStr);

    @GetMapping("/wheelApi/prize")
    String wheelPrizes();

    @PostMapping("/wheelApi/prize")
    String wheelUpdatePrize(@RequestParam("json") String json);

    @GetMapping("/wheelApi/record")
    String wheelRecord(@RequestParam(value = "startTime", required = false) String startTime,
                       @RequestParam(value = "endTime", required = false) String endTime,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "prizeName", required = false) String prizeName,
                       @RequestParam(value = "selectUserType", required = false) Integer selectUserType,
                       @RequestParam(value = "userInfo", required = false) String userInfo,
                       @RequestParam(value = "pageModelStr", required = false) String pageModelStr);

    @PostMapping("/wheelApi/record")
    String updateStatus(@RequestParam("recordId") Long recordId, @RequestParam("status") Integer status);

}
