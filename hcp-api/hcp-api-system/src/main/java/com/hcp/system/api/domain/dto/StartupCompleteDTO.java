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
    private String vinCode;
    private BigDecimal soc;
    private Integer bmsStatus;
    private Integer chargerStatus;
}