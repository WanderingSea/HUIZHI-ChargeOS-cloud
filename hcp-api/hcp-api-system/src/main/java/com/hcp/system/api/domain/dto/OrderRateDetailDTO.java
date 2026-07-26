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
@Schema(description = "订单电价详情dto")
public class OrderRateDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "费率索引")
    private Integer rateIndex;
    @Schema(description = "该时段电价（电费+服务费）")
    private BigDecimal ratePrice;
    @Schema(description = "该时段用电量（kWh）")
    private BigDecimal energy;
    @Schema(description = "该时段损耗电量")
    private BigDecimal lossEnergy;
    @Schema(description = "该时段费用")
    private BigDecimal amount;

}
