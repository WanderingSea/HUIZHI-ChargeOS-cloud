package com.hcp.system.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyEnergyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer slotIndex;
    private BigDecimal energy;
}