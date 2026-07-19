package com.hcp.operator.service;

import com.hcp.operator.domain.ChargeOrderHourlyEnergy;

import java.util.List;

/**
 * <p>
 * IChargeOrderHourlyEnergyService
 * 订单分时电量服务接口
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 17:01
 */
public interface IChargeOrderHourlyEnergyService {

    /**
     * 保存一条分时电量
     */
    int saveHourlyEnergy(ChargeOrderHourlyEnergy energy);

    /**
     * 批量保存分时电量（48 slot）
     */
    void saveHourlyEnergyBatch(List<ChargeOrderHourlyEnergy> energyList);

    /**
     * 查询订单的分时电量
     */
    List<ChargeOrderHourlyEnergy> getByOrderId(String orderId);
}
