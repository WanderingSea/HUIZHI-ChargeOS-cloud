package com.hcp.system.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每小时电量dto")
public class HourlyEnergyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;

    private Integer slotIndex;

    private BigDecimal energy;
}