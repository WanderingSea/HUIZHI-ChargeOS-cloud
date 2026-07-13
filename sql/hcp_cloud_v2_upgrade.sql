-- ============================================================================
-- HUIZHI-ChargeOS-cloud  云快充协议 V1.6 → V2.0  数据库升级迁移脚本
-- 版本: v2.0
-- 日期: 2026-07-13
-- 执行前请备份数据库！
-- 修正记录:
--   v2.0: SMALLINT→SMALLINT UNSIGNED, DECIMAL→TIME, api_version UPDATE, 字符集, 复合索引, 故障字典PDF提取
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 第一部分：修改现有表 (3张)
-- 执行顺序：api_version MODIFY → UPDATE → ADD COLUMN → 其他表
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. c_charging_pile — 充电桩表 (MODIFY + UPDATE + ADD)
-- ----------------------------------------------------------------------------
ALTER TABLE `c_charging_pile`
  MODIFY COLUMN `api_version` VARCHAR(20) DEFAULT '1.6' COMMENT '协议版本：1.6云快充/2.0云快充/2.0唐总';

UPDATE `c_charging_pile` SET `api_version` = '1.6' WHERE `api_version` = '1';
UPDATE `c_charging_pile` SET `api_version` = '2.0' WHERE `api_version` = '2';

ALTER TABLE `c_charging_pile`
  ADD COLUMN `token`              VARCHAR(14) DEFAULT NULL COMMENT '登录令牌(7字节BCD→14字符hex)',
  ADD COLUMN `phone_number`       VARCHAR(11) DEFAULT NULL COMMENT '设备SIM卡手机号(11字节ASCII)',
  ADD COLUMN `supported_network`  TINYINT     DEFAULT NULL COMMENT '支持网络制式(bit位:2G/3G/4G/5G)',
  ADD COLUMN `current_network`    TINYINT     DEFAULT NULL COMMENT '当前网络制式(bit位)';

-- ----------------------------------------------------------------------------
-- 2. c_charging_port — 充电端口表 (MODIFY + ADD)
-- ----------------------------------------------------------------------------
ALTER TABLE `c_charging_port`
  MODIFY COLUMN `fault_code`   SMALLINT UNSIGNED DEFAULT NULL COMMENT '故障码(0x0001~0xFFFF)',
  ADD    COLUMN `smoke_status` TINYINT           DEFAULT NULL COMMENT '烟感状态 1=启动/2=空闲/3=报警/4=故障',
  ADD    COLUMN `meter_value`  DECIMAL(18,4)     DEFAULT NULL COMMENT '电表示值(精确到小数点后4位)';

-- ----------------------------------------------------------------------------
-- 3. c_charging_order — 充电订单表 (ADD only)
-- ----------------------------------------------------------------------------
ALTER TABLE `c_charging_order`
  ADD COLUMN `meter_number`      VARCHAR(12)   DEFAULT NULL COMMENT '电表表号(6字节BCD)',
  ADD COLUMN `meter_cipher`      VARCHAR(68)   DEFAULT NULL COMMENT '电表密文(34字节hex)',
  ADD COLUMN `meter_start_value` DECIMAL(18,4) DEFAULT NULL COMMENT '充电开始电表读数',
  ADD COLUMN `meter_end_value`   DECIMAL(18,4) DEFAULT NULL COMMENT '充电结束电表读数',
  ADD COLUMN `vin_code`          VARCHAR(17)   DEFAULT NULL COMMENT '电动汽车VIN码(正序，无需反序)',
  ADD COLUMN `loss_total_power`  DECIMAL(18,4) DEFAULT NULL COMMENT '计损总电量',
  ADD COLUMN `trade_type`        TINYINT       DEFAULT NULL COMMENT '交易标识(桩端上报)：1=app/2=卡/3=离线卡/5=VIN码启动';


