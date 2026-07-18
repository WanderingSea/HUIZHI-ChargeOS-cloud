package com.hcp.simulator.simulation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hcp.common.core.exception.base.BaseException;
import com.hcp.simulator.dto.ChargingOrderDTO;
import com.hcp.system.api.RemoteChargingService;
import com.hcp.system.api.domain.ChargingPile;
import com.hcp.system.api.domain.ChargingPort;
import com.hcp.simulator.dto.ChargeInfoDTO;
import com.hcp.simulator.service.ChargingOrderService;
import com.hcp.simulator.simulation.interfaces.SimPileIClient;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.hcp.system.api.domain.dto.FaultReportDTO;
import com.hcp.system.api.domain.dto.FaultResetDTO;
import com.hcp.system.api.domain.dto.StartupCompleteDTO;
import com.hcp.system.api.domain.dto.VinAuthDTO;
import com.hcp.system.api.domain.dto.PowerControlDTO;
import com.hcp.system.api.domain.dto.HourlyEnergyDTO;
import com.hcp.system.api.domain.dto.RateDetailDTO;
import com.hcp.system.api.domain.dto.OrderRateDetailDTO;

@Log4j2
@Data
public class SimPileClientImpl implements SimPileIClient {

    private String connectorId;

    /**
     * 端口对应的充电枪信息
     */
    Map<String, ChargingPort> portMap;

    /**
     * 端口对应的充电信息
     */
    Map<String, ChargeInfoDTO> chargeInfoMap = new ConcurrentHashMap<>();

    /**
     * 端口对应的订单信息
     */
    Map<String, ChargingOrderDTO> chargingOrderMap = new ConcurrentHashMap<>();

    /**
     * 充电桩信息
     */
    private ChargingPile chargingPile;

    ConcurrentHashMap<String, Long> lastRealtimeMap = new ConcurrentHashMap<>();
    Map<String, ChargingOrderDTO> chargeOrderItemMap;
    private ChargingOrderService chargingOrderService;
    private RemoteChargingService remoteChargingService;


    public SimPileClientImpl(ChargingPile chargingPile, List<ChargingPort> chargingPorts) {
        chargeOrderItemMap = new ConcurrentHashMap<>();
        this.chargingPile = chargingPile;
        this.portMap = chargingPorts.stream().collect(Collectors.toConcurrentMap(ChargingPort::getDeviceId, chargingPort -> chargingPort));
        this.chargingOrderService = SpringUtil.getBean(ChargingOrderService.class);
        this.remoteChargingService = SpringUtil.getBean(RemoteChargingService.class);
    }

    @Override
    public void start() throws BaseException {
        synchronized (this) {
            for (ChargingPort chargingPort : portMap.values()) {
                chargingPort.setState("N");
                chargingPort.setGunStatus(0L);
                log.info("充电桩：{},{}初始化成功!", chargingPile.getName(), chargingPort.getName());
            }
            this.chargingPile.setRunningStatus(0L);
        }
        this.startHeart();
    }

    @Override
    public void stop() throws BaseException {
        synchronized (this) {
            chargingPile.setRunningStatus(1L);
        }
        //关闭未结束的订单
        for (ChargingOrderDTO chargingOrder : chargingOrderMap.values()) {
            sendTradeInfo(chargingOrder.getDeviceId(), "充电桩下线,停止充电订单");
        }
        for (ChargingPort chargingPort : portMap.values()) {
            chargingPort.setState("N");
        }
        log.info("【模拟桩{}】下线", chargingPile.getPileId());
    }

    @Override
    public Long link(String deviceId) throws BaseException {
        ChargingPort chargingPort = portMap.get(deviceId);
        ChargingOrderDTO chargingOrderDTO = chargingOrderMap.get(deviceId);
        if (chargingOrderDTO != null) {
            if (chargingPort.getGunStatus() == 1) {
                log.info("=====》充电中无法插枪{}-{}", chargingPile.getPileId(), chargingPort.getGunStatus());
                return chargingPort.getGunStatus();
            }
        }
        try {
            remoteChargingService.gunInsert(chargingPile.getPileId(), deviceId, "1");
        } catch (Exception e) {
            log.error("远程调用更新插枪状态接口失败:{}", e.getMessage());
        }
        log.info("=====》插枪");
        if (chargingOrderDTO != null) {
            //已有订单，更新为充电中
            chargingPort.setGunStatus(1L);
        }else {
            //没有订单，更新为正在启动充电
            chargingPort.setGunStatus(3L);
        }
        startSendRealTime(deviceId);
        return chargingPort.getGunStatus();
    }

