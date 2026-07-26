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
    /**
     * 故障类型（关联c_fault_code_dict表）
     */
    private Integer faultCode;
    /**
     * 重置时间
     */
    private String resetTime;
}
