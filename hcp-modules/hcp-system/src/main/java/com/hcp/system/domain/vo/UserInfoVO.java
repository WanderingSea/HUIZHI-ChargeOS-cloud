package com.hcp.system.domain.vo;

import lombok.Data;

/**
 * <p>
 * UserInfoVO
 * </p>
 * 后台管理用户信息VO 前台展示所需
 * @author JHan
 * @Date 2026-08-14 14:57
 */
@Data
public class UserInfoVO {

    // 用户id
    private String accountId;  // sys_user:user_id

    // 运营商对外账号，平台管理员为null
    private String accountNo;  // sys_tenant:merchant_no

    // 用户账户属性：工商主体类型：1-企业法人/2-个体工商户/3-个人自然人商户/平台级管理员为null
    private Integer accountAttribute; // sys_tenant:subject_type

    // 运营商业务类型 1-平台自营/2-联盟运营商/3-普通运营商/平台级管理员为null
    private Integer businessType; // sys_tenant:merchant_type

    // 用户身份类型：1=SYS_SUPER 2=PLAT_ADMIN 3=AGENT_OP 4=MCHT_OP 5=MCHT_STAFF
    private Integer userAttribute;  // sys_user:user_type

    // 用户昵称
    private String nickName;  // sys_user:nick_name

    // 账户创建者id（字账户是运营商的id，运营商是后台管理员id，后台管理员为null）
    private String operatorId;  // sys_user:create_user_id

    // 账户创建者主体名称（字账户是运营商的名称，运营商是sys，后台管理员为null）
    private String operatorName;  // sys_tenant:merchant_name

    // 是否是运营商主账号(0-否/1-是/平台级管理员为null)
    private Integer isMainAccount; // sys_user:is_primary

    // 是否是运营商子账号(0-否/1-是/平台级管理员为null)
    private Integer isBDAccount;  // sys_user:is_sub_account

    // 安全校验码
    private String securityCode;  // sys_user:phone

    // 用户头象
    private String avatar;  // sys_user:avatar

}
