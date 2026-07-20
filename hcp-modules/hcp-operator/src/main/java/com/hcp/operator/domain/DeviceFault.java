package com.hcp.operator.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备故障对象 c_device_fault
 *
 * @author huayue
 * @date 2026-07-16
 */
@Data
@TableName("c_device_fault")
@Schema(description = "设备故障")
public class DeviceFault extends TenantEntity
{
    private static final long serialVersionUID = 1L;

    /** 故障ID */
    @TableId(type = IdType.AUTO)
    @Schema(description = "故障ID")
    private Long id;

    /** 充电桩ID */
    @Schema(description = "充电桩ID")
    private String pileId;

    /** 充电口ID */
    @Schema(description = "充电口ID")
    private Long portId;

    /** 故障类型 */
    @Schema(description = "故障类型")
    private Integer faultType;

    /** 故障码 */
    @Schema(description = "故障码")
    private Integer faultCode;

    /** 故障时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "故障时间")
    private Date faultTime;

    /** 复位时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "复位时间")
    private Date resetTime;

    /** 状态 */
    @Schema(description = "状态(0:未修复 1:已修复)")
    private Integer status;

}
