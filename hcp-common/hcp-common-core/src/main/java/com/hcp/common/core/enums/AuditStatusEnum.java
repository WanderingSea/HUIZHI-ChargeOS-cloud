package com.hcp.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * AuditStatusEnum
 * </p>
 * 审核状态枚举
 * @author JHan
 * @Date 2026-08-08 17:41
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "审核通过"),
    REJECTED(2, "审核驳回");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
