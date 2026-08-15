package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * <p>
 * UserTypeEnum
 * 用户类型/身份枚举
 * </p>
 *
 * @author JHan
 * @Date 2026-08-08 15:31
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    /** 1=系统超级管理员 */
    SYS_SUPER(1, "系统超级管理员"),

    /** 2=平台管理员 */
    PLAT_ADMIN(2, "平台级管理员"),

    /** 3=代理商运营 */
    AGENT_OP(3, "代理商运营"),

    /** 4=商户（运营商）运营 */
    MCHT_OP(4, "运营商运营"),

    /** 5=商户（运营商）普通员工 */
    MCHT_STAFF(5, "运营商员工/子账户"),
    ;

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;

    /**
     * 根据 value 判断是否系统超级管理员
     */
    public static boolean isSysSuper(Integer value)
    {
        return Objects.equals(SYS_SUPER.getValue(), value);
    }

    /**
     * 根据 value 判断是否平台管理员及以上（含超级管理员）
     */
    public static boolean isPlatAdminOrHigher(Integer value)
    {
        return Objects.equals(SYS_SUPER.getValue(), value)
                || Objects.equals(PLAT_ADMIN.getValue(), value);
    }

    /**
     * 根据 value 判断是否为商户/运营商
     */
    public static boolean isMerchant(Integer value)
    {
        return Objects.equals(MCHT_OP.getValue(), value);
    }

    /**
     * 根据value判断是否为运营商子账号
     */
    public static boolean isMerchantStaff(Integer value){
        return Objects.equals(MCHT_STAFF.getValue(), value);
    }

    /**
     * 根据 value 判断是否为代理商运营
     */
    public static boolean isAgentOp(Integer value)
    {
        return Objects.equals(AGENT_OP.getValue(), value);
    }


}
