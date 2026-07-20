package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingPortMapper;
import com.hcp.simulator.service.ChargingPortService;
import com.hcp.system.api.domain.ChargingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 充电端口Service实现类
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ChargingPortServiceImpl extends ServiceImpl<ChargingPortMapper, ChargingPort> implements ChargingPortService {

    @Override
    public List<ChargingPort> getByDeviceId(String pileId) {
        return list(new LambdaQueryWrapper<ChargingPort>()
                .eq(ChargingPort::getPileId, pileId));
    }

    @Override
    public void updateGunStatus(String pileId, String deviceId, Long gunStatus, String state) {
        ChargingPort port = getOne(new LambdaQueryWrapper<ChargingPort>()
                .eq(ChargingPort::getPileId, pileId)
                .eq(ChargingPort::getDeviceId, deviceId));
        if (port != null) {
            port.setGunStatus(gunStatus);
            port.setState(state);
            updateById(port);
        }
    }
}
