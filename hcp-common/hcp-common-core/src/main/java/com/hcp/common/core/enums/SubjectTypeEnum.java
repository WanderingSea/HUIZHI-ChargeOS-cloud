package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * SubjectTypeEnum
 * </p>
 * 工商主体类型枚举类
 * @author JHan
 * @Date 2026-08-10 15:26
 */
@Getter
@AllArgsConstructor
public enum SubjectTypeEnum {

    CORPORATE(1, "企业法人"),
    INDIVIDUAL(2, "个体工商户"),
    NATURAL(3, "个人自然人商户"),
    ;

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
