package com.hcp.operator.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.hcp.operator.domain.RateDetail;
import com.hcp.operator.mapper.RateDetailMapper;
import com.hcp.operator.service.IRateDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;


/**
 * <p>
 * RateDetailServiceImpl
 * 动态费率段服务实现类
 * </p>
 *
 * @author JHan
 * @Date 2026-07-16 16:56
 */
@Service
@RequiredArgsConstructor
public class RateDetailServiceImpl implements IRateDetailService {

    private final RateDetailMapper rateDetailMapper;

    /*
    * 查询费率段列表-根据priceId
    * */
    @Override
    public List<RateDetail> getByPriceId(Long priceId) {
        return rateDetailMapper.selectByPriceId(priceId);
    }

    /*
    * 批量保存费率段-根据priceId
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRateDetails(Long priceId, List<RateDetail> details) {
        // 先删除旧的费率段
        rateDetailMapper.deleteByPriceId(priceId);
        // 批量插入新的费率段
        if (CollUtil.isNotEmpty(details)) {
            for (RateDetail detail : details) {
                detail.setPriceId(priceId);
                rateDetailMapper.insert(detail);
            }
        }
    }

}
