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
    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "时段索引（0~47，对应00:00~23:30）")
    private Integer slotIndex;
    @Schema(description = "该时段用电量（kWh）")
    private BigDecimal energy;
}
