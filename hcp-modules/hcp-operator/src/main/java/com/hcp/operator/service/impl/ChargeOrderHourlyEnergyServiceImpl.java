package com.hcp.operator.service.impl;

import com.hcp.common.core.context.SecurityContextHolder;
import com.hcp.operator.domain.ChargeOrderHourlyEnergy;
import com.hcp.operator.mapper.ChargeOrderHourlyEnergyMapper;
import com.hcp.operator.service.IChargeOrderHourlyEnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * ChargeOrderHourlyEnergyServiceImpl
 * 订单分时电量服务实现类
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 17:03
 */
@Service
@RequiredArgsConstructor
public class ChargeOrderHourlyEnergyServiceImpl implements IChargeOrderHourlyEnergyService {

    private final ChargeOrderHourlyEnergyMapper chargeOrderHourlyEnergyMapper;

    /*
    * 保存一条分时电量
    * */
    @Override
    public int saveHourlyEnergy(ChargeOrderHourlyEnergy energy) {
        return chargeOrderHourlyEnergyMapper.insert(energy);
    }

    /*
    * 批量保存分时电量（48 slot）
    * */
    @Override
    public void saveHourlyEnergyBatch(List<ChargeOrderHourlyEnergy> energyList) {
        chargeOrderHourlyEnergyMapper.insertBatch(energyList);
    }

    /*
    * 查询订单的分时电量
    * */
    @Override
    public List<ChargeOrderHourlyEnergy> getByOrderId(String orderId) {
        return chargeOrderHourlyEnergyMapper.selectByOrderId(orderId);
    }
}
