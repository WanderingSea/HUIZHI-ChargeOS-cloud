package com.hcp.system.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VinAuthDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 充电桩ID
     */
    private String pileId;
    /**
     * 充电端口ID
     */
    private Long portId;
    /**
     * VIN码（车辆识别码）
     */
    private String vinCode;
    /**
     * 鉴权结果（0-成功，1-失败）
     */
    private Integer authResult;
    /**
     * 关联的充电订单ID
     */
    private String orderId;
}
