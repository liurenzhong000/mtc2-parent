package io.mtc.facade.backend.feignController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.entity.AdminUser;
import io.mtc.facade.backend.feign.FacadeUser;
import io.mtc.facade.backend.repository.AdminUserRepository;
import io.mtc.facade.backend.util.SessionUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 托管用户及其账单
 *
 * @author Chinhin
 * 2018/8/6
 */
@RestController
@Transactional(readOnly = true)
@RequestMapping("/hostUser")
public class HostUserController {

    @Resource
    private FacadeUser facadeUser;

    @Resource
    private AdminUserRepository userRepository;

    @PreAuthorize("hasAuthority('user:select')")
    @GetMapping("/selectUser")
    public String selectUser(String email, String phone, @ModelAttribute PagingModel pageModel) {
        return facadeUser.selectUser(email, phone, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('user:select')")
    @GetMapping("/userBalance")
    public String userBalance(Long id) {
        return facadeUser.userBalance(id);
    }

    @PreAuthorize("hasAuthority('user:updatePromoter')")
    @PostMapping("/updatePromoter")
    public String updatePromoter(Long uid, String promoter) {
        return facadeUser.updatePromoter(uid, promoter);
    }

    @PreAuthorize("hasAuthority('bill:select')")
    @GetMapping("/bill")
    public String bill(Long uid, String currencyAddress, Integer type, Integer status, @ModelAttribute PagingModel pageModel) {
        // 设置默认排序
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setOrder("DESC");
            pageModel.setSort("id");
        }
        return facadeUser.bills(uid, currencyAddress, type, status, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('bill:update')")
    @PutMapping("/billDetail")
    public String billDetail(Long id, Integer status) {
        return facadeUser.billDetail(id, status);
    }

    /**
     * 查询云矿交易信息
     */
    @PreAuthorize("hasAuthority('bill:select')")
    @GetMapping("/yunkuang")
    public String yukuang(String startTime, String endTime, String payCurrency, Integer tradeType,
                          Integer selectUserType, String userInfo, Long tradeId, @ModelAttribute PagingModel pageModel) {
        return facadeUser.selectYunkuangTrade(startTime, endTime, payCurrency, tradeType,
                selectUserType, userInfo, tradeId, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('user:deposit')")
    @PostMapping("/deposit")
    public String deposit(HttpServletRequest request, Long uid, String number, String currencyAddress, Integer type, String note) {
        AdminUser adminUser = userRepository.findById(SessionUtil.adminId(request)).get();
        return facadeUser.deposit(uid, number, currencyAddress, type, adminUser.getUsername(), note);
    }

}
