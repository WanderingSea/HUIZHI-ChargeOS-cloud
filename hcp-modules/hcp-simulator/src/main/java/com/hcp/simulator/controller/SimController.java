package com.hcp.simulator.controller;

import com.hcp.common.core.domain.R;
import com.hcp.common.core.exception.base.BaseException;
import com.hcp.simulator.common.SimCenter;
import com.hcp.simulator.dto.ChargingOrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/evcs/sim/v1")
@Tag(name = "模拟充电桩")
public class SimController {

    @Autowired
    private SimCenter simCenter;

    @Operation(summary = "启动模拟充电桩")
    @GetMapping("/start")
    public R<String> start(@Parameter(description = "充电桩ID") String pileId) {
        try {
            log.info("sim-api-start");
            if (StringUtils.isBlank(pileId)) {
                return R.fail("充电桩编号无效");
            }
            log.info("sim-api-start-check");
            if (simCenter.isOnline(pileId)) {
                return R.fail("充电桩已启动，请勿重复启动");
            }
            log.info("sim-api-start-run");
            simCenter.start(pileId);
        } catch (BaseException ex) {
            log.error("模拟桩启动失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
        return R.ok("启动成功");
    }

    @Operation(summary = "停止模拟充电桩")
    @GetMapping("/stop")
    public R<String> stop(@Parameter(description = "充电桩ID") String pileId) {
        if (StringUtils.isBlank(pileId)) {
            return R.fail("充电桩编号无效");
        }
        try {
            simCenter.stop(pileId);
            return R.ok("停止成功");
        } catch (BaseException ex) {
            log.error("模拟桩停止失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "插枪")
    @GetMapping("/link")
    public R<String> link(@Parameter(description = "充电桩ID") String pileId,@Parameter(description = "端口-设备ID") String deviceId) {
        if (StringUtils.isBlank(pileId) || StringUtils.isBlank(deviceId)) {
            return R.fail("充电桩编号或设备编号无效");
        }
        try {
            simCenter.link(pileId, deviceId);
            return R.ok("插枪成功");
        } catch (BaseException ex) {
            log.error("模拟桩插枪失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "拔枪")
    @GetMapping("/unlink")
    public R<String> unLink(@Parameter(description = "充电桩ID") String pileId,@Parameter(description = "端口-设备ID") String deviceId) {
        if (StringUtils.isBlank(pileId) || StringUtils.isBlank(deviceId)) {
            return R.fail("充电桩编号或设备编号无效");
        }
        try {
            simCenter.unlink(pileId,deviceId);
            return R.ok("拔枪成功");
        } catch (BaseException ex) {
            log.error("模拟桩拔枪失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "开始充电")
    @PostMapping("/startCharge")
    public R<String> startCharge(@RequestBody ChargingOrderDTO chargingOrder) {
        if(chargingOrder == null){
            return R.fail("chargingOrder无效");
        }
        if (StringUtils.isBlank(chargingOrder.getPileId()) || StringUtils.isBlank(chargingOrder.getDeviceId())) {
            return R.fail("充电桩编号或设备编号无效");
        }
        try {
            simCenter.startCharge(chargingOrder);
            return R.ok("开始充电成功");
        } catch (BaseException e) {
            log.error("开始充电失败", e);
            return R.fail(e.getDefaultMessage());
        }
    }

    @Operation(summary = "停止充电")
    @GetMapping("/endCharge")
    public R<String> endCharge(@Parameter(description = "充电桩编号") String pileId,@Parameter(description = "设备-端口ID") String deviceId) {
        if (StringUtils.isBlank(pileId) || deviceId == null) {
            return R.fail("充电桩编号或设备编号无效");
        }
        try {
            simCenter.stopCharge(pileId,deviceId);
            return R.ok("停止充电成功");
        } catch (BaseException ex) {
            log.error("停止充电失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }


    @Operation(summary = "模拟故障上报(V2.0)")
    @GetMapping("/faultReport")
    public R<String> faultReport(@Parameter(description = "充电桩ID") String pileId,
                                  @Parameter(description = "设备-端口ID") String deviceId,
                                  @Parameter(description = "故障类型") Integer faultType,
                                  @Parameter(description = "故障编码") Integer faultCode) {
        try {
            simCenter.sendFaultReport(pileId, deviceId, faultType, faultCode);
            return R.ok("故障上报模拟成功");
        } catch (BaseException ex) {
            log.error("故障上报模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟故障复位(V2.0)")
    @GetMapping("/faultReset")
    public R<String> faultReset(@Parameter(description = "充电桩ID") String pileId,
                                 @Parameter(description = "设备-端口ID") String deviceId,
                                 @Parameter(description = "故障编码") Integer faultCode) {
        try {
            simCenter.sendFaultReset(pileId, deviceId, faultCode);
            return R.ok("故障复位模拟成功");
        } catch (BaseException ex) {
            log.error("故障复位模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟启动完成报告(V2.0)")
    @GetMapping("/startupComplete")
    public R<String> startupComplete(@Parameter(description = "充电桩ID") String pileId,
                                      @Parameter(description = "设备-端口ID") String deviceId,
                                      @Parameter(description = "订单ID") String orderId) {
        try {
            simCenter.sendStartupComplete(pileId, deviceId, orderId);
            return R.ok("启动完成报告模拟成功");
        } catch (BaseException ex) {
            log.error("启动完成报告模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟VIN码鉴权(V2.0)")
    @GetMapping("/vinAuth")
    public R<String> vinAuth(@Parameter(description = "充电桩ID") String pileId,
                              @Parameter(description = "设备-端口ID") String deviceId) {
        try {
            simCenter.sendVinAuth(pileId, deviceId);
            return R.ok("VIN码鉴权模拟成功");
        } catch (BaseException ex) {
            log.error("VIN码鉴权模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟功率控制(V2.0)")
    @GetMapping("/powerControl")
    public R<String> powerControl(@Parameter(description = "充电桩ID") String pileId,
                                   @Parameter(description = "设备-端口ID") String deviceId,
                                   @Parameter(description = "最大功率") Integer maxPower) {
        try {
            simCenter.sendPowerControl(pileId, deviceId, maxPower);
            return R.ok("功率控制模拟成功");
        } catch (BaseException ex) {
            log.error("功率控制模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟分时电量上报(V2.0)")
    @GetMapping("/hourlyEnergy")
    public R<String> hourlyEnergy(@Parameter(description = "充电桩ID") String pileId,
                                   @Parameter(description = "设备-端口ID") String deviceId,
                                   @Parameter(description = "订单ID") String orderId,
                                   @Parameter(description = "时段序号(0~47)") Integer slotIndex) {
        try {
            simCenter.sendHourlyEnergy(pileId, deviceId, orderId, slotIndex);
            return R.ok("分时电量上报模拟成功");
        } catch (BaseException ex) {
            log.error("分时电量上报模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    // ========== V2.0 测试触发端点 ==========

    @Operation(summary = "模拟动态费率段同步(V2.0)")
    @GetMapping("/rateDetailSync")
    public R<String> rateDetailSync(@Parameter(description = "充电桩ID") String pileId,
                                     @Parameter(description = "设备-端口ID") String deviceId) {
        try {
            simCenter.sendRateDetailSync(pileId, deviceId);
            return R.ok("费率段同步模拟成功");
        } catch (BaseException ex) {
            log.error("费率段同步模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }

    @Operation(summary = "模拟订单费率明细上报(V2.0)")
    @GetMapping("/orderRateDetail")
    public R<String> orderRateDetail(@Parameter(description = "充电桩ID") String pileId,
                                      @Parameter(description = "订单ID") String orderId) {
        try {
            simCenter.sendOrderRateDetail(pileId, orderId);
            return R.ok("订单费率明细上报模拟成功");
        } catch (BaseException ex) {
            log.error("订单费率明细上报模拟失败", ex);
            return R.fail(ex.getDefaultMessage());
        }
    }



}
