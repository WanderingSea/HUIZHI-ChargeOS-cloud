package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingOrderMapper;
import com.hcp.simulator.service.ChargingOrderService;
import com.hcp.system.api.domain.ChargingOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 充电订单Service实现类
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChargingOrderServiceImpl extends ServiceImpl<ChargingOrderMapper, ChargingOrder> implements ChargingOrderService {

    @Override
    public void updateNoEndOrder(String pileId) {
        // 根据业务需求实现
    }
}
