package com.hcp.operator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.web.controller.BaseController;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.operator.domain.ChargeOrderHourlyEnergy;
import com.hcp.operator.domain.ChargeOrderRateDetail;
import com.hcp.operator.domain.RateDetail;
import com.hcp.operator.service.*;
import com.hcp.system.api.domain.Bo.FeeRangeTime;
import com.hcp.system.api.domain.ChargingOrder;
import com.hcp.system.api.domain.ChargingPile;
import com.hcp.system.api.domain.ChargingPort;
import com.hcp.system.api.domain.Heartbeat;
import com.hcp.system.api.domain.dto.*;
import com.hcp.system.api.domain.vo.ChargingPileVO;
import com.hcp.system.api.domain.vo.PlotDetailVo;
import com.hcp.system.api.domain.vo.PlotInfoReqVO;
import com.hcp.system.api.domain.vo.PlotVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Rate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/charge")
@Slf4j
@RequiredArgsConstructor
public class ChargeController extends BaseController {
    @Autowired
    private IHeartbeatService heartbeatService;
    @Autowired
    private IChargingPileService chargingPileService;
    @Autowired
    private ICustomPriceService customPriceService;
    @Autowired
    private IChargingPortService chargingPortService;
    @Autowired
    private IChargingOrderService chargingOrderService;
    @Autowired
    private IDeviceFaultService deviceFaultService;
    @Autowired
    private IChargeStartupLogService chargeStartupLogService;
    @Autowired
    private IVinAuthLogService vinAuthLogService;
    @Autowired
    private IPowerControlLogService powerControlLogService;

    private final IRateDetailService rateDetailService;
    private final IChargeOrderRateDetailService chargeOrderRateDetailService;
    private final IChargeOrderHourlyEnergyService chargeOrderHourlyEnergyService;

    //处理设备心跳
    @GetMapping("/heartBeat")
    R<String> saveHeartBeat(@RequestParam("pileId") String pileId){
        assert pileId != null;
        ChargingPile chargingPile = chargingPileService.getById(pileId);
        if(chargingPile == null){
            return R.fail("桩不存在");
        }
        Heartbeat heartbeat = Heartbeat.builder()
                .pileId(pileId).chargestate("0").deviceId("").sourcemsg("")
                .build();
        heartbeat.setTenantId(chargingPile.getTenantId());
        heartbeatService.updateHeartbeat(heartbeat);
        return R.ok();
    }

    /**
     * 注册设备，查询设备是否在平台上
     *
     * @param pileId 桩Id
     * @return 结果
     */
    @GetMapping("/checkPile")
    R<ChargingPile> checkPile(@RequestParam("pileId") String pileId){
        return R.ok(chargingPileService.getById(pileId));
    }

    /**
     * 查询计费规则
     *
     * @param pileId 设备Id
     * @return 计费规则
     */
    @GetMapping("/feeInfo")
    R<List<FeeRangeTime>> getFeeInfo(@RequestParam("pileId") String pileId){
        assert pileId != null;
        return R.ok(customPriceService.getPriceByPileId(pileId));
    }

    /**
     * 插枪状态更新
     *
     * @param pileId 桩Id
     * @param port   接口号
     * @param gunInsert   1插枪0拔枪
     * @return 结果
     */
    @GetMapping("/gunInsert")
    R<String> gunInsert(@RequestParam("pileId") String pileId, @RequestParam("port") String port,
                        @RequestParam("gunInsert") String gunInsert){
        chargingPortService.updateGunStatus(pileId,port,gunInsert);
        return R.ok();
    }

    /**
     * 开始充电
     *
     * @param pileId 桩编号
     * @param port   接口号
     * @param amount 充电金额
     * @return 结果
     */
    @GetMapping("/startCharge")
    R<ChargingOrder> startCharge(@RequestParam("pileId") String pileId, @RequestParam("port") String port,
                                 @RequestParam("userId")Long userId,@RequestParam("amount") BigDecimal amount,
                                 @RequestParam("hour")Integer hour){
        ChargingOrder order = chargingOrderService.startChargeOrder(pileId,port,userId,amount,hour);
        return R.ok(order);
    }

    /**
     * 结束充电
     *
     * @param pileId 桩编号
     * @param port   接口号
     * @return 结果
     */
    @GetMapping("/stopCharge")
    R<String> stopCharge(@RequestParam("pileId") String pileId, @RequestParam("port") String port){
        chargingOrderService.stopChargingOrder(pileId,port);
        return R.ok();
    }

