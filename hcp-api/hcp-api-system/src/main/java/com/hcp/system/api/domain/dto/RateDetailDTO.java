package com.hcp.system.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "电价详情dto")
public class RateDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "定价规则id")
    private Long priceId;
    @Schema(description = "费率段编号")
    private Integer rateIndex;
    @Schema(description = "电费单价")
    private BigDecimal elecRate;
    @Schema(description = "服务费电价")
    private BigDecimal serviceRate;
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private String startTime;
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private String endTime;
}