    @Override
    public Long unlink(String deviceId) throws BaseException {
        ChargingPort chargingPort = portMap.get(deviceId);
        if (chargingPort.getGunStatus() == 1) {
            log.info("=====》充电中无法拔枪");
            return chargingPort.getGunStatus();
        }
        try {
            remoteChargingService.gunInsert(chargingPile.getPileId(), deviceId, "0");
        } catch (Exception e) {
            log.error("远程调用更新拔枪状态接口失败:{}", e.getMessage());
        }
        log.info("=====》拔枪");
        chargingPort.setState("N");
        chargingPort.setGunStatus(0L);
        sendRealTimeData(true, deviceId);
        return chargingPort.getGunStatus();
    }

    @Override
    public Long startCharge(ChargingOrderDTO chargingOrder) throws BaseException {
        ChargingPort chargingPort;
        synchronized (this) {
            chargingPort = portMap.get(chargingOrder.getDeviceId());
            log.info("充电桩{}状态码：{}", chargingPile.getPileId(), chargingPile.getRunningStatus());
            chargeOrderItemMap = new HashMap<>();
            if(chargingPort.getGunStatus() == 0){
                //没有插枪，更新为正在启动充电
                chargingPort.setGunStatus(3L);
            }else if(chargingPort.getGunStatus() == 3){
                //已插枪，更新为正在启动充电
                chargingPort.setGunStatus(1L);
            } else {
                log.warn("【充电订单-充电失败】订单号：{}，桩编号：{}，端口号：{}",chargingOrder.getOrderId(),chargingPort.getPileId() ,chargingPort.getDeviceId());
                throw new BaseException("正在充电中");
            }
        }
        log.info("【充电订单-开始充电】订单号：{}，桩编号：{},端口号：{}", chargingOrder.getOrderId(), this.chargingPile.getPileId(), chargingOrder.getDeviceId());
        this.chargingOrderMap.put(chargingOrder.getDeviceId(), chargingOrder);
        startSendRealTime(chargingOrder.getDeviceId());
        return chargingPort.getGunStatus();
    }

    @Override
    public Long stopCharge(String deviceId) throws BaseException {
        ChargingOrderDTO chargingOrder = this.chargingOrderMap.get(deviceId);
        if (chargingOrder == null) {
            log.info("【手动停止充电】桩编号：,端口号：" + this.chargingPile.getPileId(), deviceId);
            //端口不存在订单直接停止
            this.portMap.get(deviceId).setState("N");
            this.portMap.get(deviceId).setGunStatus(0L);
            return 0L;
        }
        log.info("【手动停止充电】桩编号：{},端口号：{},订单号：{}", this.chargingPile.getPileId(), deviceId, chargingOrder.getOrderId());
        ChargingPort chargingPort = portMap.get(chargingOrder.getDeviceId());
        if (chargingPort.getGunStatus() == 1) {
            chargingPort.setState("N");
            chargingPort.setGunStatus(0L);
            sendRealTimeData(true, deviceId);
        } else {
            throw new BaseException("【手动停止充电-停止失败】桩编号：" + this.chargingPile.getPileId() + "端口号：" + deviceId + "订单号" + chargingOrder.getOrderId());
        }
        log.info("【手动停止充电-发送停机回复】桩编号：{}", chargingPile.getPileId());
        sendTradeInfo(deviceId, "手动停止充电订单");
        return chargingPort.getGunStatus();
    }

