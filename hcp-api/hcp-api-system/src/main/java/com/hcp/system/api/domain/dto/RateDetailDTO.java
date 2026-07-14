package com.hcp.system.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer rateIndex;
    private BigDecimal elecRate;
    private BigDecimal serviceRate;
    /** 格式: HH:mm:ss */
    private String startTime;
    /** 格式: HH:mm:ss */
    private String endTime;
}