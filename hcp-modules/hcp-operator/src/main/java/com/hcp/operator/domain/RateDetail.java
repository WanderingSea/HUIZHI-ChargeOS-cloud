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
 * RateDetail
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:02
 */
@Data
@TableName("c_rate_detail")
@Schema(description = "费率详情")
public class RateDetail extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /** 动态费率段id */
    @TableId
    @Schema(description = "动态费率段id")
    private Long id;

    /** 电价id */
    @Schema(description = "电价id")
    private Long priceId;

    /** 费率索引 */
    @Excel(name = "费率索引")
    @Schema(description = "费率索引")
    private Integer rateIndex;

    /** 电价 */
    @Excel(name = "电费单价")
    @Schema(description = "电费单价")
    private BigDecimal elecRate;

    /** 服务费 */
    @Excel(name = "服务费")
    @Schema(description = "服务费")
    private BigDecimal serviceRate;

    /** 开始时间(格式: HH:mm:ss) */
    @Excel(name = "开始时间")
    @Schema(description = "开始时间(格式: HH:mm:ss)")
    private String startTime;

    /** 结束时间(格式: HH:mm:ss) */
    @Excel(name = "结束时间")
    @Schema(description = "结束时间(格式: HH:mm:ss)")
    private String endTime;


}

