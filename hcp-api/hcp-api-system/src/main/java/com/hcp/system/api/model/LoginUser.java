package com.hcp.system.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.system.api.domain.SysUser;
import lombok.Data;

/**
 *  后台管理级用户信息
 *
 * @author JHan
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 后台管理级用户身份类型
     */
    private Integer userType;

    /**
     * 后台管理级用户手机号
     */
     private String phone;

    /**
     * 后台管理级用户名id
     */
    private Long userId;

    /**
     * 后台管理级用户账号
     */
    private String username;

    /**
     * 后台管理级用户真实姓名
     */
    private String realName;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 后台管理级用户基本信息
     */
    private SysUser sysUser;

    /**
     * 运营商(租户)租赁截止日期--dhr
     */
    private Date tenantEndTime;

    /**
     * 运营商(租户)状态
     */
    private UserStatusEnum status;

}
