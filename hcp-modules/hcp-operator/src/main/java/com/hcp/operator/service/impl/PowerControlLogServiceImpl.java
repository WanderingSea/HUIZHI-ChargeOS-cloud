package com.hcp.operator.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcp.operator.domain.PowerControlLog;
import com.hcp.operator.mapper.PowerControlLogMapper;
import com.hcp.operator.service.IPowerControlLogService;
import com.hcp.system.api.domain.dto.PowerControlDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功率控制日志Service业务层处理
 *
 * @author huayue
 * @date 2026-07-16
 */
@Service
public class PowerControlLogServiceImpl implements IPowerControlLogService
{
    @Autowired
    private PowerControlLogMapper powerControlLogMapper;

    /**
     * 新增功率控制日志
     *
     * @param powerControlLog 功率控制日志
     * @return 结果
     */
    @Override
    public int save(PowerControlLog powerControlLog)
    {
        return powerControlLogMapper.insert(powerControlLog);
    }

    /**
     * 根据充电桩ID查询功率控制日志列表
     *
     * @param pileId 充电桩ID
     * @return 功率控制日志列表
     */
    @Override
    public List<PowerControlLog> getListByPileId(String pileId)
    {
        LambdaQueryWrapper<PowerControlLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PowerControlLog::getPileId, pileId);
        return powerControlLogMapper.selectList(wrapper);
    }

    /**
     * 功率控制日志上报（V2.0）
     *
     * @param dto 功率控制DTO
     */
    @Override
    public void savePowerControl(PowerControlDTO dto)
    {
        PowerControlLog log = new PowerControlLog();
        log.setPileId(dto.getPileId());
        log.setPortId(dto.getPortId());
        log.setMaxPower(dto.getMaxPower());
        log.setPriority(dto.getPriority());
        log.setLimitMinutes(dto.getLimitMinutes());
        log.setResult(0);
        powerControlLogMapper.insert(log);
    }
}
