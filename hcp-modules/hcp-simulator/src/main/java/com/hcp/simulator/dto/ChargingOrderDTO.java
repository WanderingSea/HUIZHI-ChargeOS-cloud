package com.hcp.simulator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充电订单对象 c_charging_order
 *
 * @author hcp
 * @date 2024-08-06
 */
@Data
@Schema(description = "用户实体类")
public class ChargingOrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "充电桩ID")
    private String pileId;

    @Schema(description = "端口-设备ID")
    private String deviceId;

    @Schema(description = "交易标识")
    private Integer tradeType;

    @Schema(description = "VIN码")
    private String vinCode;

    @Schema(description = "电表表号")
    private String meterNumber;

    @Schema(description = "电表密文")
    private String meterCipher;

    @Schema(description = "电表起值")
    private BigDecimal meterStartValue;

    @Schema(description = "电表止值")
    private BigDecimal meterEndValue;

    @Schema(description = "计损总电量")
    private BigDecimal lossTotalPower;

}
