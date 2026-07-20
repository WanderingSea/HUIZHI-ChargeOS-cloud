package com.hcp.operator.service;


import com.hcp.operator.domain.RateDetail;

import java.util.List;

/**
 * <p>
 * IRateDetailService
 * 动态费率段服务接口
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 16:56
 */
public interface IRateDetailService {

    /**
     * 查询费率段列表-根据priceId
     */
    List<RateDetail> getByPriceId(Long priceId);

    /**
     * 保存费率段（先删后插，幂等）
     * @param priceId 定价规则ID
     * @param details 费率段列表，最多48条
     */
    void saveRateDetails(Long priceId, List<RateDetail> details);


}