-- ============================================================================
-- 第二部分：新建表 (8张)
-- 所有新表统一 CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 4. c_device_fault — 设备故障记录表 (0x50帧)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_device_fault` (
  `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
  `pile_id`     VARCHAR(64)           NOT NULL    COMMENT '充电桩编号',
  `port_id`     BIGINT                DEFAULT NULL COMMENT '充电口ID',
  `fault_type`  TINYINT               NOT NULL    COMMENT '故障大类：1=车故障/2=车桩交互/3=桩平台/4=桩故障/5=自定义',
  `fault_code`  SMALLINT UNSIGNED     NOT NULL    COMMENT '故障编码(0x0001~0xFFFF)',
  `fault_time`  DATETIME              NOT NULL    COMMENT '故障发生时间(CP56Time2a转换)',
  `reset_time`  DATETIME              DEFAULT NULL COMMENT '故障复位时间',
  `status`      TINYINT               DEFAULT 0   COMMENT '处理状态：0=未处理/1=已复位/2=平台已确认',
  `tenant_id`   BIGINT                DEFAULT NULL COMMENT '租户编号',
  `create_time` DATETIME              DEFAULT NULL,
  `update_time` DATETIME              DEFAULT NULL,
  INDEX `idx_pile_id`   (`pile_id`),
  INDEX `idx_fault_time` (`fault_time`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='设备故障记录表';

-- ----------------------------------------------------------------------------
-- 5. c_fault_code_dict — 故障编码字典表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_fault_code_dict` (
  `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
  `fault_type`  TINYINT               NOT NULL    COMMENT '故障大类(1~5)',
  `fault_code`  SMALLINT UNSIGNED     NOT NULL    COMMENT '故障编码(0x0001~0xFFFF)',
  `fault_name`  VARCHAR(100)          NOT NULL    COMMENT '故障名称',
  `fault_desc`  VARCHAR(500)          DEFAULT NULL COMMENT '故障描述',
  UNIQUE INDEX `uk_type_code` (`fault_type`, `fault_code`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='故障编码字典表(V2.0附录13.2)';

-- ----------------------------------------------------------------------------
-- 6. c_charge_startup_log — 启动完成日志表 (0x4F帧)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_charge_startup_log` (
  `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
  `pile_id`         VARCHAR(64)         NOT NULL    COMMENT '充电桩编号',
  `port_id`         BIGINT              DEFAULT NULL COMMENT '充电口ID',
  `order_id`        VARCHAR(64)         DEFAULT NULL COMMENT '关联订单号',
  `startup_result`  TINYINT             NOT NULL    COMMENT '启动结果：0=成功/1=失败',
  `fail_code`       SMALLINT UNSIGNED   DEFAULT NULL COMMENT '失败原因(故障编码)',
  `meter_value`     DECIMAL(18,4)       DEFAULT NULL COMMENT '当前电表总值',
  `vin_code`        VARCHAR(17)         DEFAULT NULL COMMENT 'VIN码',
  `soc`             DECIMAL(5,2)        DEFAULT NULL COMMENT '电池SOC(%)',
  `bms_status`      TINYINT             DEFAULT NULL COMMENT 'BMS充电准备状态',
  `charger_status`  TINYINT             DEFAULT NULL COMMENT '充电机准备状态',
  `tenant_id`       BIGINT              DEFAULT NULL COMMENT '租户编号',
  `create_time`     DATETIME            DEFAULT NULL,
  INDEX `idx_pile_port` (`pile_id`, `port_id`),
  INDEX `idx_order_id`  (`order_id`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='充电启动完成日志表(对应0x4F帧)';

-- ----------------------------------------------------------------------------
-- 7. c_vin_auth_log — VIN码鉴权日志表 (0xA9/0xAA帧)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_vin_auth_log` (
  `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
  `pile_id`     VARCHAR(64)   NOT NULL    COMMENT '充电桩编号',
  `port_id`     BIGINT        DEFAULT NULL COMMENT '充电口ID',
  `vin_code`    VARCHAR(17)   NOT NULL    COMMENT 'VIN码',
  `auth_result` TINYINT       NOT NULL    COMMENT '鉴权结果：0=通过/1=拒绝',
  `order_id`    VARCHAR(64)   DEFAULT NULL COMMENT '鉴权通过后关联订单号',
  `tenant_id`   BIGINT        DEFAULT NULL COMMENT '租户编号',
  `create_time` DATETIME      DEFAULT NULL,
  INDEX `idx_pile_port` (`pile_id`, `port_id`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='VIN码鉴权日志表(对应0xA9/0xAA帧)';

-- ----------------------------------------------------------------------------
-- 8. c_power_control_log — 功率控制日志表 (0x60/0x59帧)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_power_control_log` (
  `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
  `pile_id`       VARCHAR(64)  NOT NULL    COMMENT '充电桩编号',
  `port_id`       BIGINT       DEFAULT NULL COMMENT '充电口ID',
  `max_power`     INT          NOT NULL    COMMENT '最大功率限制(kW)',
  `priority`      TINYINT      DEFAULT NULL COMMENT '优先级',
  `limit_minutes` INT          DEFAULT NULL COMMENT '限制时长(分钟)',
  `result`        TINYINT      DEFAULT NULL COMMENT '桩端应答结果',
  `tenant_id`     BIGINT       DEFAULT NULL COMMENT '租户编号',
  `create_time`   DATETIME     DEFAULT NULL,
  INDEX `idx_pile_port` (`pile_id`, `port_id`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='功率控制日志表(对应0x60/0x59帧)';

-- ----------------------------------------------------------------------------
-- 9. c_rate_detail — 动态费率段表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_rate_detail` (
  `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
  `price_id`     BIGINT          NOT NULL    COMMENT '关联c_custom_price.price_id',
  `rate_index`   TINYINT         NOT NULL    COMMENT '费率编号(1~48)',
  `elec_rate`    DECIMAL(12,4)   DEFAULT NULL COMMENT '电费单价(元/度)',
  `service_rate` DECIMAL(12,4)   DEFAULT NULL COMMENT '服务费单价(元/度)',
  `start_time`   TIME            DEFAULT NULL COMMENT '时段开始(HH:MM:SS)',
  `end_time`     TIME            DEFAULT NULL COMMENT '时段结束(HH:MM:SS)',
  `tenant_id`    BIGINT          DEFAULT NULL COMMENT '租户编号',
  `create_time`  DATETIME        DEFAULT NULL,
  INDEX `idx_price_id` (`price_id`, `rate_index`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='动态费率段表(V2.0 最多48段)';

-- ----------------------------------------------------------------------------
-- 10. c_charge_order_rate_detail — 订单费率明细表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_charge_order_rate_detail` (
  `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_id`    VARCHAR(64)     NOT NULL    COMMENT '关联c_charging_order.order_id',
  `rate_index`  TINYINT         NOT NULL    COMMENT '费率段编号',
  `rate_price`  DECIMAL(12,4)   DEFAULT NULL COMMENT '该段费率单价',
  `energy`      DECIMAL(12,4)   DEFAULT NULL COMMENT '该段充电电量',
  `loss_energy` DECIMAL(12,4)   DEFAULT NULL COMMENT '该段计损电量',
  `amount`      DECIMAL(12,4)   DEFAULT NULL COMMENT '该段金额',
  `create_time` DATETIME        DEFAULT NULL,
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='订单费率结算明细表';

-- ----------------------------------------------------------------------------
-- 11. c_charge_order_hourly_energy — 分时电量表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `c_charge_order_hourly_energy` (
  `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_id`    VARCHAR(64)     NOT NULL    COMMENT '关联订单号',
  `slot_index`  TINYINT         NOT NULL    COMMENT '时段序号(0~47，每半小时)',
  `energy`      DECIMAL(12,4)   DEFAULT NULL COMMENT '该半小时电量',
  `create_time` DATETIME        DEFAULT NULL,
  UNIQUE INDEX `uk_order_slot` (`order_id`, `slot_index`)
) ENGINE=InnoDB CHARACTER SET=utf8mb3 COLLATE=utf8mb3_general_ci COMMENT='分时电量分布表(48个半小时)';


-- ============================================================================
-- 第三部分：故障字典预置数据
-- 来源：云快充平台协议 V2.0 附录 13.2 设备故障代码表
-- 五大类：车故障(0x0001~0x0092) / 车桩交互(0x0093~0x01B1) / 桩平台(0x01B2~0x02BF) / 桩故障(0x02C0~0x043F) / 自定义(0x0440~0xFFFF)
-- ============================================================================

-- ==========================================
-- 大类1：车故障 (fault_type=1)
-- ==========================================
INSERT IGNORE INTO `c_fault_code_dict` (`fault_type`, `fault_code`, `fault_name`, `fault_desc`) VALUES
(1, 0x0001, 'BMS通讯异常',                        'BMS通讯链路不可用'),
(1, 0x0002, 'BCP充电参数配置报文超时',             '车辆未在规定时间内发送BCP报文'),
(1, 0x0003, 'BRO充电准备就绪报文超时',             '车辆未在规定时间内发送BRO报文'),
(1, 0x0004, 'BCS电池充电状态报文超时',             '车辆未在规定时间内发送BCS报文'),
(1, 0x0005, 'BCL电池充电需求报文超时',             '车辆未在规定时间内发送BCL报文'),
(1, 0x0006, 'BST中止充电报文超时',                 '车辆未在规定时间内发送BST报文'),
(1, 0x0007, 'BSD充电统计数据报文超时',             '车辆未在规定时间内发送BSD报文'),
(1, 0x0008, 'BSM动力蓄电池状态报文超时',           '车辆未在规定时间内发送BSM报文'),
(1, 0x0009, 'BRO重大故障停止充电',                 'BRO报文指示重大故障'),
(1, 0x000A, 'BHM桩的输出能力不匹配',               '车辆需求超过充电桩输出能力'),
(1, 0x000B, 'BRM车辆辨识报文超时',                 '车辆未在规定时间内发送BRM报文'),
(1, 0x000C, 'BEM充电错误报文超时',                 '车辆未在规定时间内发送BEM报文'),
(1, 0x000D, 'BMS需求电压过低/过高',                '车辆需求电压超出充电桩输出范围'),
(1, 0x000E, 'BMS绝缘故障',                         '车辆端绝缘检测异常'),
(1, 0x0010, 'BMS元件过温',                         'BMS监测到元件温度过高'),
(1, 0x0011, 'BMS电压过高',                         'BMS监测到电压超出正常范围'),
(1, 0x0012, 'BMS预充电压不匹配',                   '预充电电压与期望值不符'),
(1, 0x0013, 'BMS其他故障',                         'BMS上报未分类故障'),
(1, 0x0014, '动力蓄电池充电过流',                  '充电电流超过电池允许最大值'),
(1, 0x0015, '动力蓄电池温度过高',                  '电池温度超出安全范围'),
(1, 0x0016, '动力蓄电池绝缘故障',                  '电池组绝缘检测失败'),
(1, 0x0017, '动力蓄电池连接器故障',                '电池连接器物理连接异常'),
(1, 0x0018, '电池反接',                            '电池极性反接保护触发'),
(1, 0x0019, '电池欠压',                            '电池电压低于允许范围'),
(1, 0x001A, '电池电压异常',                        '电池电压超出正常范围'),
(1, 0x001B, 'CRO充电机输出就绪超时',               '充电机输出就绪超时'),
(1, 0x001C, 'CCS充电机状态报文超时',               '充电机状态报文超时'),
(1, 0x001D, 'CST充电机终止充电报文超时',           '充电机终止充电报文超时'),
(1, 0x001E, 'CSD充电统计数据报文超时',             '充电统计数据报文超时'),
(1, 0x001F, '车辆电流不匹配',                      '车辆检测电流与期望不符'),
(1, 0x0020, 'BMS异常停止',                         'BMS异常请求停止充电'),
(1, 0x0021, 'BMS需求电压异常',                     'BMS需求电压参数异常'),
(1, 0x0022, '预充电电池电压过低',                  '预充电阶段电池电压不足'),
(1, 0x0023, '车辆主动停止',                        '车辆主动发送停止充电请求'),
(1, 0x0024, '最高允许充电电压过低',                'BMS上报的最高充电电压过低'),
(1, 0x0025, 'BST绝缘故障',                         'BST报文指示绝缘故障'),
(1, 0x0026, 'BST输出连接器过温',                   'BST报文指示连接器过温'),
(1, 0x0027, 'BST-BMS元件故障',                     'BST报文指示元件故障'),
(1, 0x0028, 'BST连接器故障',                       'BST报文指示连接器故障'),
(1, 0x0029, 'BST电池组温度过高',                   'BST报文指示电池温度过高'),
(1, 0x002A, 'BST高压继电器故障',                   'BST报文指示高压继电器故障'),
(1, 0x002B, 'BST检测点2故障',                      'BST报文指示检测点2异常'),
(1, 0x002C, 'BST其他故障',                         'BST报文指示其他类型故障'),
(1, 0x002D, 'BST电流过大',                         'BST报文指示电流过大'),
(1, 0x002E, 'BST电压异常',                         'BST报文指示电压异常'),
(1, 0x002F, 'BSM单体电池电压过压',                 'BSM报文指示单体过压'),
(1, 0x0030, 'BSM单体电池电压过低',                 'BSM报文指示单体欠压'),
(1, 0x0031, 'BSM充电过电流',                       'BSM报文指示充电电流过大'),
(1, 0x0032, 'BSM电池温度过高',                     'BSM报文指示温度过高'),
(1, 0x0033, 'BSM电池绝缘状态异常',                 'BSM报文指示绝缘异常'),
(1, 0x0034, 'BSM连接器连接状态异常',               'BSM报文指示连接器异常'),
(1, 0x0035, 'BCL充电模式异常',                     'BCL报文指示充电模式不匹配'),
(1, 0x0090, '电池电压与BCP上送值不符',             '绝缘检查时发现电压不一致'),
(1, 0x0091, '绝缘检查中电池电压异常',              '绝缘检查阶段电池电压异常'),
(1, 0x0092, 'BMS通信超时',                         '充电中BMS通信超时中断');

-- ==========================================
-- 大类2：车桩交互故障 (fault_type=2)
-- ==========================================
INSERT IGNORE INTO `c_fault_code_dict` (`fault_type`, `fault_code`, `fault_name`, `fault_desc`) VALUES
(2, 0x0093, '预充电K1K2位置异常',                    '直流接触器K1K2状态检测异常'),
(2, 0x0094, '预充电K5K6位置异常',                    '直流接触器K5K6状态检测异常'),
(2, 0x0095, '电池电压过低或与上送值不符',            '绝缘检查前电池电压异常'),
(2, 0x0096, '电池最高允许充电电压小于充电机最小输出电压', '电池与充电机电压不匹配'),
(2, 0x0097, '绝缘监测前直流输出接触器外侧电压≥10V',  '绝缘前外部有正压'),
(2, 0x0098, '绝缘监测前直流输出接触器外侧电压≤-10V', '绝缘前外部有负压'),
(2, 0x0099, '启动充电前接触器外侧电压与通信报文电池电压相差＞±5%', '启动前电压不一致'),
(2, 0x009A, '启动前接触器外侧电压小于充电机最小输出电压', '启动前电压过低'),
(2, 0x009B, '启动前接触器外侧电压大于充电机最大输出电压', '启动前电压过高'),
(2, 0x009C, '电池端电压大于电池最高允许充电电压',     '电池电压超限'),
(2, 0x009D, '其他数据错误',                          '充电参数数据校验错误'),
(2, 0x009E, '电池极性反接故障',                      '检测到电池极性反接'),
(2, 0x009F, 'BRO报文(0xAA)接收超时',                 'AA->00也算接收AA超时'),
(2, 0x00A0, 'BSM报文接收超时',                       '充电中BSM报文超时'),
(2, 0x00A1, 'BMS通信协议版本不匹配',                 '通信协议版本与平台要求不一致'),
(2, 0x00A2, '充电桩充电中暂停超时',                  '充电过程中暂停超时'),
(2, 0x00A3, '动力蓄电池SOC过高',                     '电池SOC超过充电上限'),
(2, 0x00A4, '动力蓄电池SOC过低',                     '电池SOC低于允许下限'),
(2, 0x00A5, '动力蓄电池过电流',                      '充电电流超过电池允许值'),
(2, 0x00A6, '动力蓄电池绝缘状态异常',                '电池绝缘检测失败'),
(2, 0x00A7, '连接器连接状态异常',                    '充电连接器连接异常'),
(2, 0x00A8, '充电无有效电流',                        '充电启动后无有效电流输出'),
(2, 0x00A9, '高压继电器故障',                        '高压继电器动作异常'),
(2, 0x00AA, '检测点2电压检测故障',                   '检测点2电压异常'),
(2, 0x00AB, 'BMS检测电流过大',                       'BMS检测到电流超限'),
(2, 0x00AC, 'BMS检测电压异常',                       'BMS检测到电压异常'),
(2, 0x00AD, '充电机检测电压异常',                    '充电机侧检测电压异常'),
(2, 0x00AE, '充电机检测电流不匹配',                  '充电机侧检测电流与预期不符'),
(2, 0x01B0, '账单上送失败',                          '交易记录上送平台失败'),
(2, 0x01B1, '账单上送超时',                          '交易记录上送平台超时');

-- ==========================================
-- 大类3：桩/平台故障 (fault_type=3)
-- ==========================================
INSERT IGNORE INTO `c_fault_code_dict` (`fault_type`, `fault_code`, `fault_name`, `fault_desc`) VALUES
(3, 0x01B2, '对时失败',                              '充电桩与平台时间同步失败'),
(3, 0x01B3, 'VIN验证超时',                           'VIN码鉴权流程超时'),
(3, 0x01B4, '后台通讯异常',                          '充电桩与运营平台网络连接异常');

-- ==========================================
-- 大类4：桩故障 (fault_type=4)
-- ==========================================
INSERT IGNORE INTO `c_fault_code_dict` (`fault_type`, `fault_code`, `fault_name`, `fault_desc`) VALUES
(4, 0x02C0, '设备自检超时故障',                       '充电桩自检流程超时未完成'),
(4, 0x02C1, '离线故障',                               '充电桩离线无心跳'),
(4, 0x02C2, '柜门被打开故障（非检修状态）',           '非检修状态下充电桩柜门被打开'),
(4, 0x02C3, '急停按键被按下故障',                     '急停按钮被触发'),
(4, 0x02C4, '充电枪未归位告警',                       '充电枪未正确归位'),
(4, 0x02C5, '读卡器异常故障',                         'RFID/IC卡读卡器异常'),
(4, 0x02C6, '电表通讯故障',                           '充电桩与电表通讯中断'),
(4, 0x02C7, '电表数据异常故障',                       '电表返回数据校验失败'),
(4, 0x02C8, '输出接触器粘连故障',                     '直流输出接触器无法断开'),
(4, 0x02C9, '充电设备过温告警',                       '充电设备温度超过告警阈值'),
(4, 0x02CA, '充电接口过温告警',                       '充电枪/座温度超过告警阈值'),
(4, 0x02CB, '充电接口电子锁故障',                     '充电枪电子锁动作异常'),
(4, 0x02CC, '水浸故障',                               '充电桩内部检测到进水'),
(4, 0x02CD, '充电设备内部通讯故障',                   '内部总线/CAN通讯异常'),
(4, 0x02CE, '充电连接故障',                           '充电连接器物理连接异常'),
(4, 0x02CF, '枪口异常故障',                           '充电枪口机械或电气异常'),
(4, 0x02D0, '车位锁故障',                             '车位锁动作异常'),
(4, 0x02D1, '车位锁电池耗尽故障',                     '车位锁电池电量耗尽'),
(4, 0x02D2, '车位锁落锁失败故障',                     '车位锁无法锁定'),
(4, 0x02D3, '执行远程功率分配策略失败告警',           '功率分配策略执行异常'),
(4, 0x02D4, '交流接触器故障',                         '交流输入接触器动作异常'),
(4, 0x02D5, '枪头插拔次数告警',                       '充电枪插拔次数超过维护阈值'),
(4, 0x02D6, '启动充电超时故障',                       '启动充电流程超时'),
(4, 0x02D7, '启动完成应答失败故障',                   '平台未确认启动完成报告'),
(4, 0x02D8, '导引板通讯故障',                         '导引电路板通讯异常'),
(4, 0x02D9, '灯板通讯故障',                           '指示灯板通讯异常'),
(4, 0x02DA, '输出短路故障',                           '充电输出端检测到短路'),
(4, 0x02DB, '避雷器故障',                             'SPD防雷保护模块失效'),
(4, 0x02DC, '烟雾故障',                               '充电桩内部检测到烟雾'),
(4, 0x02DD, '交易记录已满告警',                       '本地存储的交易记录数量超限'),
(4, 0x02DE, '输入电源故障',                           '交流输入过压/过流/欠压/跳闸'),
(4, 0x02DF, '交流断路器故障',                         '交流输入断路器动作异常'),
(4, 0x02E0, '检测点电压检测故障',                     '检测点电压采样异常'),
(4, 0x02E1, '输入缺相告警',                           '三相交流输入缺相'),
(4, 0x02E2, '地线故障',                               '接地保护异常'),
(4, 0x02E3, '交流防雷故障',                           '交流侧防雷保护异常'),
(4, 0x02E4, '三相不平衡告警',                         '三相交流输入电压不平衡'),
(4, 0x02E5, '车辆占位超时告警',                       '车辆占用充电位超时未充电'),
(4, 0x02E6, '系统风扇故障',                           '充电桩散热系统风扇异常'),
(4, 0x02E7, '模块风扇故障',                           '充电模块内部风扇异常'),
(4, 0x02E8, '模块通讯故障',                           '充电模块与主控通讯异常'),
(4, 0x02E9, '电源模块地址冲突故障',                   '多个电源模块地址重复'),
(4, 0x02EA, '电源模块故障',                           '充电电源模块硬件故障'),
(4, 0x02EB, '电源模块过温告警',                       '电源模块温度超过告警阈值'),
(4, 0x02EC, '无空闲模块可用',                         '所有充电模块均不可用'),
(4, 0x02ED, '直流接触器故障',                         '直流输出接触器动作异常'),
(4, 0x02EE, '直流熔断器故障',                         '直流输出熔断器熔断'),
(4, 0x02EF, '中间继电器故障',                         '控制回路中间继电器异常'),
(4, 0x02F0, '辅助电源故障',                           '控制电路辅助电源异常'),
(4, 0x02F1, '绝缘监测故障',                           '绝缘监测模块异常'),
(4, 0x02F2, '泄放回路故障',                           '泄放电路工作异常'),
(4, 0x02F3, '内部通讯故障',                           '充电桩内部通讯异常'),
(4, 0x02F4, '充电设备暂停使用',                       '充电桩维护中暂停服务'),
(4, 0x02F5, '自检功率分配超时告警',                   '自检阶段功率分配超时'),
(4, 0x02F6, '母联粘连故障',                           '母联接触器粘连'),
(4, 0x02F7, '预充完成超时故障',                       '预充电流程超时'),
(4, 0x02F8, '模块开机超时故障',                       '充电模块开机超时'),
(4, 0x02F9, '功率控制模块故障',                       '功率分配控制模块异常'),
(4, 0x02FA, '开关模块故障',                           '开关控制模块异常'),
(4, 0x02FB, '计费控制单元通讯故障',                   '计费控制单元与主控通讯异常'),
(4, 0x02FC, '环境监控板通讯故障',                     '环境监控板通讯异常'),
(4, 0x02FD, '空调通讯故障',                           '空调模块通讯异常'),
(4, 0x02FE, '无源开出盒通讯故障',                     '无源开出盒通讯异常'),
(4, 0x02FF, '无源开入盒通讯故障',                     '无源开入盒通讯异常'),
(4, 0x0300, '绝缘采样盒通讯故障',                     '绝缘采样盒通讯异常'),
(4, 0x0301, '直流采样盒通讯故障',                     '直流采样盒通讯异常'),
(4, 0x0302, '输出电压过压故障',                       '充电输出电压超过额定上限'),
(4, 0x0303, '输出电压过流故障',                       '充电输出电流超过额定上限'),
(4, 0x0304, '输出电压欠压故障',                       '充电输出电压低于额定下限'),
(4, 0x0305, '桩群电容量超过额定限制故障',             '站点总功率超过变压器容量'),
(4, 0x0306, '车/桩电压异常故障',                      '车辆与充电桩电压匹配异常'),
(4, 0x0307, '模块保护故障',                           '充电模块保护动作'),
(4, 0x0308, '预充电启动模块失败',                     '预充电阶段启动模块失败'),
(4, 0x0309, '输出电流大于最高允许充电电流',           '输出电流超过允许上限'),
(4, 0x030A, '系统重启',                               '充电桩系统异常重启'),
(4, 0x030B, '监控之间通信出错',                       '主监控与从监控通信异常'),
(4, 0x030C, '液晶屏通讯故障',                         '显示屏与主控通讯异常'),
(4, 0x030D, '绝缘检查电池电压未达预设值',             '绝缘检查前电池电压不足'),
(4, 0x030E, '烟感故障',                               '烟雾传感器故障'),
(4, 0x030F, '交流失电',                               '交流输入电源中断'),
(4, 0x0310, 'K1K2位置异常',                           '直流接触器K1K2状态不符合预期'),
(4, 0x0311, '充电模块交流过压',                       '模块交流输入过压'),
(4, 0x0312, '充电模块交流欠压',                       '模块交流输入欠压'),
(4, 0x0313, '充电模块短路故障',                       '模块输出短路'),
(4, 0x0314, '整流柜通信出错',                         '整流柜与控制单元通信异常'),
(4, 0x0315, '绝缘检查启动模块失败',                   '绝缘检查阶段模块启动失败'),
(4, 0x0316, '泄放超时',                               '泄放电路放电超时'),
(4, 0x0317, '充电机最大输出能力不足',                 '充电机输出功率不满足需求'),
(4, 0x0318, '预充电电池电压过高',                     '预充电阶段电池电压过高'),
(4, 0x0319, '枪1或枪2绝缘监测故障',                   '充电枪绝缘监测异常'),
(4, 0x031A, '充电桩其他故障',                         '充电桩未分类的其他故障'),
(4, 0x031B, '自检功率分配超时',                       '自检功率分配流程超时'),
(4, 0x031C, '预充完成超时',                           '预充电完成流程超时'),
(4, 0x031D, '桩群电容量超过额定限制',                 '站点功率超限'),
(4, 0x031E, '漏电保护',                               '漏电保护装置动作'),
(4, 0x031F, '地线报警',                               '接地异常报警'),
(4, 0x0320, '交流防雷报警',                           '交流侧防雷保护告警'),
(4, 0x0321, '其他电源故障',                           '电源系统其他故障'),
(4, 0x0322, '车/桩电压异常',                          '车辆与充电桩电压匹配异常'),
(4, 0x0323, '系统闭锁',                               '系统安全闭锁保护'),
(4, 0x0324, '控制导引故障',                           'CC/CP控制导引信号异常'),
(4, 0x0325, '充电机温湿度异常',                       '充电机内部温湿度超出范围'),
(4, 0x0326, '交流输入接触器拒动/误动故障',            '交流输入接触器动作异常'),
(4, 0x0327, '直流输出接触器拒动/误动故障',            '直流输出接触器动作异常'),
(4, 0x0328, '直流输出接触器粘连故障',                 '直流输出接触器无法断开'),
(4, 0x0329, '并联接触器拒动/误动故障',                '并联接触器动作异常'),
(4, 0x032A, '并联接触器粘连故障',                     '并联接触器无法断开'),
(4, 0x032B, '充电机模块通信告警（局部）',             '部分充电模块通信告警'),
(4, 0x032C, '充电机模块通信故障（全部）',             '全部充电模块通信中断'),
(4, 0x032D, '充电机模块故障（局部）',                 '部分充电模块故障'),
(4, 0x032E, '充电机模块故障（全部）',                 '全部充电模块故障'),
(4, 0x032F, '直流输出电流过流故障',                   '直流输出电流超限'),
(4, 0x0330, '直流输出短路故障',                       '直流输出端短路'),
(4, 0x0331, '直流采样单元通讯故障',                   '直流采样单元通讯异常'),
(4, 0x0332, '充电桩内部其他通讯故障',                 '内部其他通讯链路异常'),
(4, 0x0333, '辅助电源故障(仅直流桩使用)',             '直流桩辅助电源异常'),
(4, 0x0334, '模块开机升压失败',                       '充电模块开机后升压失败');

-- ==========================================
-- 大类5：自定义故障 (fault_type=5)  范围 0x0440~0xFFFF
-- ==========================================
INSERT IGNORE INTO `c_fault_code_dict` (`fault_type`, `fault_code`, `fault_name`, `fault_desc`) VALUES
(5, 0x0440, '厂商自定义故障起始',                     '厂商自定义故障编码起始值，可根据实际需要扩展');


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 迁移完成
-- 共计：3张表ALTER（7个新字段+2个类型修改）+ 8张新表 + 144条故障字典数据
-- ============================================================================