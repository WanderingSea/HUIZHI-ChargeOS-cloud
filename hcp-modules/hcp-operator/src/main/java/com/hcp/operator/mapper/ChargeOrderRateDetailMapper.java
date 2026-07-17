package com.hcp.operator.mapper;

import com.hcp.common.mybatisplus.mapper.BaseMapperX;
import com.hcp.operator.domain.ChargeOrderRateDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * ChargeOrderRateDetailMapper
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:41
 */
public interface ChargeOrderRateDetailMapper extends BaseMapperX<ChargeOrderRateDetail> {

    /**
     * 根据订单号查询费率明细
     */
    List<ChargeOrderRateDetail> selectByOrderId(@Param("orderId") String orderId);

}
