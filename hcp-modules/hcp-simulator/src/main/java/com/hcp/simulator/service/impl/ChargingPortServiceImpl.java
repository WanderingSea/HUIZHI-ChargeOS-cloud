package com.hcp.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcp.simulator.mapper.ChargingPortMapper;
import com.hcp.simulator.service.ChargingPortService;
import com.hcp.system.api.domain.ChargingPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargingPortServiceImpl extends ServiceImpl<ChargingPortMapper, ChargingPort> implements ChargingPortService {

    @Override
    public List<ChargingPort> getByDeviceId(String pileId) {
        LambdaQueryWrapper<ChargingPort> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingPort::getPileId, pileId);
        return this.list(wrapper);
    }

    @Override
    public void updateGunStatus(String pileId, String deviceId, Long gunStatus, String state) {
        LambdaUpdateWrapper<ChargingPort> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChargingPort::getPileId, pileId)
                .eq(ChargingPort::getDeviceId, deviceId)
                .set(ChargingPort::getGunStatus, gunStatus)
                .set(ChargingPort::getState, state);
        this.update(wrapper);
    }
}