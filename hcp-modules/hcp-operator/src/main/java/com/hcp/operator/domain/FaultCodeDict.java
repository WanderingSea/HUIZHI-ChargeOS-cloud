package com.hcp.operator.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 故障码字典对象 c_fault_code_dict
 *
 * @author huayue
 * @date 2026-07-16
 */
@Data
@TableName("c_fault_code_dict")
@Schema(description = "故障码字典")
public class FaultCodeDict implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 字典ID */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典ID")
    private Long id;

    /** 故障类型 */
    @Schema(description = "故障类型")
    private Integer faultType;

    /** 故障码 */
    @Schema(description = "故障码")
    private Integer faultCode;

    /** 故障名称 */
    @Schema(description = "故障名称")
    private String faultName;

    /** 故障描述 */
    @Schema(description = "故障描述")
    private String faultDesc;

}
