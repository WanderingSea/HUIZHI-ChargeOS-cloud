package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * MerchantTypeEnum
 * </p>
 * 运营商业务类型枚举
 * @author JHan
 * @Date 2026-08-08 17:24
 */
@Getter
@AllArgsConstructor
public enum MerchantTypeEnum {

    PLATFORM_SELF(1, "平台自营"),
    ALLIANCE_OPERATOR(2, "联盟运营商"),
    NORMAL_MERCHANT(3, "普通运营商");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
