package com.hcp.system.domain.vo;

import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.enums.UserTypeEnum;
import lombok.Data;

/**
 * <p>
 * UserListVO
 * </p>
 * 用户精简信息VO（用于下拉列表筛选等场景）
 * @author JHan
 * @Date 2026-08-15 15:18
 */
@Data
public class UserListVO {

    // 用户ID
    private String userId;

    // 用户账号
    private String account;

    // 用户昵称
    private String nickName;

    // 用户真实姓名
    private String userName;

    // 用户头像
    private String avatar;

    // 用户手机号
    private String securityCode;

    // 账号状态（0正常 1停用）
    private Integer status;

    // 用户身份类型
    private Integer role;

    // 是否是运营商主账号
    private Integer isMain;

    // 创建者id
    private String createBy;

    // 用户所属运营商id
    private String accountOwner;

}
