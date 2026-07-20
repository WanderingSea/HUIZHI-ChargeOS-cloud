package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingPileMapper;
import com.hcp.simulator.service.ChargingPileService;
import com.hcp.system.api.domain.ChargingPile;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 充电桩Service实现类
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChargingPileServiceImpl extends ServiceImpl<ChargingPileMapper, ChargingPile> implements ChargingPileService {

    @Override
    public void updateRunningStatus(String pileId, long status) {
        ChargingPile pile = getById(pileId);
        if (pile != null) {
            pile.setRunningStatus(status);
            updateById(pile);
        }
    }
}