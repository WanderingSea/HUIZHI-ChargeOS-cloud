package com.hcp.operator.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.*;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.utils.uuid.Seq;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.operator.constant.ChargeStatus;
import com.hcp.operator.constant.FeeConstant;
import com.hcp.operator.constant.OrderState;
import com.hcp.operator.domain.ChargeOrderHourlyEnergy;
import com.hcp.operator.domain.ChargeOrderRateDetail;
import com.hcp.operator.domain.ManagerTotalDataVO;
import com.hcp.operator.domain.QueryChargePileVo;
import com.hcp.operator.mapper.*;
import com.hcp.operator.service.*;
import com.hcp.operator.utils.RandomUtil;
import com.hcp.operator.websocket.WebSocket;
import com.hcp.operator.websocket.bean.MessageDto;
import com.hcp.system.api.RemoteSimulatorService;
import com.hcp.system.api.domain.*;
import com.hcp.system.api.domain.Bo.FeeRangeTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcp.common.core.text.Convert;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.mybatisplus.constant.MybatisPageConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * 充电订单Service业务层处理
 *
 * @author hcp
 * @date 2024-08-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingOrderServiceImpl implements IChargingOrderService
{
    @Autowired
    private ChargingOrderMapper chargingOrderMapper;
    @Autowired
    private OrderLogMapper orderLogMapper;
    @Autowired
    private ChargingPortMapper chargingPortMapper;
    @Autowired
    private ChargingPileMapper chargingPileMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private RemoteSimulatorService simulatorService;

    @Autowired
    private IRateDetailService rateDetailService;
    @Autowired
    private IChargeOrderRateDetailService chargeOrderRateDetailService;
    @Autowired
    private IChargeOrderHourlyEnergyService chargeOrderHourlyEnergyService;
    @Autowired
    private ICustomPriceService customPriceService;

    /**
     * 查询充电订单
     *
     * @param orderId 充电订单主键
     * @return 充电订单
     */
    @Override
    public ChargingOrder selectChargingOrderByOrderId(String orderId)
    {
        return chargingOrderMapper.selectById(orderId);
    }

    @Override
    public AjaxResult selectChargingOrderByOrderNumber(String orderNumber)
    {
        ChargingOrder order = chargingOrderMapper.selectChargingOrderByOrderNumber(orderNumber);
        if(Objects.isNull(order)){
            AjaxResult.error("订单不存在");
        }
        // 查询订单日志数据
        List<OrderLog> logList = orderLogMapper.selectOrderLogListByOrderNumber(orderNumber);
        if(CollUtil.isNotEmpty(logList)){
            logList.sort(Comparator.comparing(OrderLog::getCreateTime));
            order.setLogList(logList);
        }
        return AjaxResult.success(order);
    }

    /**
     * 查询充电订单列表-分页
     *
     * @param chargingOrder 充电订单
     * @return 充电订单
     */
    @Override
    public IPage<ChargingOrder> selectChargingOrderPage(ChargingOrder chargingOrder)
    {
        Page mpPage =new Page(Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_NUM),1L)
                ,Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_SIZE),10L));
        return chargingOrderMapper.selectChargingOrderListPage(mpPage,chargingOrder);
    }

    /**
     * 查询充电订单列表
     *
     * @param chargingOrder 充电订单
     * @return 充电订单
     */
    @Override
    public List<ChargingOrder> selectChargingOrderList(ChargingOrder chargingOrder)
    {
        return chargingOrderMapper.selectChargingOrderList(chargingOrder);
    }

    /**
     * 新增充电订单
     *
     * @param chargingOrder 充电订单
     * @return 结果
     */
    @Override
    public int insertChargingOrder(ChargingOrder chargingOrder)
    {
        return chargingOrderMapper.insert(chargingOrder);
    }

    /**
     * 修改充电订单
     *
     * @param chargingOrder 充电订单
     * @return 结果
     */
    @Override
    public int updateChargingOrder(ChargingOrder chargingOrder)
    {
        return chargingOrderMapper.updateById(chargingOrder);
    }

    /**
     * 批量删除充电订单
     *
     * @param orderIds 需要删除的充电订单主键
     * @return 结果
     */
    @Override
    public int deleteChargingOrderByOrderIds(String[] orderIds)
    {
        return chargingOrderMapper.deleteChargingOrderByOrderIds(orderIds);
    }

    /**
     * 删除充电订单信息
     *
     * @param orderId 充电订单主键
     * @return 结果
     */
    @Override
    public int deleteChargingOrderByOrderId(String orderId)
    {
        return chargingOrderMapper.deleteById(orderId);
    }

    @Override
    public ManagerTotalDataVO getManageTotalData(QueryChargePileVo pileVo) {

        Long userId = SecurityUtils.getUserId();
        // 非管理员或未选择用户 则查询当前用户数据
        if (!SysUser.isAdmin(userId) && pileVo.getUserId()==null)
        {
            pileVo.setUserId(userId);
        }
        ManagerTotalDataVO vo = chargingOrderMapper.getManageTotalData(pileVo);

        vo.setSumSales(vo.getSumSales().setScale(2, RoundingMode.HALF_UP));
        vo.setReSales(vo.getReSales().setScale(2, RoundingMode.HALF_UP));

        vo.setRealSales(vo.getSumSales().subtract(vo.getReSales()).setScale(2,RoundingMode.HALF_UP));

        vo.setChargeFee(vo.getChargeFee().setScale(2, RoundingMode.HALF_UP));
        vo.setServiceFee(vo.getServiceFee().setScale(2, RoundingMode.HALF_UP));

        vo.setConsumePower(vo.getConsumePower().setScale(2,RoundingMode.HALF_UP));
        vo.setChargeTimes(vo.getSumCount());


        Integer realHour = Integer.valueOf(vo.getRealHour());
        String lastTotalHour  =    realHour / 60 + "小时" + realHour % 60 + "分钟";

        vo.setRealHour(lastTotalHour);

        return vo;
    }

    @Override
    public ChargingOrder startChargeOrder(String pileId, String port,Long userId, BigDecimal amount,Integer hour) {
        ChargingPile chargingPile = chargingPileMapper.getById(pileId);
        Assert.notNull(chargingPile,"充电桩未录入");
        ChargingPort chargingPort = chargingPortMapper.selectPort(pileId, port);
        Assert.notNull(chargingPort,"接口信息不存在");
        if (null==userId){
            userId = SecurityUtils.getUserId();
        }
        ChargingOrder chargingOrder = new ChargingOrder();
//        String orderId = pileId.concat(port).concat(LocalDateTimeUtil.format(LocalDateTime.now(),"yyyyMMddHHmmss")).concat(Seq.getId(new AtomicInteger(1),2));
        String orderNumber = RandomUtil.getRandomNumber(32);
        chargingOrder.setOrderId(orderNumber);

        chargingOrder.setUserId(userId);
        chargingOrder.setOrderNumber(orderNumber);
        chargingOrder.setOrderState(OrderState.PLACE);
        chargingOrder.setPileId(pileId);
        chargingOrder.setDeviceType(4L);
        chargingOrder.setPortId(chargingPort.getPortId());
        chargingOrder.setStartTime(LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss"));
        chargingOrder.setChargeStatus(ChargeStatus.PRE_CHARGE);
        chargingOrder.setIsFee(FeeConstant.FREE_FEE);
        chargingOrder.setHour(String.valueOf(hour));
        chargingOrder.setTenantId(chargingPort.getTenantId());
        chargingOrderMapper.insertOrder(chargingOrder);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderNumber(orderNumber);
        orderLog.setMainProcess(1L);
        orderLog.setBriefInfo("客户下单");
        orderLog.setLogContent("用户下单");
        orderLog.setTenantId(chargingPort.getTenantId());
        orderLog.setCreateTime(new Date());
        orderLogMapper.insertOrderLog(orderLog);
        //启动模拟器充电
        chargingOrder.setDeviceId(port);
        R<String> stringR = simulatorService.startCharge(chargingOrder);
        log.info("模拟器启动结果:{}", JSONObject.toJSONString(stringR));
        return chargingOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopChargingOrder(String pileId, String port) {
        ChargingPort chargingPort = chargingPortMapper.selectPort(pileId, port);
        Assert.notNull(chargingPort,"充电口信息为空");
        ChargingOrder  order = chargingOrderMapper.findChargingOrder(pileId,chargingPort.getPortId(),ChargeStatus.CHARGING);
        Assert.notNull(order,"充电中订单信息未找到");
        chargingOrderMapper.updateByPrimaryKey(order);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderNumber(order.getOrderNumber());
        orderLog.setMainProcess(1L);
        orderLog.setBriefInfo("客户结束充电");
        orderLog.setLogContent("用户手动结束充电,结算中");
        orderLog.setTenantId(chargingPort.getTenantId());
        orderLog.setCreateTime(new Date());
        orderLogMapper.insertOrderLog(orderLog);
        R<String> stringR = simulatorService.stopCharge(pileId, port);
        log.info("模拟器停止充电结果:{}", JSONObject.toJSONString(stringR));
    }

    /*
    * 订单完成处理方法
    * */
    @Override
    public void handleOrderInfo(String orderId, Double totalPower, BigDecimal totalAmount, BigDecimal electricFee, BigDecimal serviceFee, String startTime, String endTime, String stopReason) {
        ChargingOrder order = chargingOrderMapper.getById(orderId);
        Assert.notNull(order,"订单信息为空");
        order.setConsumePower(new BigDecimal(totalPower));
        order.setOrdergold(String.valueOf(totalAmount));
        order.setPrice(String.valueOf(electricFee));
        order.setServiceFee(serviceFee);
        order.setRealEndTime(DateUtil.parseDateTime(endTime));
        long realHour = DateUtil.between(DateUtil.parseDateTime(startTime), DateUtil.parseDateTime(endTime), DateUnit.HOUR);
        order.setRealHour(String.valueOf(realHour));
        order.setOrderState(OrderState.PAYED);
        order.setChargeStatus(ChargeStatus.FINISH_CHARGE);
        order.setPayTime(new Date());
        order.setRealEndTime(DateUtil.parseDateTime(endTime));
        chargingOrderMapper.updateByPrimaryKey(order);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderNumber(order.getOrderNumber());
        orderLog.setMainProcess(1L);
        orderLog.setBriefInfo("完成订单");
        orderLog.setLogContent("已完成订单,消费金额:"+totalAmount);
        orderLog.setTenantId(order.getTenantId());
        orderLog.setCreateTime(new Date());
        orderLogMapper.insertOrderLog(orderLog);
    }

    @Override
    public void updateOrderStart(String orderId, String pileId, String port, String startResult, String failReason) {
        ChargingOrder order = chargingOrderMapper.getById(orderId);
        Assert.notNull(order,"订单信息为空");
        order.setChargeStatus(ChargeStatus.CHARGING);
        chargingOrderMapper.updateByPrimaryKey(order);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderNumber(order.getOrderNumber());
        orderLog.setMainProcess(1L);
        orderLog.setCreateTime(new Date());
        orderLog.setBriefInfo("开始充电");
        orderLog.setLogContent("远程充电启动，启动结果:"+startResult+",失败原因:"+failReason);
        orderLog.setTenantId(order.getTenantId());
        orderLogMapper.insertOrderLog(orderLog);
    }

    @Override
    public void updateOrderStop(String pileId, String port, String stopResult, String failReason) {
        ChargingPort chargingPort = chargingPortMapper.selectPort(pileId, port);
        Assert.notNull(chargingPort,"充电口信息为空");
        ChargingOrder  order = chargingOrderMapper.findChargingOrder(pileId,chargingPort.getPortId(),ChargeStatus.CHARGING);
        Assert.notNull(order,"充电中订单信息未找到");
        order.setEndTime(LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss"));
        order.setOrderState(OrderState.SETTLE);
        chargingOrderMapper.updateByPrimaryKey(order);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderNumber(order.getOrderNumber());
        orderLog.setMainProcess(1L);
        orderLog.setCreateTime(new Date());
        orderLog.setBriefInfo("充电结束");
        orderLog.setLogContent("远程充电结束，关闭结果:"+stopResult+",失败原因:"+failReason);
        orderLog.setTenantId(order.getTenantId());
        orderLogMapper.insertOrderLog(orderLog);
    }

    @Override
    public void handleChargingHeartBeat(String orderId, Double voltage, Double electric, String soc, Double chargePower, BigDecimal chargeFee, BigDecimal serviceFee,BigDecimal chargePrice) {
        log.info("收到实时充电数据:订单号:{},电压:{},电流:{},soc:{},已充电量:{},已充金额:{}",orderId,voltage,electric,soc,chargePower,chargePrice);
        ChargingOrder order = chargingOrderMapper.getById(orderId);
        Assert.notNull(order,"订单信息为空");
        order.setChargeStatus(ChargeStatus.CHARGING);
        order.setChargeFee(chargeFee);
        order.setServiceFee(serviceFee);
        order.setOrdergold(String.valueOf(chargePrice));
        order.setChargingCurrent(String.valueOf(electric));
        order.setConsumePower(BigDecimal.valueOf(chargePower));
        order.setHour(String.valueOf(DateUtil.between(DateUtil.parseDateTime(order.getStartTime()),new Date(), DateUnit.HOUR)));
        long diffInMillis = DateUtil.between(DateUtil.parseDateTime(order.getStartTime()), new Date(), DateUnit.MS);
        double diffInHours = diffInMillis / 3600000.0;
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedHours = df.format(diffInHours);
        order.setRealHour(formattedHours);
        order.setChargingCdgl(String.valueOf(NumberUtil.mul(voltage,electric)));
        chargingOrderMapper.updateByPrimaryKey(order);
        MessageDto messageDto  = MessageDto.builder().hasChargePower(BigDecimal.valueOf(chargePower))
                .totalFee(chargePrice).chargeMin((int) DateUtil.between(DateUtil.parseDateTime(order.getStartTime()),new Date(),DateUnit.MINUTE)).preEndMin(12).serviceFee(serviceFee).powerFee(chargeFee)
                .soc(Integer.valueOf(soc)).realTimePower(NumberUtil.mul(voltage,electric)).voltage(voltage.floatValue()).electricity(electric.floatValue()).build();
        WebSocket.sendMessageToOrder(orderId, JSONUtil.toJsonStr(messageDto));
    }

    @Override
    public Page<ChargingOrder> queryOrderList(ChargingOrder chargingOrder) {
        Page mpPage =new Page(chargingOrder.getPageNo(),chargingOrder.getPageSize());
        return chargingOrderMapper.selectChargingOrderListPage(mpPage,chargingOrder);
    }

    /**
     * V2.0 订单完成处理方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleV2OrderSettlement(String orderId, Double totalPower, BigDecimal totalAmount,
                                          BigDecimal electricFee, BigDecimal serviceFee, String startTime, String endTime,
                                          String stopReason,
                                          String meterNumber, String meterCipher,
                                          BigDecimal meterStartValue, BigDecimal meterEndValue,
                                          String vinCode, BigDecimal lossTotalPower, Integer tradeType){
        // 调用原有的基础结算逻辑
        this.handleOrderInfo(orderId, totalPower, totalAmount, electricFee, serviceFee,
                startTime, endTime, stopReason);

        // 更新新增的7个字段
        ChargingOrder order = chargingOrderMapper.getById(orderId);
        order.setMeterNumber(meterNumber)
              .setMeterCipher(meterCipher)
              .setMeterStartValue(meterStartValue)
              .setMeterEndValue(meterEndValue)
              .setVinCode(vinCode)
              .setLossTotalPower(lossTotalPower)
              .setTradeType(tradeType);
        chargingOrderMapper.updateByPrimaryKey(order);

        // 48段费率计算 + 分时电量 + 费率明细写入
        this.processV2RateSettlement(order,startTime,endTime,totalPower);
    }

    /**
     * V2.0 费率结算处理：
     * 1. 48段费率计算
     * 2. 分时电量写入
     * 3. 费率明细写入
     */
    private void processV2RateSettlement(ChargingOrder order, String startTime, String endTime, Double totalPower) {
          // 获取该桩的费率模板
          List<FeeRangeTime> feeRangeList = customPriceService.getPriceByPileId(order.getPileId());
          if(CollUtil.isEmpty(feeRangeList)){
              log.warn("桩{}未配置费率，跳过V2.0费率结算", order.getPileId());
              return;
          }
          // 解析充电起止时间
          DateTime chargeStart = DateUtil.parseDateTime(startTime);
          DateTime chargeEnd = DateUtil.parseDateTime(endTime);
          long totalMinutes = DateUtil.between(chargeStart,chargeEnd,DateUnit.MINUTE);
          if(totalMinutes <= 0) return;

          // ===== A. 分时电量计算（48 slot，每半小时）=====
          this.calcAndSaveHourlyEnergy(order.getOrderId(), chargeStart, chargeEnd, totalPower, totalMinutes);

          // ===== B. 费率段匹配计算 =====
          this.calcAndSaveOrderRateDetails(order.getOrderId(), chargeStart, chargeEnd,
                totalPower, totalMinutes, feeRangeList);
    }

    /**
     * 分时电量计算：按半小时切分充电时段，均摊电量
     */
    private void calcAndSaveHourlyEnergy(String orderId, DateTime chargeStart,
                                         DateTime chargeEnd, Double totalPower, long totalMinutes) {
        List<ChargeOrderHourlyEnergy> energyList = new ArrayList<>();

        // 从充电开始时间，按半小时递增，直到结束
        DateTime cursor = DateUtil.beginOfDay(chargeStart).offset(DateField.MINUTE
                                  ,getSlotIndex(chargeStart) * 30);
        while(cursor.isBefore(chargeEnd)) {
            DateTime slotEnd = DateUtil.offset(cursor, DateField.MINUTE, 30);
            // 计算该slot在充电时段内的有效分钟数
            long effectiveMinutes = Math.min(
                    DateUtil.between(
                            // 取较晚的时间：如果 cursor 在 chargeStart 之前，用 chargeStart
                            cursor.isBefore(chargeStart) ? chargeStart : cursor,
                            // 取较早的时间：如果 slotEnd 在 chargeEnd 之后，用 chargeEnd
                            slotEnd.isAfter(chargeEnd) ? chargeEnd : slotEnd,
                            DateUnit.MINUTE
                    ), 30
            );
            if (effectiveMinutes > 0) {
                double slotEnergy = (effectiveMinutes / (double) totalMinutes) * totalPower;
                int slotIndex = getSlotIndex(cursor);

                ChargeOrderHourlyEnergy energy = new ChargeOrderHourlyEnergy();
                energy.setOrderId(orderId);
                energy.setSlotIndex(slotIndex);
                energy.setEnergy(BigDecimal.valueOf(slotEnergy).setScale(4, RoundingMode.HALF_UP));
                energyList.add(energy);
            }
            cursor = DateUtil.offset(cursor, DateField.MINUTE, 30);
        }

        if (CollUtil.isNotEmpty(energyList)) {
            chargeOrderHourlyEnergyService.saveHourlyEnergyBatch(energyList);
            log.info("订单{}分时电量写入完成，共{}条", orderId, energyList.size());
        }
    }


    /**
     *  费率段匹配计算：匹配充电时段与费率时段，计算每段的电量和金额
     */
    private void calcAndSaveOrderRateDetails(String orderId, DateTime chargeStart,
                                             DateTime chargeEnd, Double totalPower, long totalMinutes,
                                             List<FeeRangeTime> feeRangeList) {
        List<ChargeOrderRateDetail> detailList = new ArrayList<>();

        for (int i = 0; i < feeRangeList.size(); i++) {
            FeeRangeTime range = feeRangeList.get(i);
            // 费率段的起止时间（转换为当天的DateTime）
            DateTime rangeStart = DateUtil.parseTimeToday(String.valueOf(range.getStartTime()));
            DateTime rangeEnd = DateUtil.parseTimeToday(String.valueOf(range.getEndTime()));

            // 计算该费率段与充电时段的重叠分钟数
            long overlapMinutes = getOverlapMinutes(chargeStart, chargeEnd, rangeStart, rangeEnd);
            if (overlapMinutes <= 0) continue;

            // 按时间比例分配电量
            double segmentEnergy = (overlapMinutes / (double) totalMinutes) * totalPower;
            // 计算金额（电费 + 服务费）
            BigDecimal ratePrice = BigDecimal.valueOf(range.getServicePrice().doubleValue());
            BigDecimal amount = ratePrice.multiply(BigDecimal.valueOf(segmentEnergy));

            ChargeOrderRateDetail detail = new ChargeOrderRateDetail();
            detail.setOrderId(orderId)
                  .setRateIndex(i + 1)
                  .setRatePrice(ratePrice)
                  .setEnergy(BigDecimal.valueOf(segmentEnergy).setScale(4, RoundingMode.HALF_UP))
                  .setLossEnergy(BigDecimal.ZERO)  // 计损电量，按实际策略调整
                  .setAmount(amount.setScale(4, RoundingMode.HALF_UP));
            detailList.add(detail);
        }

        if (CollUtil.isNotEmpty(detailList)) {
            chargeOrderRateDetailService.saveOrderRateDetailBatch(detailList);
            log.info("订单{}费率明细写入完成，共{}条", orderId, detailList.size());
        }
    }


    /**
     * 工具方法：计算两个时段的重叠分钟数
     */
    private long getOverlapMinutes(DateTime chargeStart, DateTime chargeEnd,
                                   DateTime rangeStart, DateTime rangeEnd) {
        // 取两个时间中较晚的作为重叠开始时间
        DateTime overlapStart = chargeStart.isBefore(rangeStart) ? rangeStart : chargeStart;
        // 取两个时间中较早的作为重叠结束时间
        DateTime overlapEnd = chargeEnd.isAfter(rangeEnd) ? rangeEnd : chargeEnd;
        if (overlapStart.isBefore(overlapEnd)) {
            return DateUtil.between(overlapStart, overlapEnd, DateUnit.MINUTE);
        }
        return 0;
    }

    /**
     * 工具方法：获取时间对应的slot索引（0~47）
     */
    private int getSlotIndex(DateTime time) {
           int hour = DateUtil.hour(time, true);
           int minute = DateUtil.minute(time);
           return hour * 2 + (minute >= 30 ? 1 : 0);
    }






}