    @Override
    public void sendRealTimeData(Boolean sendForce, String deviceId) throws BaseException {
        try {
            ChargingOrderDTO chargingOrder = chargingOrderMap.get(deviceId);
            long minDuration = (chargingOrder == null) ? 60000 : 15000;
            if ((System.currentTimeMillis() - lastRealtimeMap.getOrDefault(deviceId, 0L) < minDuration) && !sendForce) {
                return;
            }
            lastRealtimeMap.put(deviceId, System.currentTimeMillis());
            ChargingPort chargingPort = portMap.get(deviceId);
            ChargeInfoDTO chargeInfoDTO = chargeInfoMap.get(chargingPort.getDeviceId());
            if (chargeInfoDTO == null || chargingOrder == null || chargingPort.getGunStatus() == 0 || chargingPort.getGunStatus() == 3) {
                chargeInfoDTO = new ChargeInfoDTO(chargingPort.getPileType(), chargingPort.getDeviceId(), chargingPort.getGunStatus(), 0D, 0D, 0F,
                        0F, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Date(), new Date());
                chargeInfoMap.put(chargingPort.getDeviceId(), chargeInfoDTO);
            } else {
                chargeInfoDTO.setGunStatus(chargingPort.getGunStatus());
                chargeInfoDTO.setVoltage((double) RandomUtil.randomInt(240, 500));
                chargeInfoDTO.setElectric((double) RandomUtil.randomInt(30, 100));
                chargeInfoDTO.setSoc(chargeInfoDTO.getSoc() + Math.round(RandomUtil.randomFloat(0.1F, 1F) * 10) / 10.0F);
                chargeInfoDTO.setChargePower(chargeInfoDTO.getChargePower() + Math.round(RandomUtil.randomFloat(0.1F, 0.8F) * 10) / 10.0F);
                chargeInfoDTO.setChargeFee(BigDecimal.valueOf(chargeInfoDTO.getChargePower() * 1.5F));
                chargeInfoDTO.setServiceFee(chargeInfoDTO.getServiceFee().add(BigDecimal.valueOf(0.1F)));
                chargeInfoDTO.setChargePrice(chargeInfoDTO.getChargeFee().add(chargeInfoDTO.getServiceFee()));
                chargeInfoDTO.setEndTime(new Date());
                try {
                    remoteChargingService.chargingHeartBeat(chargingPile.getPileId(), chargingPort.getDeviceId(), chargingOrder.getOrderId(), String.valueOf(chargeInfoDTO.getGunStatus()),
                            chargeInfoDTO.getVoltage(), chargeInfoDTO.getElectric(), String.valueOf(NumberUtil.parseInt(String.valueOf(chargeInfoDTO.getSoc()))), Double.valueOf(chargeInfoDTO.getChargePower()),
                            chargeInfoDTO.getChargeFee(), chargeInfoDTO.getServiceFee(), chargeInfoDTO.getChargePrice());
                } catch (Exception e) {
                    log.error("远程调用充电中心跳接口失败", e);
                }
            }
            if (chargeInfoDTO.getSoc() >= 100) {
                if (chargingOrder != null) {
                    log.info("订单{}：充电桩{}({}),充满电自动停止", chargingOrder.getOrderId(),chargingPile.getPileId(), deviceId);
                }
                chargeInfoDTO.setSoc(100F);
                sendTradeInfo(deviceId, "充满电自动停止");
            }
        } catch (Exception e) {
            log.error("{}充电桩({}),{}({}),发送实时状态数据失败:{}", chargingPile.getName(),chargingPile.getPileId(),portMap.get(deviceId).getName(),deviceId,e.getMessage());
        }
    }

