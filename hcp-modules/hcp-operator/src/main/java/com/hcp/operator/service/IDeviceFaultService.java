package com.hcp.operator.service;

import java.util.Date;
import java.util.List;
import com.hcp.operator.domain.DeviceFault;

/**
 * 设备故障Service接口
 *
 * @author huayue
 * @date 2026-07-16
 */
public interface IDeviceFaultService
{
    /**
     * 新增设备故障
     *
     * @param deviceFault 设备故障
     * @return 结果
     */
    int save(DeviceFault deviceFault);

    /**
     * 根据ID查询设备故障
     *
     * @param id 故障ID
     * @return 设备故障
     */
    DeviceFault getById(Long id);

    /**
     * 根据充电桩ID查询故障列表
     *
     * @param pileId 充电桩ID
     * @return 设备故障列表
     */
    List<DeviceFault> getListByPileId(String pileId);

    /**
     * 复位故障
     *
     * @param id 故障ID
     * @return 结果
     */
    int reset(Long id);

    /**
     * 复位故障（指定复位时间）
     *
     * @param id 故障ID
     * @param resetTime 复位时间
     * @return 结果
     */
    int reset(Long id, Date resetTime);
}