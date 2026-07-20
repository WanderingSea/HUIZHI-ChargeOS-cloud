package com.hcp.operator.service;

import java.util.List;
import com.hcp.operator.domain.VinAuthLog;
import com.hcp.system.api.domain.dto.VinAuthDTO;

/**
 * VIN认证日志Service接口
 *
 * @author huayue
 * @date 2026-07-16
 */
public interface IVinAuthLogService
{
    /**
     * 新增VIN认证日志
     *
     * @param vinAuthLog VIN认证日志
     * @return 结果
     */
    int save(VinAuthLog vinAuthLog);

    /**
     * 根据充电桩ID查询VIN认证日志列表
     *
     * @param pileId 充电桩ID
     * @return VIN认证日志列表
     */
    List<VinAuthLog> getListByPileId(String pileId);

    /**
     * VIN码鉴权上报（V2.0）
     *
     * @param dto VIN鉴权DTO
     */
    void saveVinAuth(VinAuthDTO dto);
}
