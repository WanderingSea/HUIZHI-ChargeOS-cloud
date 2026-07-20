package com.hcp.operator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hcp.common.mybatisplus.mapper.BaseMapperX;
import com.hcp.operator.domain.RateDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * RateDetailMapper
 * </p>
 *
 * @author JHan
 * @Date 2026/7/16 16:36
 */
@Mapper
public interface RateDetailMapper extends BaseMapperX<RateDetail> {

    /**
     * 根据priceId查询所有费率段（按rate_index排序）
     */
    List<RateDetail> selectByPriceId(@Param("priceId") Long priceId);

    /**
     * 删除某个定价规则的所有费率段
     */
    int deleteByPriceId(@Param("priceId") Long priceId);

}