    /**
     * 订单信息
     *
     * @param orderId     订单编号
     * @param totalPower  总耗电量
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param electricFee 电费
     * @param serviceFee  服务费
     * @param totalAmount 总费用
     * @param stopReason  结束原因
     * @return 结果
     */
    @GetMapping("/orderInfo")
    R<String> orderInfo(@RequestParam("orderId") String orderId, @RequestParam("totalPower") Double totalPower,
                        @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime,
                        @RequestParam("electricFee") BigDecimal electricFee, @RequestParam("serviceFee") BigDecimal serviceFee,
                        @RequestParam("totalAmount") BigDecimal totalAmount, @RequestParam("stopReason") String stopReason){
        chargingOrderService.handleOrderInfo(orderId,totalPower,totalAmount,electricFee,serviceFee,startTime,endTime,stopReason);
        return R.ok();
    }

    /**
     * 启动结果
     *
     * @param pileId      桩编号
     * @param port        接口号
     * @param orderId     订单号
     * @param startResult 启动结果
     * @param failReason  失败原因
     * @return 结果
     */
    @GetMapping("/startResult")
    R<String> startResult(@RequestParam("pileId") String pileId, @RequestParam("port") String port,
                          @RequestParam("orderId") String orderId, @RequestParam("startResult") String startResult,
                          @RequestParam("failReason") String failReason){
        chargingOrderService.updateOrderStart(orderId,pileId,port,startResult,failReason);
        return R.ok();
    }

    /**
     * 停止充电结果
     *
     * @param pileId     桩编号
     * @param port       接口号
     * @param stopResult 停止结果
     * @param failReason 失败原因
     * @return 停止结果
     */
    @GetMapping("/stopResult")
    R<String> stopResult(@RequestParam("pileId") String pileId, @RequestParam("port") String port,
                         @RequestParam("stopResult") String stopResult, @RequestParam("failReason") String failReason){
        chargingOrderService.updateOrderStop(pileId,port,stopResult,failReason);
        return R.ok();
    }

    /**
     * 充电中心跳
     * @param pileId 桩编号
     * @param port 接口号
     * @param orderId 订单号
     * @param status 接口状态
     * @param voltage 电压
     * @param electric 电流
     * @param soc soc
     * @param chargePower 已充电量
     * @param chargePrice 已充金额
     * @return 结果
     */
    @GetMapping("/chargingHeartBeat")
    R<String> chargingHeartBeat(@RequestParam("pileId") String pileId, @RequestParam("port") String port,
                                @RequestParam("orderId") String orderId, @RequestParam("status") String status,
                                @RequestParam("voltage") Double voltage, @RequestParam("electric") Double electric,
                                @RequestParam("soc") String soc, @RequestParam("chargePower") Double chargePower,
                                @RequestParam("chargeFee") BigDecimal chargeFee, @RequestParam("serviceFee") BigDecimal serviceFee,
                                @RequestParam("chargePrice") BigDecimal chargePrice){
        chargingOrderService.handleChargingHeartBeat(orderId,voltage,electric,soc,chargePower,chargeFee,serviceFee,chargePrice);
        return R.ok();
    }
    @PostMapping("/getPlotInfo")
    @ApiOperation("查询充电桩列表")
    public R<List<PlotVO>> getPlotInfo(@RequestBody PlotInfoReqVO plotInfoReqVO) {

        List<PlotVO> plotInfo = chargingPileService.getPlotInfo(plotInfoReqVO);

        return R.ok(plotInfo);
    }

    @GetMapping("/queryChargingPileData")
    @ApiOperation("查询充电桩列表")
    public R<ChargingPileVO> queryChargingPileData(@RequestParam("pileId") String pileId) {
        ChargingPileVO chargingPileVo = chargingPileService.queryChargingPileData(pileId);
        if (ObjectUtil.isNotEmpty(chargingPileVo)){
            List<ChargingPort> portData = chargingPortService.selectPortByPileId(pileId);
            chargingPileVo.setList(portData);
        }

        return R.ok(chargingPileVo);
    }

    @GetMapping("/plotDetail")
    public R<PlotDetailVo> plotDetail(  @RequestParam("plotId") String plotId,
                                              @RequestParam("deviceType") String deviceType) {
        PlotDetailVo plotDetailVo = chargingPileService.plotDetail(plotId,deviceType);
        return R.ok(plotDetailVo);
    }

    @PostMapping("/getPlotInfoPage")
    @ApiOperation("分页查询充电桩列表")
    R<Page<PlotVO>> getPlotInfoPage(@RequestBody PlotInfoReqVO plotInfoReqVO) {

        Page<PlotVO> plotInfo = chargingPileService.getPlotInfoPage(plotInfoReqVO);

        return R.ok(plotInfo);
    }


    /**
     * 设备故障上报 (V2.0 0x50帧)
     */
    @PostMapping("/faultReport")
    R<String> faultReport(@RequestBody FaultReportDTO dto) {
        deviceFaultService.reportFault(dto);
        return R.ok();
    }

