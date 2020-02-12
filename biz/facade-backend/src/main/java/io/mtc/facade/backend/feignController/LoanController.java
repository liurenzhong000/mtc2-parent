package io.mtc.facade.backend.feignController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.backend.feign.FacadeUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 借款控制器
 *
 * @author Chinhin
 * 2018/10/11
 */
@RestController
@RequestMapping("/loan")
public class LoanController {

    @Resource
    private FacadeUser facadeUser;

    /**
     * 获取借币配置信息
     */
    @PreAuthorize("hasAuthority('loanConfig:update')")
    @GetMapping("/config")
    public String getLoanConfig() {
        return facadeUser.getLoanConfig();
    }

    /**
     * 更新借币配置信息
     * @param loanConfigJson 借币配置信息json
     */
    @PreAuthorize("hasAuthority('loanConfig:update')")
    @PostMapping("/config")
    public String updateLoanConfig(String loanConfigJson) {
        return facadeUser.updateLoanConfig(loanConfigJson);
    }

    @PreAuthorize("hasAuthority('loanRecord:select')")
    @GetMapping
    public String records(String sn, Integer status, @ModelAttribute PagingModel pageModel) {
        return facadeUser.loanRecords(sn, status, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('loanRecord:update')")
    @PostMapping
    public String updateRecord(Long id, Integer targetStatus, String repayment) {
        return facadeUser.updateLoanRecord(id, targetStatus, repayment);
    }

    @PreAuthorize("hasAuthority('loanBonus:select')")
    @GetMapping("/bonus")
    public String bonusRecords(Integer bonusStatus, @ModelAttribute PagingModel pageModel) {
        return facadeUser.bonusRecords(bonusStatus, CommonUtil.toJson(pageModel));
    }

    /**
     * 发放提成
     * @param id 提成记录id
     * @param bonus 提成值字符串
     */
    @PreAuthorize("hasAuthority('loanBonus:update')")
    @PostMapping("/bonus")
    public String bonus(Long id, String bonus) {
        return facadeUser.bonus(id, bonus);
    }

}
