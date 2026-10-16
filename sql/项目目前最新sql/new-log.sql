-- 操作审计日志表 sys_audit_log（合并原 sys_oper_log + sys_logininfor）
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE `log_audit` (
     `id`            bigint        NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
     `audit_type`    tinyint       NOT NULL                   COMMENT '审计类型：1=登录 2=登出 3=新增 4=修改 5=删除 6=导出 7=授权 8=其他',
     `module`        varchar(50)   DEFAULT ''                 COMMENT '操作模块（如"充电桩管理""用户管理"）',
     `log_desc`      varchar(255)  DEFAULT ''                 COMMENT '日志描述（如"新增充电桩P001""登录成功"）',
     `user_id`       bigint        DEFAULT NULL               COMMENT '操作人ID',
     `user_name`     varchar(50)   DEFAULT ''                 COMMENT '操作人账号',
     `dept_name`     varchar(50)   DEFAULT ''                 COMMENT '部门名称',
     `oper_ip`       varchar(128)  DEFAULT ''                 COMMENT '操作IP',
     `oper_location` varchar(255)  DEFAULT ''                 COMMENT '操作地点',
     `request_url`   varchar(255)  DEFAULT ''                 COMMENT '请求URL',
     `request_method`  varchar(10) DEFAULT ''                 COMMENT '请求方式（GET/POST/PUT/DELETE）',
     `request_param` varchar(2000) DEFAULT ''                 COMMENT '请求参数（敏感字段脱敏）',
     `response_result` varchar(2000) DEFAULT ''               COMMENT '响应结果',
     `status`        tinyint       DEFAULT '0'                COMMENT '操作状态：0=成功 1=失败',
     `error_msg`     varchar(2000) DEFAULT ''                 COMMENT '错误信息',
     `cost_time`     bigint        DEFAULT '0'                COMMENT '耗时(毫秒)',
     `merchant_id`     bigint        NOT NULL                   COMMENT '租户(运营商)ID',
     `create_time`   datetime      DEFAULT NULL               COMMENT '创建时间',
     PRIMARY KEY (`id`) USING BTREE,
     KEY `idx_audit_merchant_type` (`merchant_id`, `audit_type`),
     KEY `idx_audit_user` (`user_id`, `create_time`),
     KEY `idx_audit_time` (`create_time`),
     KEY `idx_audit_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表';


# 设备通信报文日志表 c_device_message_log
-- 创建于: hcp-operator 模块
-- 存储策略：MySQL 只存异常报文+充电流程报文，正常30s周期上报不落地
-- 若对接 ES/ClickHouse 可另行适配
DROP TABLE IF EXISTS `c_device_message_log`;
CREATE TABLE `log_device_message` (
       `id`            bigint        NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
       `merchant_id`     bigint        NOT NULL                    COMMENT '租户(运营商)ID',
       `pile_code`     varchar(64)   NOT NULL                    COMMENT '充电桩编号（站点ID+桩号）',
       `port_id`       bigint        DEFAULT NULL                COMMENT '充电口ID（null=桩级报文）',
       `order_no`      varchar(64)   DEFAULT NULL                COMMENT '关联订单号（充电流程报文必填）',
       `msg_direction` tinyint       NOT NULL                    COMMENT '报文方向：1=上行(桩→平台) 2=下行(平台→桩)',
       `msg_type`      varchar(32)   NOT NULL                    COMMENT '报文类型：0x01=登录 0x02=实时上报 0x4F=启动确认 0x5A=停止确认 0x60=功率控制 0xA9=VIN鉴权 ...',
       `msg_tag`       tinyint       NOT NULL DEFAULT '0'        COMMENT '报文标签：0=异常报文 1=充电流程报文 2=手动召唤报文',
       `raw_hex`       mediumtext                                 COMMENT '原始报文(十六进制字符串，单条上限约16MB)',
       `raw_length`    int           DEFAULT '0'                  COMMENT '原始报文字节数',
       `parse_status`  tinyint       DEFAULT '0'                  COMMENT '解析状态：0=未解析 1=解析成功 2=解析失败',
       `parse_error`   varchar(500)  DEFAULT NULL                 COMMENT '解析失败原因',
       `create_time`   datetime      NOT NULL                    COMMENT '报文接收/发送时间',
       PRIMARY KEY (`id`) USING BTREE,
       KEY `idx_msg_pile_time` (`pile_code`, `create_time`),
       KEY `idx_msg_order` (`order_no`),
       KEY `idx_msg_merchant_time` (`merchant_id`, `create_time`),
       KEY `idx_msg_tag` (`msg_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='设备通信报文日志表';


# 设备状态变更日志表 c_device_status_log
-- 创建于: hcp-operator 模块
DROP TABLE IF EXISTS `c_device_status_log`;
CREATE TABLE `log_device_status` (
            `id`            bigint        NOT NULL AUTO_INCREMENT     COMMENT '主键ID',
            `merchant_id`     bigint        NOT NULL                  COMMENT '租户(运营商)ID',
            `pile_code`     varchar(64)   NOT NULL                    COMMENT '充电桩编号',
            `port_id`       bigint        DEFAULT NULL                COMMENT '充电口ID（null=桩级状态变更）',
            `status_type`   tinyint       NOT NULL                    COMMENT '状态类型：1=桩运行状态 2=枪口状态 3=烟感状态 4=BMS状态 5=充电机状态',
            `old_status`    tinyint       DEFAULT NULL                COMMENT '变更前状态码',
            `new_status`    tinyint       NOT NULL                    COMMENT '变更后状态码',
            `change_reason` varchar(255)  DEFAULT NULL                COMMENT '变更原因（如"心跳超时""人工下线""故障触发"）',
            `source`        tinyint       DEFAULT '0'                 COMMENT '来源：0=设备上报 1=平台操作 2=系统自动判定',
            `create_time`   datetime      NOT NULL                    COMMENT '变更时间',
            PRIMARY KEY (`id`) USING BTREE,
            KEY `idx_status_pile_time` (`pile_code`, `status_type`, `create_time`),
            KEY `idx_status_merchant_time` (`merchant_id`, `create_time`),
            KEY `idx_status_port` (`port_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='设备状态变更日志表';


# 告警故障日志表 c_alarm_log
-- 创建于: hcp-operator 模块
-- 合并原有 c_device_fault 表，统一故障+告警管理
DROP TABLE IF EXISTS `c_alarm_log`;
CREATE TABLE `log_alarm` (
        `id`            bigint        NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
        `merchant_id`     bigint        NOT NULL                    COMMENT '租户(运营商)ID',
        `pile_code`     varchar(64)   NOT NULL                    COMMENT '充电桩编号',
        `port_id`       bigint        DEFAULT NULL                COMMENT '充电口ID',
        `order_no`      varchar(64)   DEFAULT NULL                COMMENT '关联订单号',
        `alarm_type`    tinyint       NOT NULL                    COMMENT '告警大类：1=设备故障 2=过温告警 3=烟感告警 4=电量对账异常 5=通信异常 6=自定义',
        `alarm_code`    smallint      NOT NULL                    COMMENT '告警编码(0x0001~0xFFFF)',
        `alarm_name`    varchar(100)  NOT NULL                    COMMENT '告警名称（冗余，方便查询）',
        `alarm_level`   tinyint       DEFAULT '1'                 COMMENT '告警级别：1=一般 2=重要 3=严重',
        `event_type`    tinyint       NOT NULL                    COMMENT '事件类型：1=发生 2=恢复',
        `alarm_time`    datetime      NOT NULL                    COMMENT '告警发生/恢复时间',
        `alarm_detail`  varchar(1000) DEFAULT NULL                COMMENT '告警详情（冗余关键字段快照）',
        `process_status` tinyint      DEFAULT '0'                 COMMENT '处理状态：0=未处理 1=处理中 2=已处理 3=已忽略',
        `process_by`    varchar(50)   DEFAULT NULL                COMMENT '处理人',
        `process_time`  datetime      DEFAULT NULL                COMMENT '处理时间',
        `process_remark` varchar(500) DEFAULT NULL                COMMENT '处理备注',
        `create_time`   datetime      NOT NULL                    COMMENT '创建时间',
        PRIMARY KEY (`id`) USING BTREE,
        KEY `idx_alarm_pile_time` (`pile_code`, `alarm_time`),
        KEY `idx_alarm_merchant_time` (`merchant_id`, `alarm_time`),
        KEY `idx_alarm_status` (`process_status`),
        KEY `idx_alarm_type_level` (`alarm_type`, `alarm_level`),
        KEY `idx_alarm_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='告警故障日志表';


# 订单日志表 c_order_log
-- 创建于: hcp-operator 模块
-- 重构原有 c_order_log，增加结构化字段
DROP TABLE IF EXISTS `c_order_log(弃)`;
CREATE TABLE `log_order` (
         `id`            bigint        NOT NULL AUTO_INCREMENT    COMMENT '主键ID',
         `merchant_id`     bigint        NOT NULL                    COMMENT '租户(运营商)ID',
         `order_no`      varchar(64)   NOT NULL                    COMMENT '订单编号',
         `pile_code`     varchar(64)   DEFAULT NULL                COMMENT '充电桩编号',
         `port_id`       bigint        DEFAULT NULL                COMMENT '充电口ID',
         `user_id`       bigint        DEFAULT NULL                COMMENT '用户ID',
         `log_type`      tinyint       NOT NULL                    COMMENT '日志类型：1=订单创建 2=启动充电 3=充电中 4=停止充电 5=订单结算 6=退款 7=异常中断',
         `log_title`     varchar(50)   DEFAULT ''                  COMMENT '日志标题（如"启动充电""订单结算"）',
         `log_content`   varchar(500)  DEFAULT NULL                COMMENT '日志内容（如"消费金额:30.00元,开始SOC:20%,结束SOC:85%"）',
         `before_data`   varchar(2000) DEFAULT NULL                COMMENT '变更前数据快照(JSON)',
         `after_data`    varchar(2000) DEFAULT NULL                COMMENT '变更后数据快照(JSON)',
         `source`        tinyint       DEFAULT '0'                 COMMENT '来源：0=系统自动 1=管理员操作 2=用户操作',
         `operator_id`   bigint        DEFAULT NULL                COMMENT '操作人ID（人工操作时填写）',
         `operator_name` varchar(50)   DEFAULT NULL                COMMENT '操作人姓名',
         `create_time`   datetime      NOT NULL                    COMMENT '日志时间',
         PRIMARY KEY (`id`) USING BTREE,
         KEY `idx_order_merchant` (`merchant_id`, `order_no`),
         KEY `idx_order_time` (`order_no`, `create_time`),
         KEY `idx_order_pile` (`pile_code`, `create_time`),
         KEY `idx_order_type` (`log_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='订单操作日志表';
