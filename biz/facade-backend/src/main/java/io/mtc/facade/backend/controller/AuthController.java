package io.mtc.facade.backend.controller;

import io.mtc.common.util.ResultUtil;
import io.mtc.facade.backend.model.MenuBean;
import io.mtc.facade.backend.model.SessionUser;
import io.mtc.facade.backend.config.MenuConfig;
import io.mtc.facade.backend.config.security.SecurityPwdEncode;
import io.mtc.facade.backend.entity.AdminUser;
import io.mtc.facade.backend.repository.AdminUserRepository;
import io.mtc.facade.backend.util.SessionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 登录用户管理类
 *
 * @author Chinhin
 * 2018/6/11
 */
@RestController
public class AuthController {

    @Resource
    private AdminUserRepository userRepository;

    @Resource
    private SecurityPwdEncode securityPwdEncode;

    @GetMapping("/isLogin")
    public String isLogin(HttpServletRequest request) {
        return ResultUtil.success(SessionUtil.isLogin(request));
    }

    @GetMapping("/sessionId")
    public String sessionId(HttpServletRequest request){
        return ResultUtil.success(SessionUtil.sessionId(request));
    }

    @GetMapping("/menu")
    public String menu(HttpServletRequest request) {
        List<MenuBean> result = new ArrayList<>();
        // 当前管理员拥有的权限
        Map<String, Boolean> hasPermission = SessionUtil.auths(request);
        List<MenuBean> allMenu = MenuConfig.menu();
        for (MenuBean it : allMenu) {
            // 如果有二级菜单
            if (it.isHasSub()) {
                List<MenuBean> tempSub = new ArrayList<>();
                for (MenuBean it2 : it.getSubMenus()) {
                    if (hasPermission.get(it2.getPermission()) != null) {
                        tempSub.add(it2);
                    }
                }
                if (tempSub.size() > 0) {
                    MenuBean tempMenuBean = MenuBean.createLv1HasLv2Menu(it.getName(), it.getIcon(), tempSub);
                    result.add(tempMenuBean);
                }
                // 如果只是一级菜单
            } else {
                // 拥有  "菜单需要的权限"
                if (hasPermission.get(it.getPermission()) != null) {
                    result.add(it);
                }
            }
        }
        return ResultUtil.success(result);
    }

    @Transactional
    @PostMapping("/updatePwd")
    public String updatePwd(HttpServletRequest request, String oldPwd, String newPwd) {
        AdminUser adminUser = userRepository.findById(SessionUtil.adminId(request)).get();
        if (securityPwdEncode.matches(oldPwd, adminUser.getPassword())) {
            adminUser.setPassword(securityPwdEncode.encode(newPwd));
            userRepository.save(adminUser);
            return ResultUtil.success("修改密码成功");
        } else {
            return ResultUtil.error("旧密码错误");
        }
    }

    /**
     * 获得当前用户的登录信息
     * @param request 供提供用户id
     * @return 结果
     */
    @GetMapping("/adminInfo")
    public String adminInfo(HttpServletRequest request) {
        AdminUser adminUser = userRepository.findById(SessionUtil.adminId(request)).get();
        SessionUser sessionUser = new SessionUser();
        BeanUtils.copyProperties(adminUser, sessionUser);
        return ResultUtil.success(sessionUser);
    }

    /**
     * 修改自己的信息
     * @param request 提供登录用户id
     * @param email 邮件
     * @param phone 电话
     * @return 结果
     */
    @PostMapping("/updateInfo")
    public String updateInfo(HttpServletRequest request, String email, String phone) {
        AdminUser adminUser = userRepository.findById(SessionUtil.adminId(request)).get();
        adminUser.setEmail(email);
        adminUser.setPhone(phone);
        userRepository.save(adminUser);
        return ResultUtil.success(adminUser);
    }

}
