package io.mtc.facade.backend.config;

import io.mtc.facade.backend.model.MenuBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单集合
 *
 * @author Chinhin
 * 2018/6/12
 */
public class MenuConfig {

    private static List<MenuBean> allMenu;

    /**
     * 获取所有的菜单
     * @return 所有的菜单
     */
    public static List<MenuBean> menu() {
        if (allMenu == null) {
            synchronized (MenuConfig.class) {
                allMenu = new ArrayList<>();

                List<MenuBean> adminSubMenus = new ArrayList<>();
                adminSubMenus.add(MenuBean.createLv2Menu("管理员", "admin:select", "page/admin/index.html"));
                adminSubMenus.add(MenuBean.createLv2Menu("角色", "role:select", "page/role/index.html"));
                adminSubMenus.add(MenuBean.createLv2Menu("权限", "permission:select", "page/permission/index.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("管理员管理", "fa-users", adminSubMenus));

                List<MenuBean> tokenSubMenus = new ArrayList<>();
                tokenSubMenus.add(MenuBean.createLv2Menu("平台代币", "currency:select", "page/currency/index.html"));
                tokenSubMenus.add(MenuBean.createLv2Menu("代币分类", "currency:select", "page/currencyCategory/index.html"));
                tokenSubMenus.add(MenuBean.createLv2Menu("发币记录", "currency:select", "page/createCurrency/index.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("币种管理", "fa-btc", tokenSubMenus));

                allMenu.add(MenuBean.createLv1Menu("交易记录", "trans:select", "page/trans/index.html", "fa-money"));

                List<MenuBean> userSubMenus = new ArrayList<>();
                userSubMenus.add(MenuBean.createLv2Menu("用户", "user:select", "page/user/index.html"));
                userSubMenus.add(MenuBean.createLv2Menu("账单", "bill:select", "page/bill/index.html"));
                userSubMenus.add(MenuBean.createLv2Menu("云矿交易", "bill:select", "page/bill/yunkuang.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("托管用户", "fa-user", userSubMenus));

                List<MenuBean> notificationSubMenus = new ArrayList<>();
                notificationSubMenus.add(MenuBean.createLv2Menu("推送模板", "notifyTemplate:select", "page/notifyTemplate/index.html"));
                notificationSubMenus.add(MenuBean.createLv2Menu("推送记录", "notifyRecord:select", "page/notifyRecord/index.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("推送", "fa-envelope", notificationSubMenus));

                List<MenuBean> loanSubMenus = new ArrayList<>();
                loanSubMenus.add(MenuBean.createLv2Menu("借款配置", "loanConfig:update", "page/loanConfig/index.html"));
                loanSubMenus.add(MenuBean.createLv2Menu("借款记录", "loanRecord:select", "page/loanRecord/index.html"));
                loanSubMenus.add(MenuBean.createLv2Menu("借款奖励", "loanBonus:select", "page/loanBonus/index.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("借款", "fa-usd", loanSubMenus));

                List<MenuBean> saleSubMenus = new ArrayList<>();
                saleSubMenus.add(MenuBean.createLv2Menu("转盘配置", "wheel:update", "page/wheel/index.html"));
                saleSubMenus.add(MenuBean.createLv2Menu("转盘中奖记录", "wheel:record", "page/wheel/record.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("营销", "fa-spinner fa-spin", saleSubMenus));

                List<MenuBean> settingSubMenus = new ArrayList<>();
                settingSubMenus.add(MenuBean.createLv2Menu("自定义网页", "custom:select", "page/custom/index.html"));
                settingSubMenus.add(MenuBean.createLv2Menu("APP加载页", "launchScreen:update", "page/launchScreen/form.html"));
                settingSubMenus.add(MenuBean.createLv2Menu("App管理", "appVersion:select", "page/appVersion/index.html"));
                allMenu.add(MenuBean.createLv1HasLv2Menu("设置", "fa-gear", settingSubMenus));
            }
        }
        return allMenu;
    }

}
