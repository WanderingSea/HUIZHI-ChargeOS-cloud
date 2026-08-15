package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * SettleCycleEnum
 * </p>
 * 结算周期枚举
 * @author JHan
 * @Date 2026-08-08 17:40
 */
@Getter
@AllArgsConstructor
public enum SettleCycleEnum {

    /** 日结 */
    DAY(1, "日结"),
    /** 周结 */
    MONTH(7, "周结"),
    /** 月结 */
    YEAR(30, "月结");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
