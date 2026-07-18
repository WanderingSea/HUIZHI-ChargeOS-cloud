package com.hcp.operator.service.impl;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcp.operator.domain.DeviceFault;
import com.hcp.operator.mapper.DeviceFaultMapper;
import com.hcp.operator.service.IDeviceFaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设备故障Service业务层处理
 *
 * @author huayue
 * @date 2026-07-16
 */
@Service
public class DeviceFaultServiceImpl implements IDeviceFaultService
{
    @Autowired
    private DeviceFaultMapper deviceFaultMapper;

    /**
     * 新增设备故障
     *
     * @param deviceFault 设备故障
     * @return 结果
     */
    @Override
    public int save(DeviceFault deviceFault)
    {
        return deviceFaultMapper.insert(deviceFault);
    }

    /**
     * 根据ID查询设备故障
     *
     * @param id 故障ID
     * @return 设备故障
     */
    @Override
    public DeviceFault getById(Long id)
    {
        return deviceFaultMapper.selectById(id);
    }

    /**
     * 根据充电桩ID查询故障列表
     *
     * @param pileId 充电桩ID
     * @return 设备故障列表
     */
    @Override
    public List<DeviceFault> getListByPileId(String pileId)
    {
        LambdaQueryWrapper<DeviceFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceFault::getPileId, pileId);
        return deviceFaultMapper.selectList(wrapper);
    }

    /**
     * 复位故障
     *
     * @param id 故障ID
     * @return 结果
     */
    @Override
    public int reset(Long id)
    {
        DeviceFault fault = deviceFaultMapper.selectById(id);
        if (fault == null)
        {
            return 0;
        }
        fault.setResetTime(new Date());
        fault.setStatus(1);
        return deviceFaultMapper.updateById(fault);
    }

    /**
     * 复位故障（指定复位时间）
     *
     * @param id 故障ID
     * @param resetTime 复位时间
     * @return 结果
     */
    @Override
    public int reset(Long id, Date resetTime)
    {
        DeviceFault fault = deviceFaultMapper.selectById(id);
        if (fault == null)
        {
            return 0;
        }
        fault.setResetTime(resetTime);
        fault.setStatus(1);
        return deviceFaultMapper.updateById(fault);
    }
}