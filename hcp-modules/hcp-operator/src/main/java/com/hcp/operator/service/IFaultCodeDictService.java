package com.hcp.operator.service;

import java.util.List;
import com.hcp.operator.domain.FaultCodeDict;

/**
 * 故障码字典Service接口
 *
 * @author huayue
 * @date 2026-07-16
 */
public interface IFaultCodeDictService
{
    /**
     * 查询故障码字典列表
     *
     * @return 故障码字典列表
     */
    List<FaultCodeDict> getList();

    /**
     * 根据故障类型和故障码查询
     *
     * @param faultType 故障类型
     * @param faultCode 故障码
     * @return 故障码字典
     */
    FaultCodeDict getByTypeAndCode(Integer faultType, Integer faultCode);
}