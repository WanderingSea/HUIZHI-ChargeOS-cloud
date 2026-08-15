package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * SexEnum
 * </p>
 * 性别枚举
 *
 * @author JHan
 * @Date 2026-08-08 16:57
 */
@Getter
@AllArgsConstructor
public enum SexEnum {

    MALE(0, "男"),

    FEMALE(1, "女"),

    UNKNOWN(2, "未知"),
    ;

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
