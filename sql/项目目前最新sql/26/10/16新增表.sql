#  新增 c_charging_pile_brand 充电桩品牌表
CREATE TABLE c_charging_pile_brand (
     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
     merchant_id BIGINT NOT NULL COMMENT '租户/运营商 ID（SaaS隔离）',
     brand_code VARCHAR(64) NOT NULL COMMENT '品牌编码：weijing/tonghe/zhinet',
     brand_name VARCHAR(100) NOT NULL COMMENT '品牌名称：蔚景、通合、智网',
     remark VARCHAR(255) COMMENT '备注',
     sort INT DEFAULT 0 COMMENT '排序',
     status TINYINT DEFAULT 1 COMMENT '状态：1启用/0禁用',
     create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
     update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     UNIQUE KEY uk_merchant_brandcode (merchant_id, brand_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩品牌表';

#  新增 c_charging_pile_model 充电桩型号模板表（重点！存预设硬件信息）
#  每条记录绑定一个品牌，**存储这个型号固定的硬件参数**，选中型号自动把这些字段回填到充电桩实例。
CREATE TABLE c_charging_pile_model (
      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
      merchant_id BIGINT NOT NULL COMMENT '租户/运营商 ID（SaaS隔离）',
      brand_id BIGINT NOT NULL COMMENT '关联品牌ID c_charging_pile_brand.id',
      model_code VARCHAR(64) NOT NULL COMMENT '型号编码',
      model_name VARCHAR(128) NOT NULL COMMENT '型号名称：通合直流桩双枪-60kw',
      pile_type TINYINT NOT NULL COMMENT '桩类型：1直流 2交流',
      gun_count INT NOT NULL COMMENT '枪数量：1单枪 /2双枪',
      rated_power DECIMAL(10,2) NOT NULL COMMENT '额定总功率 kW',
      single_gun_power DECIMAL(10,2) COMMENT '单枪额定功率 kW',
      protocol_type VARCHAR(64) COMMENT '通讯协议：OCPP1.6 / 蔚景私有协议 / 国标104',
      max_current DECIMAL(10,2) COMMENT '最大输出电流',
      voltage_level VARCHAR(32) COMMENT '额定电压',
      remark VARCHAR(255) COMMENT '型号备注',
      sort INT DEFAULT 0,
      status TINYINT DEFAULT 1 COMMENT '状态：1启用/0禁用',
      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY uk_merchant_modelcode (merchant_id, model_code),
      KEY idx_brand_id (brand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩型号模板表';

#



























