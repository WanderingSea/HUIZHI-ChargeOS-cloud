package com.hcp.system.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartupCompleteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pileId;
    private Long portId;
    private String orderId;
    private Integer startupResult;
    private Integer failCode;
    private BigDecimal meterValue;
    /**
     * VIN码（车辆识别码）
     */
    private String vinCode;
    /**
     * 电池SOC值（剩余电量百分比）
     */
    private BigDecimal soc;
    /**
     * BMS状态（电池管理系统状态）
     */
    private Integer bmsStatus;
    /**
     * 充电机状态
     */
    private Integer chargerStatus;
}
