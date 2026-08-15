package com.hcp.system.domain;

import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author JHan
 */
@Data
public class SysUserRole
{
    /** 后台管理级用户ID */
    private Long userId;

    /** 后台管理级角色身份ID */
    private Long roleId;

    /** 运营商(租户)ID */
    private Long merchantId;
}