    @Override
    public void sendTradeInfo(String deviceId, String stopReason) throws BaseException {
        try {
            ChargingOrderDTO chargingOrder = chargingOrderMap.get(deviceId);
            //充电完成后，更新充电订单信息，发送充电数据
            if (chargingOrder == null) {
                log.error("充电桩{}，端口号{}，充电订单不存在!", this.chargingPile.getPileId(), deviceId);
                return;
            }
            ChargeInfoDTO chargeInfoDTO = chargeInfoMap.get(chargingOrder.getDeviceId());
            chargeInfoDTO.setEndTime(new Date());
            try {
                remoteChargingService.orderInfo(chargingOrder.getOrderId(), Double.valueOf(chargeInfoDTO.getChargePower()),
                        DateUtil.formatDateTime(chargeInfoDTO.getStartTime()), DateUtil.formatDateTime(chargeInfoDTO.getEndTime()),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, stopReason);

                // === V2.0 新增：费率段同步 + 订单费率明细 + 分时电量 ===
                try {
                    // 1. 费率段同步：构造多段费率（模拟全天费率）
                    List<RateDetailDTO> rateDetailList = new ArrayList<>();
                    // 峰时段 08:00-12:00
                    rateDetailList.add(new RateDetailDTO(1L, 1,
                            new BigDecimal("1.2"), new BigDecimal("0.6"), "08:00:00", "12:00:00"));
                    // 平时段 12:00-18:00
                    rateDetailList.add(new RateDetailDTO(1L, 2,
                            new BigDecimal("0.8"), new BigDecimal("0.4"), "12:00:00", "18:00:00"));
                    // 谷时段 18:00-24:00
                    rateDetailList.add(new RateDetailDTO(1L, 3,
                            new BigDecimal("0.4"), new BigDecimal("0.2"), "18:00:00", "23:59:59"));
                    if (CollUtil.isNotEmpty(rateDetailList)) {
                        remoteChargingService.rateDetailSync(rateDetailList);
                    }

                    // 2. 订单费率明细：按实际充电时长拆分为多条
                    double totalPower = chargeInfoDTO.getChargePower();
                    // 假设充电时长2小时，拆分3段（按整点）
                    double[] segmentPowers = {totalPower * 0.4, totalPower * 0.35, totalPower * 0.25};
                    BigDecimal[] segmentPrices = {new BigDecimal("1.8"), new BigDecimal("1.2"), new BigDecimal("0.6")};
                    for (int i = 0; i < segmentPowers.length; i++) {
                        OrderRateDetailDTO detailDTO = new OrderRateDetailDTO();
                        detailDTO.setOrderId(chargingOrder.getOrderId());
                        detailDTO.setRateIndex(i + 1);
                        detailDTO.setRatePrice(segmentPrices[i]);
                        detailDTO.setEnergy(BigDecimal.valueOf(segmentPowers[i]).setScale(4, RoundingMode.HALF_UP));
                        detailDTO.setLossEnergy(BigDecimal.ZERO);
                        detailDTO.setAmount(BigDecimal.valueOf(segmentPowers[i])
                                .multiply(segmentPrices[i]).setScale(4, RoundingMode.HALF_UP));
                        remoteChargingService.orderRateDetail(detailDTO);
                    }

                    // 3. 分时电量：按充电起止时间，每半小时上报一条
                    double powerPerSlot = totalPower / 4; // 假设2小时=4个slot
                    int startSlot = getSlotIndex(chargeInfoDTO.getStartTime());
                    for (int i = 0; i < 4; i++) {
                        HourlyEnergyDTO energyDTO = new HourlyEnergyDTO();
                        energyDTO.setOrderId(chargingOrder.getOrderId());
                        energyDTO.setSlotIndex(startSlot + i);
                        energyDTO.setEnergy(BigDecimal.valueOf(powerPerSlot).setScale(4, RoundingMode.HALF_UP));
                        remoteChargingService.hourlyEnergyReport(energyDTO);
                    }

                } catch (Exception e) {
                    log.error("V2.0结算数据上报失败:{}", e.getMessage());
                }
            }catch (Exception e){
                log.error("远程调用发送充电订单信息接口失败:{}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("充电桩{}：端口{},发送充电订单信息失败", chargingPile.getPileId(), deviceId, e);
            throw new RuntimeException("发送充电订单信息失败");
        } finally {
            chargeInfoMap.remove(deviceId);
            chargingOrderMap.remove(deviceId);
            ChargingPort chargingPort = portMap.get(deviceId);
            if (chargingPort != null) {
                chargingPort.setState("N");
                chargingPort.setGunStatus(0L);
            }
        }
    }

    /**
     * 获取时间对应的半小时 slot 索引（0~47）
     * 例如 00:00→0, 00:30→1, 01:00→2, ..., 23:30→47
     */
    private int getSlotIndex(Date date) {
        int hour = DateUtil.hour(date, true);
        int minute = DateUtil.minute(date);
        return hour * 2 + (minute >= 30 ? 1 : 0);
    }

    public synchronized void startSendRealTime(String deviceId) throws BaseException {
        String state = portMap.get(deviceId).getState();
        if("N".equals(state)){
            portMap.get(deviceId).setState("Y");
            new Thread(() -> {
                for (; ; ) {
                    try {
                        if (deviceId == null) {
                            break;
                        }
                        ChargingPort chargingPort = portMap.get(deviceId);
                        if(chargingPort == null){
                            log.warn("充电桩{}不存在端口{},无法发送心跳",chargingPile.getPileId(),deviceId);
                            break;
                        }
                        log.info("循环-端口实时状态数据心跳----{}", deviceId);
                        //充端口实时状态数据给服务器
                        sendRealTimeData(true,deviceId);
                        Thread.sleep(5000);
                        if("N".equals(chargingPort.getState()) || chargingPort.getGunStatus() == 0L){
                            log.info("充电桩{}端口{}已断开,停止发送心跳",chargingPile.getPileId(),deviceId);
                            break;
                        }
                    } catch (Exception e) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        log.error("{}充电桩({}),发送心跳失败:{}", chargingPile.getName(),chargingPile.getPileId(),e.getMessage());
//                    throw new RuntimeException("端口发送心跳失败");
                    }
                }
            }).start();
        }
    }

    @Override
    public void startHeart() throws BaseException {
        new Thread(() -> {
            for (; ; ) {
                try {
                    //发送一般心跳
                    if (chargingPile == null) {
                        break;
                    }
                    log.info("循环-充电桩心跳----{}", chargingPile.getPileId());
                    try {
                        //充电桩发送心跳给服务器
                        remoteChargingService.saveHeartBeat(chargingPile.getPileId());
                    } catch (Exception e) {
                        log.error("远程调用发送充电桩心跳失败:{}", e.getMessage());
                    }
                    Thread.sleep(10000);
                    if (chargingPile != null && chargingPile.getRunningStatus() == 1) {
                        log.info("充电桩{}已断开，停止发送心跳",chargingPile.getPileId());
                        break;
                    }
                } catch (Exception e) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    log.error("{}充电桩({}),发送心跳失败:{}", chargingPile.getName(),chargingPile.getPileId(),e.getMessage());
//                    throw new RuntimeException("充电桩发送心跳失败");
                }
            }
        }).start();
    }

