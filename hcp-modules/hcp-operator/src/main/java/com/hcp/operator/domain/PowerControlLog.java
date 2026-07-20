package com.hcp.operator.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 功率控制日志对象 c_power_control_log
 *
 * @author huayue
 * @date 2026-07-16
 */
@Data
@TableName("c_power_control_log")
@Schema(description = "功率控制日志")
public class PowerControlLog extends TenantEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    /** 充电桩ID */
    @Schema(description = "充电桩ID")
    private String pileId;

    /** 充电口ID */
    @Schema(description = "充电口ID")
    private Long portId;

    /** 最大功率 */
    @Schema(description = "最大功率")
    private Integer maxPower;

    /** 优先级 */
    @Schema(description = "优先级")
    private Integer priority;

    /** 限制分钟数 */
    @Schema(description = "限制分钟数")
    private Integer limitMinutes;

    /** 执行结果 */
    @Schema(description = "执行结果")
    private Integer result;

}