    /**
     * 设备故障复位上报 (V2.0 0x4B帧)
     */
    @PostMapping("/faultReset")
    R<String> faultReset(@RequestBody FaultResetDTO dto) {
        deviceFaultService.resetFault(dto);
        return R.ok();
    }

    /**
     * 充电机启动完成报告 (V2.0 0x4F帧)
     */
    @PostMapping("/startupComplete")
    R<String> startupComplete(@RequestBody StartupCompleteDTO dto) {
        chargeStartupLogService.saveStartupComplete(dto);
        return R.ok();
    }

    /**
     * VIN码鉴权上报 (V2.0 0xA9/0xAA帧)
     */
    @PostMapping("/vinAuth")
    R<String> vinAuth(@RequestBody VinAuthDTO dto) {
        vinAuthLogService.saveVinAuth(dto);
        return R.ok();
    }

    /**
     * 功率控制日志上报 (V2.0 0x60/0x59帧)
     */
    @PostMapping("/powerControl")
    R<String> powerControl(@RequestBody PowerControlDTO dto) {
        powerControlLogService.savePowerControl(dto);
        return R.ok();
    }

    /**
     * 费率段同步（V2.0）<br>
     * 桩端/管理端上报该桩的费率模板，写入c_rate_detail表
     */
    @PostMapping("/rateDetailSync")
    @ApiOperation("接收桩同步的费率模板")
    public R<String> rateDetailSync(@RequestParam("priceId") Long priceId,
                                    @RequestBody List<RateDetailDTO> dtoList) {
        // 参数校验
        if (priceId == null) {
            return R.fail("定价规则ID不能为空");
        }
        // 参数校验
        if (CollUtil.isEmpty(dtoList)) {
            return R.fail("费率数据不能为空");
        }
        // DTO转实体类
        List<RateDetail> details = BeanUtil.copyToList(dtoList, RateDetail.class);

        // 设置priceId到每个实体（确保数据完整性）
        details.forEach(detail -> detail.setPriceId(priceId));

        rateDetailService.saveRateDetails(priceId,details);
        return R.ok("费率同步成功");
    }

    /**
     * 订单费率明细上报（V2.0）<br>
     * 充电结束后，接收桩端上送的订单费率明细
     */
    @PostMapping("/orderRateDetail")
    @ApiOperation("接收订单费率明细")
    public R<String> orderRateDetail(@RequestBody OrderRateDetailDTO dto) {
        ChargeOrderRateDetail detail = new ChargeOrderRateDetail();
        BeanUtil.copyProperties(dto, detail);
        chargeOrderRateDetailService.saveOrderRateDetail(detail);
        return R.ok("订单费率明细上报成功");
    }

    /**
     * 分时电量上报（V2.0）<br>
     * 充电结束后，接收桩端上送的分时电量数据
     */
    @PostMapping("/hourlyEnergy")
    @ApiOperation("接收订单分时电量数据")
    R<String> hourlyEnergy(@RequestBody HourlyEnergyDTO dto,
                           @RequestParam("orderId") String orderId){
        ChargeOrderHourlyEnergy energy = new ChargeOrderHourlyEnergy();
        BeanUtil.copyProperties(dto, energy);
        energy.setOrderId(orderId);
        chargeOrderHourlyEnergyService.saveHourlyEnergy(energy);
        return R.ok("订单分时电量数据上报成功");
    }

    /**
     * V2.0 订单结算
     */
    @PostMapping("/V2OrderSettlement")
    @ApiOperation("V2.0 订单结算")
    R<String> v2OrderSettlement(@RequestParam("orderId") String orderId,
                                @RequestParam("totalPower") Double totalPower,
                                @RequestParam("startTime") String startTime,
                                @RequestParam("endTime") String endTime,
                                @RequestParam("electricFee") BigDecimal electricFee,
                                @RequestParam("serviceFee") BigDecimal serviceFee,
                                @RequestParam("totalAmount") BigDecimal totalAmount,
                                @RequestParam("stopReason") String stopReason,
                                @RequestParam(name = "meterNumber", required = false) String meterNumber,
                                @RequestParam(name = "meterCipher", required = false) String meterCipher,
                                @RequestParam(name = "meterStartValue", required = false) BigDecimal meterStartValue,
                                @RequestParam(name = "meterEndValue", required = false) BigDecimal meterEndValue,
                                @RequestParam(name = "vinCode", required = false) String vinCode,
                                @RequestParam(name = "lossTotalPower", required = false) BigDecimal lossTotalPower,
                                @RequestParam(name = "tradeType", required = false) Integer tradeType) {
        chargingOrderService.handleV2OrderSettlement(orderId, totalPower, totalAmount,
                electricFee, serviceFee, startTime, endTime, stopReason,
                meterNumber, meterCipher, meterStartValue, meterEndValue,
                vinCode, lossTotalPower, tradeType);
        return R.ok("订单结算成功");
    }

}
