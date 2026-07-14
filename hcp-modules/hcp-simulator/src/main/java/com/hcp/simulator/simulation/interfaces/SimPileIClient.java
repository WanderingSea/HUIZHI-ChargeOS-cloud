package com.hcp.simulator.simulation.interfaces;


import com.hcp.common.core.exception.base.BaseException;
import com.hcp.simulator.dto.ChargingOrderDTO;

public interface SimPileIClient {

    /**
     * 充电桩启动
     */
    void start() throws BaseException;

    /**
     * 充电桩停止（断电）
     */
    void stop() throws BaseException;

    /**
     * 充电桩-车插枪
     */
    Long link(String deviceId) throws BaseException;

    /**
     * 充电桩-车拔枪
     */
    Long unlink(String deviceId) throws BaseException;

    /**
     * 启动充电
     */
    Long startCharge(ChargingOrderDTO chargingOrder) throws BaseException;

    /**
     * 停止充电
     */
    Long stopCharge(String deviceId) throws BaseException;

    void sendRealTimeData(Boolean sendForce,String deviceId) throws BaseException;

    void sendTradeInfo(String deviceId,String stopReason) throws BaseException;

    void startHeart() throws BaseException;

    /**
     * 判断充电枪是否存在
     */
    boolean portIsExist(String deviceId);

    /**
     * 判断充电枪是否正在使用
     */
    boolean portIsUse(String deviceId);

    /**
     * 判断充电枪是否正在充电
     */
    boolean portIsCharging(String deviceId);

    /**
     * 根据设备ID获取端口Id
     */
    Long getPortIdByDeviceId(String deviceId);


    /** (V2.0 0x50帧) 模拟设备故障报告 */
    void sendFaultReport(String deviceId, Integer faultType, Integer faultCode) throws BaseException;

    /** (V2.0 0x4B帧) 模拟故障复位 */
    void sendFaultReset(String deviceId, Integer faultCode) throws BaseException;

    /** (V2.0 0x4F帧) 模拟启动完成报告 */
    void sendStartupComplete(String deviceId, String orderId) throws BaseException;

    /** (V2.0 0xA9帧) 模拟VIN码鉴权 */
    void sendVinAuth(String deviceId) throws BaseException;

    /** (V2.0 0x59帧) 模拟功率控制应答 */
    void sendPowerControl(String deviceId, Integer maxPower) throws BaseException;

    /** (V2.0 分时电量) 模拟分时电量上报 */
    void sendHourlyEnergy(String orderId, Integer slotIndex) throws BaseException;

    /** (V2.0 费率模型) 模拟动态费率段同步 */
    void sendRateDetailSync(String deviceId) throws BaseException;

    /** (V2.0 0x3D帧) 模拟订单费率明细上报 */
    void sendOrderRateDetail(String orderId) throws BaseException;
}
