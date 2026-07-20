package com.hcp.operator.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.hcp.operator.domain.ChargeOrderRateDetail;
import com.hcp.operator.mapper.ChargeOrderRateDetailMapper;
import com.hcp.operator.service.IChargeOrderRateDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * ChargeOrderRateDetailServiceImpl
 * 订单费率结算服务实现类
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 16:59
 */
@Service
@RequiredArgsConstructor
public class ChargeOrderRateDetailServiceImpl implements IChargeOrderRateDetailService {

    private final ChargeOrderRateDetailMapper chargeOrderRateDetailMapper;

    /*
    * 保存一条订单费率明细
    * */
    @Override
    public int saveOrderRateDetail(ChargeOrderRateDetail detail) {
        return chargeOrderRateDetailMapper.insert(detail);
    }

    /*
    * 批量保存订单费率明细
    * */
    @Override
    public void saveOrderRateDetailBatch(List<ChargeOrderRateDetail> detailList) {
        if (CollUtil.isNotEmpty(detailList)) {
            detailList.forEach(chargeOrderRateDetailMapper::insert);
        }
    }

    /*
    * 查询订单的费率明细
    * */
    @Override
    public List<ChargeOrderRateDetail> getByOrderId(String orderId) {
        return chargeOrderRateDetailMapper.selectByOrderId(orderId);
    }
}
