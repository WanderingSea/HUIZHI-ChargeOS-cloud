package com.hcp.operator.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 充电启动日志对象 c_charge_startup_log
 *
 * @author huayue
 * @date 2026-07-16
 */
@Data
@TableName("c_charge_startup_log")
@Schema(description = "充电启动日志")
public class ChargeStartupLog extends TenantEntity
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

    /** 订单ID */
    @Schema(description = "订单ID")
    private String orderId;

    /** 启动结果 */
    @Schema(description = "启动结果")
    private Integer startupResult;

    /** 失败码 */
    @Schema(description = "失败码")
    private Integer failCode;

    /** 电表示值 */
    @Schema(description = "电表示值")
    private BigDecimal meterValue;

    /** VIN码 */
    @Schema(description = "VIN码")
    private String vinCode;

    /** SOC */
    @Schema(description = "SOC")
    private BigDecimal soc;

    /** BMS状态 */
    @Schema(description = "BMS状态")
    private Integer bmsStatus;

    /** 充电机状态 */
    @Schema(description = "充电机状态")
    private Integer chargerStatus;

}
