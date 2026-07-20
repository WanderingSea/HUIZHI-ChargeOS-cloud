package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingOrderMapper;
import com.hcp.simulator.service.ChargingOrderService;
import com.hcp.system.api.domain.ChargingOrder;
import org.springframework.stereotype.Service;

@Service
public class ChargingOrderServiceImpl extends ServiceImpl<ChargingOrderMapper, ChargingOrder> implements ChargingOrderService {

    @Override
    public void updateNoEndOrder(String pileId) {
        LambdaUpdateWrapper<ChargingOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChargingOrder::getPileId, pileId)
                .ne(ChargingOrder::getOrderState, "5")
                .set(ChargingOrder::getOrderState, "2");
        this.update(wrapper);
    }
}