    public boolean portIsExist(String deviceId){
        return portMap.get(deviceId) != null;
    }

    public boolean portIsUse(String deviceId){
        return "Y".equals(portMap.get(deviceId).getState());
    }

    public boolean portIsCharging(String deviceId){
        return portMap.get(deviceId).getGunStatus() == 1;
    }

    public Long getPortIdByDeviceId(String deviceId){
        return portMap.get(deviceId).getPortId();
    }

    /**
     * 模拟发送设备故障报告 (V2.0 0x50帧)
     */
    public void sendFaultReport(String deviceId, Integer faultType, Integer faultCode) {
        try {
            FaultReportDTO dto = new FaultReportDTO();
            dto.setPileId(chargingPile.getPileId());
            ChargingPort port = portMap.get(deviceId);
            dto.setPortId(port != null ? port.getPortId() : null);
            dto.setFaultType(faultType);
            dto.setFaultCode(faultCode);
            dto.setFaultTime(DateUtil.now());
            remoteChargingService.faultReport(dto);
            log.info("【模拟桩{}】发送故障报告 type={} code={}", chargingPile.getPileId(), faultType, faultCode);
        } catch (Exception e) {
            log.error("发送故障报告失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送故障复位 (V2.0 0x4B帧)
     */
    public void sendFaultReset(String deviceId, Integer faultCode) {
        try {
            FaultResetDTO dto = new FaultResetDTO();
            dto.setPileId(chargingPile.getPileId());
            ChargingPort port = portMap.get(deviceId);
            dto.setPortId(port != null ? port.getPortId() : null);
            dto.setFaultCode(faultCode);
            dto.setResetTime(DateUtil.now());
            remoteChargingService.faultReset(dto);
            log.info("【模拟桩{}】发送故障复位 code={}", chargingPile.getPileId(), faultCode);
        } catch (Exception e) {
            log.error("发送故障复位失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送启动完成报告 (V2.0 0x4F帧)
     */
    public void sendStartupComplete(String deviceId, String orderId) {
        try {
            StartupCompleteDTO dto = new StartupCompleteDTO();
            dto.setPileId(chargingPile.getPileId());
            ChargingPort port = portMap.get(deviceId);
            dto.setPortId(port != null ? port.getPortId() : null);
            dto.setOrderId(orderId);
            dto.setStartupResult(0);
            dto.setFailCode(0);
            dto.setMeterValue(RandomUtil.randomBigDecimal(new BigDecimal("0"), new BigDecimal("10000")));
            dto.setVinCode("TEST" + RandomUtil.randomNumbers(14));
            dto.setSoc(RandomUtil.randomBigDecimal(new BigDecimal("20"), new BigDecimal("95")));
            dto.setBmsStatus(0);
            dto.setChargerStatus(0);
            remoteChargingService.startupComplete(dto);
            log.info("【模拟桩{}】发送启动完成报告 orderId={}", chargingPile.getPileId(), orderId);
        } catch (Exception e) {
            log.error("发送启动完成报告失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送VIN码鉴权 (V2.0 0xA9帧)
     */
    public void sendVinAuth(String deviceId) {
        try {
            VinAuthDTO dto = new VinAuthDTO();
            dto.setPileId(chargingPile.getPileId());
            ChargingPort port = portMap.get(deviceId);
            dto.setPortId(port != null ? port.getPortId() : null);
            dto.setVinCode("TEST" + RandomUtil.randomNumbers(14));
            remoteChargingService.vinAuth(dto);
            log.info("【模拟桩{}】发送VIN码鉴权 vin={}", chargingPile.getPileId(), dto.getVinCode());
        } catch (Exception e) {
            log.error("发送VIN码鉴权失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送功率控制应答 (V2.0 0x59帧)
     */
    public void sendPowerControl(String deviceId, Integer maxPower) {
        try {
            PowerControlDTO dto = new PowerControlDTO();
            dto.setPileId(chargingPile.getPileId());
            ChargingPort port = portMap.get(deviceId);
            dto.setPortId(port != null ? port.getPortId() : null);
            dto.setMaxPower(maxPower);
            dto.setPriority(1);
            dto.setLimitMinutes(0);
            remoteChargingService.powerControl(dto);
            log.info("【模拟桩{}】发送功率控制 maxPower={}", chargingPile.getPileId(), maxPower);
        } catch (Exception e) {
            log.error("发送功率控制失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送分时电量上报
     */
    public void sendHourlyEnergy(String orderId, Integer slotIndex) {
        try {
            HourlyEnergyDTO dto = new HourlyEnergyDTO();
            dto.setOrderId(orderId);
            dto.setSlotIndex(slotIndex);
            dto.setEnergy(RandomUtil.randomBigDecimal(new BigDecimal("0"), new BigDecimal("10")));
            remoteChargingService.hourlyEnergyReport(dto);
            log.info("【模拟桩{}】发送分时电量 orderId={} slot={}", chargingPile.getPileId(), orderId, slotIndex);
        } catch (Exception e) {
            log.error("发送分时电量失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送动态费率段同步 (V2.0 费率模型)
     */
    public void sendRateDetailSync(String deviceId) {
        try {
            RateDetailDTO dto = new RateDetailDTO();
            dto.setRateIndex(RandomUtil.randomInt(1, 48));
            dto.setElecRate(RandomUtil.randomBigDecimal(new BigDecimal("0.3"), new BigDecimal("1.5")));
            dto.setServiceRate(RandomUtil.randomBigDecimal(new BigDecimal("0.1"), new BigDecimal("0.8")));
            dto.setStartTime("00:00:00");
            dto.setEndTime("23:59:59");
            java.util.List<RateDetailDTO> dtoList = java.util.Collections.singletonList(dto);
            remoteChargingService.rateDetailSync(dtoList);
            log.info("【模拟桩{}】发送动态费率段同步", chargingPile.getPileId());
        } catch (Exception e) {
            log.error("发送动态费率段同步失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟发送订单费率明细上报 (V2.0 0x3D帧)
     */
    public void sendOrderRateDetail(String orderId) {
        try {
            OrderRateDetailDTO dto = new OrderRateDetailDTO();
            dto.setOrderId(orderId);
            dto.setRateIndex(RandomUtil.randomInt(1, 4));
            dto.setRatePrice(RandomUtil.randomBigDecimal(new BigDecimal("0.3"), new BigDecimal("1.5")));
            dto.setEnergy(RandomUtil.randomBigDecimal(new BigDecimal("0.1"), new BigDecimal("20")));
            dto.setLossEnergy(RandomUtil.randomBigDecimal(new BigDecimal("0"), new BigDecimal("0.5")));
            dto.setAmount(RandomUtil.randomBigDecimal(new BigDecimal("1"), new BigDecimal("30")));
            remoteChargingService.orderRateDetail(dto);
            log.info("【模拟桩{}】发送订单费率明细 orderId={}", chargingPile.getPileId(), orderId);
        } catch (Exception e) {
            log.error("发送订单费率明细失败: {}", e.getMessage());
        }
    }
}
