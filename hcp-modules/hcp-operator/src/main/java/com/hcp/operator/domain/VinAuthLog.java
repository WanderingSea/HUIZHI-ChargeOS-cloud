package com.hcp.operator.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * VIN认证日志对象 c_vin_auth_log
 *
 * @author huayue
 * @date 2026-07-16
 */
@Data
@TableName("c_vin_auth_log")
@Schema(description = "VIN认证日志")
public class VinAuthLog extends TenantEntity
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

    /** VIN码 */
    @Schema(description = "VIN码")
    private String vinCode;

    /** 认证结果 */
    @Schema(description = "认证结果")
    private Integer authResult;

    /** 订单ID */
    @Schema(description = "订单ID")
    private String orderId;

}
