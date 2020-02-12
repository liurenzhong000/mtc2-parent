package io.mtc.facade.backend.model;

import lombok.Data;

import java.util.List;

/**
 * 菜单类
 *
 * @author Chinhin
 * 2018/6/12
 */
@Data
public class MenuBean {

    private MenuBean(String name) {
        this.name = name;
    }

    // 菜单名
    private String name;

    // 要求的权限
    private String permission;

    // 图标: 二级菜单没有图标
    private String icon;

    // 是否拥有子菜单
    private boolean hasSub = false;

    // 请求地址，相对路径
    private String href;

    // 子菜单
    private List<MenuBean> subMenus;

    /**
     * 创建一个二级的菜单
     * @param name 名字
     * @param permission 权限
     * @param href 链接
     * @return 实例
     */
    public static MenuBean createLv2Menu(String name, String permission, String href) {
        MenuBean temp = new MenuBean(name);
        temp.setPermission(permission);
        temp.setHref(href);
        return temp;
    }

    /**
     * 创建一个没有二级菜单的一级菜单
     */
    public static MenuBean createLv1Menu(String name, String permission, String href, String icon) {
        MenuBean temp = new MenuBean(name);
        temp.setPermission(permission);
        temp.setHref(href);
        temp.setIcon(icon);
        return temp;
    }

    /**
     * 创建一个包含二级菜单的一级菜单
     * @param name 名字
     * @param icon 图标
     * @param subMenu 子菜单
     * @return 实例
     */
    public static MenuBean createLv1HasLv2Menu(String name, String icon, List<MenuBean> subMenu) {
        MenuBean temp = new MenuBean(name);
        temp.setIcon(icon);
        temp.setHasSub(true);
        temp.setSubMenus(subMenu);
        return temp;
    }

}
