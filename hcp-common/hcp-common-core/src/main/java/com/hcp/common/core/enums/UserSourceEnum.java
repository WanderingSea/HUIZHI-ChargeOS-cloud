package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * UserSourceEnum
 * </p>
 * 用户来源枚举
 * @author JHan
 * @Date 2026-08-08 17:02
 */
@Getter
@AllArgsConstructor
public enum UserSourceEnum {

    PLATFORM(1, "平台后台创建"),
    OPERATOR(2, "运营商管理员注册"),
    CUSTOM(3, "C端自助注册"),
    OPEN(4, "开放平台接入");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
