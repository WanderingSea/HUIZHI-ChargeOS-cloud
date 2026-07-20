package com.hcp.operator.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.annotation.Excel;
import com.hcp.common.core.web.domain.BaseEntity;
import com.hcp.common.core.web.domain.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.math.BigDecimal;

/**
 * <p>
 * ChargeOrderRateDetail
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:03
 */
@Data
@TableName("c_charge_order_rate_detail")
public class ChargeOrderRateDetail extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId
    @Schema(description = "主键ID")
    private Long id;

    /** 关联订单ID */
    @Excel(name = "订单ID")
    @Schema(description = "关联c_charging_order.order_id")
    private String orderId;

    /** 费率段编号 */
    @Excel(name = "费率段编号")
    @Schema(description = "费率段编号")
    private Integer rateIndex;

    /** 该段费率单价 */
    @Excel(name = "费率单价")
    @Schema(description = "该段费率单价")
    private BigDecimal ratePrice;

    /** 该段充电电量 */
    @Excel(name = "充电电量")
    @Schema(description = "该段充电电量")
    private BigDecimal energy;

    /** 该段计损电量 */
    @Excel(name = "计损电量")
    @Schema(description = "该段计损电量")
    private BigDecimal lossEnergy;

    /** 该段金额 */
    @Excel(name = "金额")
    @Schema(description = "该段金额")
    private BigDecimal amount;


}
