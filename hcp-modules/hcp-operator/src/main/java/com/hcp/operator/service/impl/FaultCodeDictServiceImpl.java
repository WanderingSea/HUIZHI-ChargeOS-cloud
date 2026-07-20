package com.hcp.operator.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcp.operator.domain.FaultCodeDict;
import com.hcp.operator.mapper.FaultCodeDictMapper;
import com.hcp.operator.service.IFaultCodeDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 故障码字典Service业务层处理
 *
 * @author huayue
 * @date 2026-07-16
 */
@Service
public class FaultCodeDictServiceImpl implements IFaultCodeDictService
{
    @Autowired
    private FaultCodeDictMapper faultCodeDictMapper;

    /**
     * 查询故障码字典列表
     *
     * @return 故障码字典列表
     */
    @Override
    public List<FaultCodeDict> getList()
    {
        return faultCodeDictMapper.selectList();
    }

    /**
     * 根据故障类型和故障码查询
     *
     * @param faultType 故障类型
     * @param faultCode 故障码
     * @return 故障码字典
     */
    @Override
    public FaultCodeDict getByTypeAndCode(Integer faultType, Integer faultCode)
    {
        LambdaQueryWrapper<FaultCodeDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaultCodeDict::getFaultType, faultType);
        wrapper.eq(FaultCodeDict::getFaultCode, faultCode);
        return faultCodeDictMapper.selectOne(wrapper);
    }
}