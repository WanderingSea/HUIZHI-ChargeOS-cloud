package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingPileMapper;
import com.hcp.simulator.service.ChargingPileService;
import com.hcp.system.api.domain.ChargingPile;
import org.springframework.stereotype.Service;

@Service
public class ChargingPileServiceImpl extends ServiceImpl<ChargingPileMapper, ChargingPile> implements ChargingPileService {

    @Override
    public void updateRunningStatus(String pileId, long status) {
        LambdaUpdateWrapper<ChargingPile> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(ChargingPile::getRunningStatus, status)
                .eq(ChargingPile::getPileId, pileId);
        this.update(wrapper);
    }
}