package com.hcp.common.core.web.domain;

import lombok.Data;


/**
 * Entity基类
 *
 * @author JHan
 */
@Data
public class TenantEntity extends BaseEntity
{

    /** 租户/运营商ID */
    private Long merchantId;

}
