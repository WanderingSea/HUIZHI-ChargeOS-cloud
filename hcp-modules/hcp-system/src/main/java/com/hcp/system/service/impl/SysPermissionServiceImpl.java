package com.hcp.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.system.api.domain.SysRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.hcp.system.api.domain.SysUser;
import com.hcp.system.service.ISysMenuService;
import com.hcp.system.service.ISysPermissionService;
import com.hcp.system.service.ISysRoleService;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements ISysPermissionService
{

    private final ISysRoleService roleService;
    private final ISysMenuService menuService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户对象
     * @return 角色权限信息
     */
    @Override
    public Set<String>  getRolePermission(SysUser user) {

        Set<String> roles = new HashSet<String>();

        UserTypeEnum userType = user.getUserType();

        // 管理员拥有所有权限
        if (userType == UserTypeEnum.SYS_SUPER || userType == UserTypeEnum.PLAT_ADMIN) {
                roles.add("is_admin");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }

        return roles;
    }

    /**
     * 获取菜单数据权限 <br>
     * 根据不同的身份，获取不同的菜单权限信息
     * 1-系统级管理员：全部权限
     * 2-平台级管理员：运营部分的所有权限(去除开发的权限)
     * 4-普通运营商权限
     * 5-运营商子账户权限
     * @param user 用户对象
     * @return 菜单权限信息
     */
    @Override
    public Set<String> getMenuPermission(SysUser user) {
        Set<String> perms = new HashSet<String>();

        // 获取用户身份
        Integer role = user.getUserType().getValue();

        // 系统级管理员：全部权限
        if (Objects.equals(role, UserTypeEnum.SYS_SUPER.getValue())) {
              perms.add("*:*:*");
        } else {
            // 根据用户的身份，获取不同的菜单权限信息
            List<SysRole> roles = user.getRoles();

            if (!CollectionUtils.isEmpty(roles)) {
                // 多角色设置 permissions 属性，以便数据权限匹配权限
                for (SysRole r : roles) {
                    Set<String> rolePerms = menuService.selectMenuPermsByRoleId(r.getRoleId());
                    r.setPermissions(rolePerms);
                    perms.addAll(rolePerms);
                }
            }
            else {
                perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
            }
        }
        return perms;
    }
}
