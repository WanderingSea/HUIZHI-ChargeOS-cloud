package com.hcp.operator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.annotation.Excel;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * ChargeOrderHourlyEnergy
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:03
 */
@Data
@TableName("c_charge_order_hourly_energy")
@Schema(description = "订单每小时电量")
public class ChargeOrderHourlyEnergy extends TenantEntity {

    private static final long serialVersionUID = 1L;


    /** 主键ID */
    @TableId
    @Schema(description = "主键ID")
    private Long id;

    /** 关联订单号 */
    @Excel(name = "订单号")
    @Schema(description = "关联订单号")
    private String orderId;

    /** 时段序号(0~47，每半小时) */
    @Excel(name = "时段序号")
    @Schema(description = "时段序号(0~47，每半小时)")
    private Integer slotIndex;

    /** 该半小时电量 */
    @Excel(name = "电量")
    @Schema(description = "该半小时电量")
    private BigDecimal energy;
}
