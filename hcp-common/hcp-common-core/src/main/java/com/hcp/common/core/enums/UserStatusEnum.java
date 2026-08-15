package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态
 *
 * @author JHan
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum
{
    NORMAL(0, "正常"),

    DISABLE(1, "停用"),

    DELETED(1, "删除");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
