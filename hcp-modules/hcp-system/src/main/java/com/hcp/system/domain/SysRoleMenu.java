package com.hcp.system.domain;

import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author JHan
 */
@Data
public class SysRoleMenu
{
    /** 管理级用户角色身份ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;

    /** 运营商(租户)ID */
    private Long merchantId;


}
