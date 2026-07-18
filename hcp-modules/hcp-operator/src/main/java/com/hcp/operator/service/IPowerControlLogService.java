package com.hcp.operator.service;

import java.util.List;
import com.hcp.operator.domain.PowerControlLog;

/**
 * 功率控制日志Service接口
 *
 * @author huayue
 * @date 2026-07-16
 */
public interface IPowerControlLogService
{
    /**
     * 新增功率控制日志
     *
     * @param powerControlLog 功率控制日志
     * @return 结果
     */
    int save(PowerControlLog powerControlLog);

    /**
     * 根据充电桩ID查询功率控制日志列表
     *
     * @param pileId 充电桩ID
     * @return 功率控制日志列表
     */
    List<PowerControlLog> getListByPileId(String pileId);
}