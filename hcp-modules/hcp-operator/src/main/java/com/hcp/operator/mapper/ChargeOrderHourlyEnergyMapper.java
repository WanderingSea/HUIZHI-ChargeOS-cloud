package com.hcp.operator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hcp.common.mybatisplus.mapper.BaseMapperX;
import com.hcp.operator.domain.ChargeOrderHourlyEnergy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * ChargeOrderHourlyEnergyMapper
 * 订单小时充电电量明细mapper
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:43
 */
public interface ChargeOrderHourlyEnergyMapper extends BaseMapperX<ChargeOrderHourlyEnergy> {

    /**
     * 根据订单号查询订单每小时充电电量明细
     */
    List<ChargeOrderHourlyEnergy> selectByOrderId(@Param("orderId") String orderId);


}
