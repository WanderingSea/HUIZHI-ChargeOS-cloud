package com.hcp.operator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcp.operator.domain.ChargeStartupLog;
import com.hcp.operator.mapper.ChargeStartupLogMapper;
import com.hcp.operator.service.IChargeStartupLogService;
import com.hcp.system.api.domain.dto.StartupCompleteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 充电启动日志Service业务层处理
 *
 * @author huayue
 * @date 2026-07-16
 */
@Service
public class ChargeStartupLogServiceImpl implements IChargeStartupLogService
{
    @Autowired
    private ChargeStartupLogMapper chargeStartupLogMapper;

    /**
     * 新增充电启动日志
     *
     * @param chargeStartupLog 充电启动日志
     * @return 结果
     */
    @Override
    public int save(ChargeStartupLog chargeStartupLog)
    {
        return chargeStartupLogMapper.insert(chargeStartupLog);
    }

    /**
     * 根据订单ID查询充电启动日志
     *
     * @param orderId 订单ID
     * @return 充电启动日志
     */
    @Override
    public ChargeStartupLog getByOrderId(String orderId)
    {
        LambdaQueryWrapper<ChargeStartupLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargeStartupLog::getOrderId, orderId);
        return chargeStartupLogMapper.selectOne(wrapper);
    }

    /**
     * 充电机启动完成上报（V2.0）
     *
     * @param dto 启动完成DTO
     */
    @Override
    public void saveStartupComplete(StartupCompleteDTO dto)
    {
        ChargeStartupLog log = new ChargeStartupLog();
        log.setPileId(dto.getPileId());
        log.setPortId(dto.getPortId());
        log.setOrderId(dto.getOrderId());
        log.setStartupResult(dto.getStartupResult());
        log.setFailCode(dto.getFailCode());
        log.setMeterValue(dto.getMeterValue());
        log.setVinCode(dto.getVinCode());
        log.setSoc(dto.getSoc());
        log.setBmsStatus(dto.getBmsStatus());
        log.setChargerStatus(dto.getChargerStatus());
        chargeStartupLogMapper.insert(log);
    }
}
