package com.hcp.system.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultResetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pileId;
    private Long portId;
    private Integer faultCode;
    private String resetTime;
}