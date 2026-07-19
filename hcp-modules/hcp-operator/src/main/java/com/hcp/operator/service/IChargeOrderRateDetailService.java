package com.hcp.operator.service;

import com.hcp.operator.domain.ChargeOrderRateDetail;

import java.util.List;

/**
 * <p>
 * IChargeOrderRateDetailService
 * 订单费率明细服务接口
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 16:58
 */
public interface IChargeOrderRateDetailService {

    /**
     * 保存一条订单费率明细
     */
    int saveOrderRateDetail(ChargeOrderRateDetail detail);

    /**
     * 批量保存订单费率明细
     */
    void saveOrderRateDetailBatch(List<ChargeOrderRateDetail> detailList);

    /**
     * 查询订单的费率明细
     */
    List<ChargeOrderRateDetail> getByOrderId(String orderId);

}
