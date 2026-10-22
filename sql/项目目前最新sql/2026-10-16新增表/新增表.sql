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

#  新增 c_mp_user_car 表管理车主绑定的多台车辆，扫码下单时选择车辆，自动回填车牌号到充电订单；VIN 码用于即插即充场景匹配车辆身份。
CREATE TABLE `c_mp_user_car` (
      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
      `user_id` bigint NOT NULL COMMENT '关联车主 c_mp_user.id',
      `plate_number` varchar(20) NOT NULL COMMENT '车牌号',
      `plate_color` tinyint DEFAULT 1 COMMENT '车牌颜色：1蓝 2绿 3黄 4白',
      `vin` varchar(50) DEFAULT NULL COMMENT '车架号VIN，即插即充BMS匹配用',
      `car_model` varchar(64) DEFAULT NULL COMMENT '车辆品牌型号',
      `register_date` date DEFAULT NULL COMMENT '注册日期',
      `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认车辆：1是 0否，一个用户仅一个默认',
      `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0解绑',
      `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0未删除 1已删除',
      `current_mileage` double DEFAULT NULL COMMENT '当前行驶里程',
      `battery_capacity` double DEFAULT NULL COMMENT '电池容量',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      KEY `idx_user_id` (`user_id`),
      UNIQUE KEY `uk_user_plate` (`user_id`, `plate_number`) COMMENT '同一用户不能重复绑定同车牌'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车主绑定车辆表';


# 新增 c_charge_price_time_slot: 所有模式的时段、电价、服务费全部存在这一张表，通过 `rule_id` 关联主表，一对多。兼容标准、分时、自定义分时、分段 4 种模式。
CREATE TABLE `c_charge_price_time_slot` (
      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时段主键',
      `rule_id` bigint NOT NULL COMMENT '关联计费规则主表 c_charge_price_rule.id',
      `time_type` tinyint NOT NULL DEFAULT 0 COMMENT '时段类型：0普通/分段 1尖 2峰 3平 4谷 5深谷',
      `start_time` varchar(5) NOT NULL COMMENT '开始时间，格式 HH:mm，如 08:00',
      `end_time` varchar(5) NOT NULL COMMENT '结束时间，格式 HH:mm，如 22:00',
      `elec_price` decimal(10,4) NOT NULL COMMENT '电费单价，单位：元/kWh',
      `service_price` decimal(10,4) NOT NULL COMMENT '服务费单价，单位：元/kWh',
      `sort` int DEFAULT 0 COMMENT '时段排序',
      PRIMARY KEY (`id`),
      KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费时段电价明细表';


# 建议新增：运营商清分/收款账户表（对接汇付等渠道的分账接收方建档）
# 关联：merchant_id -> sys_tenant.id（与 sys_tenant_info.merchant_id 同源）
CREATE TABLE `sys_tenant_settle_account` (
      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
      `merchant_id` bigint NOT NULL COMMENT '运营商ID，关联 sys_tenant.id（SaaS隔离）',
      `channel_code` varchar(32) NOT NULL DEFAULT 'huifu' COMMENT '清分渠道：huifu=汇付，预留多渠道',
      `account_type` tinyint NOT NULL COMMENT '收款账户类型：1-对公账户/2-对私账户',
      `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否当前生效：1-是/0-否；同一渠道应用层保证仅一条默认',
      `verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '建档校验状态：0-草稿/待验证 1-已验证通过 2-驳回',
      `channel_member_no` varchar(128) DEFAULT NULL COMMENT '渠道侧会员号/分账接收方ID（汇付建档返回）',
      `channel_bind_id` varchar(128) DEFAULT NULL COMMENT '渠道绑定关系ID',

    -- 对私（个人）字段
      `personal_name` varchar(64) DEFAULT NULL COMMENT '个人姓名（对私）',
      `personal_id_type` varchar(32) DEFAULT NULL COMMENT '个人证件类型（对私）',
      `personal_id_no` varchar(64) DEFAULT NULL COMMENT '个人证件号码（对私，建议加密存储）',
      `personal_bank_name` varchar(128) DEFAULT NULL COMMENT '个人开户银行（对私）',

    -- 对公（法人）字段
      `legal_name` varchar(64) DEFAULT NULL COMMENT '法人姓名（对公）',
      `legal_id_type` varchar(32) DEFAULT NULL COMMENT '法人证件类型（对公）',
      `legal_id_no` varchar(64) DEFAULT NULL COMMENT '法人证件号码（对公）',
      `legal_id_expire` varchar(16) DEFAULT NULL COMMENT '法人证件有效期 yyyyMMdd（对公）',

    -- 对公（企业）字段
      `company_name` varchar(128) DEFAULT NULL COMMENT '企业名称（对公）',
      `company_cert_type` varchar(32) DEFAULT NULL COMMENT '企业证件类型（对公）',
      `company_cert_no` varchar(64) DEFAULT NULL COMMENT '企业证件号码/统一社会信用代码（对公）',
      `company_cert_expire` varchar(16) DEFAULT NULL COMMENT '企业证件有效期 yyyyMMdd，长期填20991231',
      `company_bank_name` varchar(128) DEFAULT NULL COMMENT '企业开户银行（对公）',
      `business_scope` varchar(255) DEFAULT NULL COMMENT '企业证件经营范围（对公）',

    -- 通用银行账户（对公=企业对公账号，对私=个人卡号）
      `bank_account_name` varchar(128) DEFAULT NULL COMMENT '开户名：对公=企业全称，对私=个人姓名',
      `bank_card_no` varchar(64) DEFAULT NULL COMMENT '银行卡号/对公账号',
      `bank_branch_code` varchar(12) DEFAULT NULL COMMENT '开户行联行号12位，精准打款核验',
      `province_code` varchar(12) DEFAULT NULL COMMENT '开户/企业地址省编码',
      `province_name` varchar(32) DEFAULT NULL COMMENT '省名称',
      `city_code` varchar(12) DEFAULT NULL COMMENT '市编码',
      `city_name` varchar(32) DEFAULT NULL COMMENT '市名称',
      `company_address` varchar(255) DEFAULT NULL COMMENT '企业详细地址（对公）',

    -- 证照图片（复用项目现有 OSS 对象存储）
      `license_img` varchar(255) DEFAULT NULL COMMENT '三证合一证件照（对公）',
      `bank_permit_img` varchar(255) DEFAULT NULL COMMENT '开户银行许可证照（对公）',
      `legal_id_front` varchar(255) DEFAULT NULL COMMENT '法人身份证正面（对公）',
      `legal_id_back` varchar(255) DEFAULT NULL COMMENT '法人身份证反面（对公）',

       `operator_mobile` varchar(20) DEFAULT NULL COMMENT '经办人/联系人手机号',
       `verify_remark` varchar(255) DEFAULT NULL COMMENT '渠道校验/驳回说明',
       `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0停用',
       `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0存在 1删除',
       `create_by` varchar(64) DEFAULT '',
       `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
       `update_by` varchar(64) DEFAULT '',
       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       `remark` varchar(500) DEFAULT NULL,
       PRIMARY KEY (`id`),
       KEY `idx_merchant` (`merchant_id`),
       KEY `idx_channel` (`channel_code`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营商清分/收款账户表';

# 新增 用户 - 运营商共享授权表:平台可以把普通用户共享给指定的某个运营商,主要是用于给特定用户在这个运营商下的不同优惠
CREATE TABLE `c_mp_user_merchant_share` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '共享记录主键',
                                       `user_id` bigint NOT NULL COMMENT '平台车主用户ID，关联 user_member.id',
                                       `merchant_id` bigint NOT NULL COMMENT '归属运营商ID，关联 sys_merchant.id',

                                       `category_id` bigint DEFAULT NULL COMMENT '共享分类ID，关联共享分类表',
                                       `group_type` varchar(64) DEFAULT NULL COMMENT '集团分类（企业用户标签，对应截图集团分类列）',

                                       `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1生效中 0已取消 2已过期',
                                       `effect_time` datetime NOT NULL COMMENT '生效时间',
                                       `expire_time` datetime DEFAULT NULL COMMENT '到期时间，NULL=永久',

                                       `share_source` tinyint NOT NULL COMMENT '共享来源：1平台后台分配 2用户主动授权 3企业签约同步',
                                       `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（平台管理员）',

                                       `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
                                       `cancel_operator` bigint DEFAULT NULL COMMENT '取消操作人',
                                       `cancel_reason` varchar(255) DEFAULT NULL,

                                       `remark` varchar(500) DEFAULT NULL,
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                       PRIMARY KEY (`id`),
    -- 联合唯一：同一个用户、同一个运营商、同一个分类，仅一条有效记录
                                       UNIQUE KEY `uk_user_merchant_category` (`user_id`, `merchant_id`, `category_id`),
                                       KEY `idx_merchant_id` (`merchant_id`),
                                       KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-运营商共享分配表';


# 新增 c_mp_user_wallet 表:平台统一电子钱包，存储可用余额、充电冻结金额，支持预充值、充电扣费、结算退款。
CREATE TABLE `c_mp_user_wallet` (
     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
     `user_id` bigint NOT NULL COMMENT '关联车主 c_mp_user.id',
     `balance` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额（元）',
     `frozen_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额（充电中预占用金额）',
     `total_recharge` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
     `total_consume` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
     `pay_password` varchar(128) DEFAULT NULL COMMENT '支付密码（BCrypt加密存储）',
     `wallet_status` tinyint NOT NULL DEFAULT 1 COMMENT '钱包状态：1正常 0冻结',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车主钱包表';


# 新增 c_mp_user_invoice 表:管理车主开票抬头，支持个人 / 企业两种类型，充电结束后申请开票时直接选用。
CREATE TABLE `c_mp_user_invoice` (
       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
       `user_id` bigint NOT NULL COMMENT '关联车主 c_mp_user.id',
       `invoice_type` tinyint NOT NULL COMMENT '发票类型：1个人 2企业',
       `title_name` varchar(128) NOT NULL COMMENT '发票抬头名称',
       `tax_number` varchar(64) DEFAULT NULL COMMENT '纳税人识别号（企业必填）',
       `address` varchar(255) DEFAULT NULL COMMENT '企业地址',
       `mobile` varchar(20) DEFAULT NULL COMMENT '联系电话',
       `bank_name` varchar(128) DEFAULT NULL COMMENT '开户银行',
       `bank_account` varchar(64) DEFAULT NULL COMMENT '银行账号',
       `email` varchar(128) DEFAULT NULL COMMENT '收票邮箱',
       `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认抬头：1是 0否',
       `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0删除',
       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       PRIMARY KEY (`id`),
       KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票抬头管理表';

# 新增 c_mp_user_wallet_transaction 表:钱包余额变动必须全链路留痕，所有充值、消费、冻结、解冻、退款都生成流水，用于财务对账、差错排查、资损防控。
CREATE TABLE `c_mp_user_wallet_transaction` (
        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
        `user_id` bigint NOT NULL COMMENT '关联车主 c_mp_user.id',
        `wallet_id` bigint NOT NULL COMMENT '关联钱包 c_mp_user_wallet.id',
        `trans_no` varchar(64) NOT NULL COMMENT '交易流水号（全局唯一）',
        `trans_type` tinyint NOT NULL COMMENT '交易类型：1充值 2消费扣费 3充电冻结 4解冻 5退款 6调账',
        `amount` decimal(10,2) NOT NULL COMMENT '交易金额，正数增加余额，负数扣减余额',
        `balance_before` decimal(10,2) NOT NULL COMMENT '交易前可用余额',
        `balance_after` decimal(10,2) NOT NULL COMMENT '交易后可用余额',
        `business_type` tinyint DEFAULT NULL COMMENT '业务类型：1充电订单 2充值订单 3退款单',
        `business_no` varchar(64) DEFAULT NULL COMMENT '关联业务单号：充电订单号/充值单号',
        `trans_status` tinyint NOT NULL DEFAULT 1 COMMENT '交易状态：1成功 2处理中 3失败',
        `remark` varchar(255) DEFAULT NULL COMMENT '交易备注',
        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (`id`),
        UNIQUE KEY `uk_trans_no` (`trans_no`),
        KEY `idx_user_time` (`user_id`, `create_time`),
        KEY `idx_business` (`business_type`, `business_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序端用户钱包交易流水表';


# 2026/10/21 新增 运营商/代理商 短信账户表
CREATE TABLE `sms_merchant_account` (
           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
           `subject_id` bigint NOT NULL COMMENT '主体ID(运营商/代理商)',
           `balance` int NOT NULL DEFAULT 0 COMMENT '剩余可用条数',
           `total_recharge` int NOT NULL DEFAULT 0 COMMENT '累计充值条数',
           `total_consume` int NOT NULL DEFAULT 0 COMMENT '累计消耗条数',
           `warn_threshold` int DEFAULT 50 COMMENT '余额预警阈值（条）',
           `status` tinyint NOT NULL DEFAULT 1 COMMENT '账户状态：1正常 0冻结',
           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
           PRIMARY KEY (`id`),
           UNIQUE KEY `uk_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营商短信账户表';

# 2026/10/21 新增 运营商短信充值订单表
CREATE TABLE `sms_merchant_recharge_order` (
          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
          `subject_id` bigint NOT NULL COMMENT '主体ID(运营商/代理商)',
          `order_no` varchar(64) NOT NULL COMMENT '充值订单号',
          `package_name` varchar(64) NOT NULL COMMENT '套餐名称',
          `sms_count` int NOT NULL COMMENT '充值条数',
          `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额（元）',
          `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0待支付 1已支付 2已关闭',
          `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
          `pay_channel` varchar(32) DEFAULT NULL COMMENT '支付渠道：wechat/alipay',
          `operator_id` bigint DEFAULT NULL COMMENT '操作人（平台后台充值时记录）',
          `remark` varchar(255) DEFAULT NULL,
          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (`id`),
          UNIQUE KEY `uk_order_no` (`order_no`),
          KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信充值订单表';


# 2026/10/21 新增 短信发送记录表
CREATE TABLE `sms_send_log` (
           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
           `subject_id` bigint DEFAULT 0 COMMENT '主体ID(运营商/代理商),平台公共为0',
           `mobile` varchar(20) NOT NULL COMMENT '接收手机号',
           `template_code` varchar(64) NOT NULL COMMENT '短信模板编码',
           `template_content` varchar(500) NOT NULL COMMENT '发送内容',
           `sms_type` tinyint NOT NULL COMMENT '短信类型：1验证码 2通知 3营销',
           `send_status` tinyint NOT NULL DEFAULT 0 COMMENT '发送状态：0待发送 1成功 2失败',
           `fee_count` int DEFAULT 0 COMMENT '扣费条数',
           `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
           `biz_type` varchar(32) DEFAULT NULL COMMENT '业务场景：charge_start/charge_end/alert',
           `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务单号：订单号',
           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
           PRIMARY KEY (`id`),
           KEY `idx_subject_time` (`subject_id`, `create_time`),
           KEY `idx_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送记录表';


# 2026/10/21 新增 代理商 - 运营商短信分配流水表：记录代理商给下属运营商充值短信的流水，用于对账、结算差价收益。
CREATE TABLE `sms_agent_distribute_log` (
           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
           `agent_id` bigint NOT NULL COMMENT '代理商ID',
           `merchant_id` bigint NOT NULL COMMENT '目标运营商ID',
           `sms_count` int NOT NULL COMMENT '分配条数',
           `cost_price` decimal(10,4) NOT NULL COMMENT '代理商成本单价（元/条）',
           `sale_price` decimal(10,4) NOT NULL COMMENT '给运营商的单价（元/条）',
           `profit_amount` decimal(10,2) NOT NULL COMMENT '本次差价收益',
           `operator_id` bigint NOT NULL COMMENT '操作人（代理商管理员）',
           `remark` varchar(255) DEFAULT NULL,
           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
           PRIMARY KEY (`id`),
           KEY `idx_agent_time` (`agent_id`, `create_time`),
           KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商短信分配流水表';


# 2026/10/21 新增 平台短信模板表
CREATE TABLE `sms_template` (
       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
       `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '所属租户/代理商ID：0-平台全局通用模板；>0-运营商/代理商专属定制模板',
       `template_code` varchar(64) NOT NULL COMMENT '系统内模板唯一编码，如 CHARGE_START_NOTICE',
       `template_name` varchar(128) NOT NULL COMMENT '模板名称（后台展示用）',
       `sms_type` tinyint NOT NULL COMMENT '短信类型：1验证码 2充电通知 3设备告警 4营销活动',
       `sign_name` varchar(64) NOT NULL COMMENT '短信签名，如「云快充」',
       `template_content` varchar(1000) NOT NULL COMMENT '模板正文，变量用${变量名}占位，例：您的充电已启动，场站${stationName}，枪号${gunNo}',
       `variables` varchar(500) DEFAULT NULL COMMENT '支持的变量列表，逗号分隔：stationName,gunNo,orderNo',

       `provider_template_id` varchar(128) DEFAULT NULL COMMENT '短信服务商审核通过的正式模板ID（阿里云/腾讯云）',
       `provider_type` varchar(32) DEFAULT NULL COMMENT '短信服务商：aliyun / tencent',

       `scene_code` varchar(64) DEFAULT NULL COMMENT '业务场景编码，对应触发场景',
       `scene_desc` varchar(255) DEFAULT NULL COMMENT '业务场景说明',

       `sort` int DEFAULT 0 COMMENT '排序',
       `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
       `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '服务商审核状态：0待审核 1审核通过 2审核驳回',
       `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',

       `remark` varchar(500) DEFAULT NULL,
       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_template_code` (`template_code`),
       KEY `idx_tenant_id` (`tenant_id`),
       KEY `idx_sms_type` (`sms_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台短信模板配置表';











