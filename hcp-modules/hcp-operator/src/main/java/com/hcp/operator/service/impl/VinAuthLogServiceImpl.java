package com.hcp.operator.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcp.operator.domain.VinAuthLog;
import com.hcp.operator.mapper.VinAuthLogMapper;
import com.hcp.operator.service.IVinAuthLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * VIN认证日志Service业务层处理
 *
 * @author huayue
 * @date 2026-07-16
 */
@Service
public class VinAuthLogServiceImpl implements IVinAuthLogService
{
    @Autowired
    private VinAuthLogMapper vinAuthLogMapper;

    /**
     * 新增VIN认证日志
     *
     * @param vinAuthLog VIN认证日志
     * @return 结果
     */
    @Override
    public int save(VinAuthLog vinAuthLog)
    {
        return vinAuthLogMapper.insert(vinAuthLog);
    }

    /**
     * 根据充电桩ID查询VIN认证日志列表
     *
     * @param pileId 充电桩ID
     * @return VIN认证日志列表
     */
    @Override
    public List<VinAuthLog> getListByPileId(String pileId)
    {
        LambdaQueryWrapper<VinAuthLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VinAuthLog::getPileId, pileId);
        return vinAuthLogMapper.selectList(wrapper);
    }
}