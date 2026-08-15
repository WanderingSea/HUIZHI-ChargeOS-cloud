package com.hcp.system.api.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.web.domain.BaseEntity;
import com.hcp.common.core.web.domain.TenantEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.hcp.common.core.annotation.Excel;
import com.hcp.common.core.annotation.Excel.ColumnType;

import java.util.Set;

/**
 * 角色表 sys_role
 *
 * @author JHan
 */
@Data
@NoArgsConstructor
@TableName("sys_role")
public class SysRole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    @TableId
    private Long roleId;

    /** 角色名称 */
    @Excel(name = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    /** 角色权限标识 */
    @Excel(name = "角色权限标识")
    @NotBlank(message = "角色权限标识不能为空")
    @Size(max = 100, message = "角色权限标识长度不能超过100个字符")
    private String roleKey;

    /**
     * 角色权限标识 数字
     */
    @Excel(name = "角色类型标识(1-开发后台总管理/2-官方平台经营总后台/3-联盟运营商/4-普通运营商、个体商户/5-商户子账号)")
    private Integer roleType;

    /**
     * 角色身份展示 3-is_agent/4-is_tenant/5-is_tenant_d
     */
    private String roleShow;

    /**
     * 角色类型标识 数字层级
     */
    @Excel(name = "角色类型层级(1-5,数字越小层级越高)")
    private Integer roleLevel;

    /** 角色排序 */
    @Excel(name = "角色排序")
    @NotBlank(message = "显示顺序不能为空")
    private String roleSort;

    /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
    private boolean menuCheckStrictly;

    /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
    private boolean deptCheckStrictly;

    /** 是否管理员角色（0 不是 1是） */
    private boolean adminRole;

    /** 角色状态（0正常 1停用） */
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    /** 备注 */
    private String remark;

    /** 运营商ID */
    private Long merchantId;

    @TableField(exist = false)
    /** 用户是否存在此角色标识 默认不存在 */
    private boolean flag = false;

    @TableField(exist = false)
    /** 菜单组 */
    private Long[] menuIds;

    @TableField(exist = false)
    /** 部门组（数据权限） */
    private Long[] deptIds;

    /** 角色菜单权限 */
    private Set<String> permissions;

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }
}
