package com.hcp.operator.service;

import com.hcp.operator.domain.ChargeStartupLog;
import com.hcp.system.api.domain.dto.StartupCompleteDTO;

/**
 * 充电启动日志Service接口
 *
 * @author huayue
 * @date 2026-07-16
 */
public interface IChargeStartupLogService
{
    /**
     * 新增充电启动日志
     *
     * @param chargeStartupLog 充电启动日志
     * @return 结果
     */
    int save(ChargeStartupLog chargeStartupLog);

    /**
     * 根据订单ID查询充电启动日志
     *
     * @param orderId 订单ID
     * @return 充电启动日志
     */
    ChargeStartupLog getByOrderId(String orderId);

    /**
     * 充电机启动完成上报（V2.0）
     *
     * @param dto 启动完成DTO
     */
    void saveStartupComplete(StartupCompleteDTO dto);
}
