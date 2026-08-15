package com.hcp.system.api.domain;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hcp.common.core.enums.SexEnum;
import com.hcp.common.core.enums.UserSourceEnum;
import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.common.core.validate.Phone;
import com.hcp.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.hcp.common.core.annotation.Excel;
import com.hcp.common.core.annotation.Excel.ColumnType;
import com.hcp.common.core.annotation.Excel.Type;
import com.hcp.common.core.annotation.Excels;
import com.hcp.common.core.xss.Xss;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户对象 sys_user
 *
 * @author JHan
 */
@Data
@NoArgsConstructor
@TableName("sys_user")
public class SysUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 后台管理级用户主键ID */
    @Excel(name = "后台管理级用户主键ID", cellType = ColumnType.NUMERIC, prompt = "用户编号")
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "后台管理级用户主键ID")
    private Long userId;

    /** 用户账号 */
    @Excel(name = "用户账号")
    @Xss(message = "用户账号不能包含脚本字符")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 60, message = "用户账号长度不能超过60个字符")
    @Schema(description = "用户账号")
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 60, message = "用户昵称长度不能超过60个字符")
    @Schema(description = "用户昵称")
    private String nickName;

    /** 手机号码（用于登录） */
    @Excel(name = "手机号码")
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    @Phone(message = "手机号码格式不正确")
    @NotBlank(message = "手机号码不能为空")
    @Schema(description = "手机号码")
    private String phone;

    /** 用户真实姓名 */
    @Excel(name = "用户真实姓名")
    @Xss(message = "用户真实姓名不能包含脚本字符")
    @Size(min = 0, max = 60, message = "用户真实姓名长度不能超过60个字符")
    @Schema(description = "用户真实姓名")
    private String realName;

    /** 用户身份类型 */
    @Excel(name = "用户身份类型")
    @Schema(description = "用户类型/身份（1=SYS_SUPER 2=PLAT_ADMIN 3=AGENT_OP 4=MCHT_OP 5=MCHT_STAFF）")
    private UserTypeEnum userType;

    /**
     * 系统级用户所属运营商id,关联sys_tenant表(平台级及更高级管理员id为0)
     */
    @Excel(name = "系统级用户所属运营商id")
    @Schema(description = "系统级用户所属运营商id,关联sys_tenant表(平台级及更高级管理员id为0) ")
    private Long merchantId;

    /** 后台管理密码(初始为系统随机生成) */
    @Schema(description = "后台管理密码(初始为系统随机生成)")
    private String password;

    /** 管理者的邮箱 */
    @Excel(name = "管理者的邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @Schema(description = "管理者的邮箱")
    private String email;

    /** 用户性别（0男 1女 2未知） */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    @Schema(description = "用户性别（0男 1女 2未知）")
    private SexEnum sex;

    /** 管理者主页头像 */
    @Schema(description = "管理者的主页头像（url地址）")
    private String avatar;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    @Schema(description = "帐号状态（0正常/1停用）")
    private UserStatusEnum status;

    /** 用户来源（1-平台后台创建/2-运营商管理员注册/3-C端自助注册/4-开放平台接入） */
    @Excel(name = "用户来源", readConverterExp = "1-平台后台创建/2-运营商管理员注册/3-C端自助注册/4-开放平台接入")
    @Schema(description = "用户来源:1-平台后台创建/2-运营商管理员注册/3-C端自助注册/4-开放平台接入")
    private UserSourceEnum userSource;

    /**
     * 是否运营商主账号
     */
    @Schema(description = "是否是运营商主账号(运营商子账号和平台管理为0)（0=否/1=是），每个tenant_id仅一条为1")
    private Integer isPrimary;

    /** 是否是运营商子账号(0-否/1-是) */
    @Schema(description = "是否是运营商子账号(0-否/1-是)")
    private Integer isSubAccount;

    /**
     * 最后修改密码时间，用于密码过期策略
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后修改密码时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后修改密码时间，用于密码过期策略")
    private Date passwordUpdateTime;

    /** 删除标志（0代表存在 1代表删除） */
    @Schema(description = "删除标志（0-存在/1-删除）")
    private Integer delFlag;

    /** 部门ID */
    @Schema(description = "运营商子账号归属部门ID，无则为0，平台级管理员为0")
    private Long deptId;

    /** 最后登录IP */
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    @Schema(description = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    @Schema(description = "最后登录时间")
    private Date loginDate;

    @Schema(description = "账号创建者id")
    private Long createById;

    @Schema(description = "创建者真实姓名")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新者真实姓名")
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;


    /** 部门对象 */
    @TableField(exist = false)
    @Excels({
        @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
        @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    @Schema(description = "部门对象")
    private SysDept dept;

    /** 角色对象 */
    @TableField(exist = false)
    private List<SysRole> roles;

    /** 角色组 */
    @TableField(exist = false)
    private Long[] roleIds;

    /** 岗位组 */
    @TableField(exist = false)
    private Long[] postIds;

    /** 角色身份标识ID */
    @TableField(exist = false)
    private Long roleId;

    public SysUser(Long userId)
    {
        this.userId = userId;
    }

    // 开发级管理员(最高权限)
    public boolean isSuperAdmin()
    {
        return isSuperAdmin(Long.valueOf(this.userType.getValue()));
    }

    public static boolean isSuperAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }

    // 平台级管理员(此最高权限)
    public boolean isPlatformAdmin()
    {
        return isPlatformAdmin(Long.valueOf(this.userType.getValue()));
    }

    public static boolean isPlatformAdmin(Long roleId) {
        return roleId != null && 2L == roleId;
    }

    // 普通运营商级管理员(普通管理权限)
    public boolean isMerchantAdmin(){
        return isMerchantAdmin(Long.valueOf(this.userType.getValue()));
    }

    public static boolean isMerchantAdmin(Long roleId) {
        return roleId != null && 4L == roleId;
    }

    // 普通运营商子账号级管理员(运营商级自定义权限)
    public boolean isMerchantSub(){
        return isMerchantAdmin(Long.valueOf(this.userType.getValue()));
    }

    public static boolean isMerchantSub(Long roleId) {
        return roleId != null && 5L == roleId;
    }


}
