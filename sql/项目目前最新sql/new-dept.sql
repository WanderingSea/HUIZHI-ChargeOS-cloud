# 运营商/租户(顶级组织) 自定义部门角色身份
CREATE TABLE `sys_dept_role` (
            `dept_role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门角色ID',
            `dept_id` bigint NOT NULL COMMENT '所属部门ID',
            `role_name` varchar(100) NOT NULL COMMENT '角色名称',
            `role_key` varchar(100) NOT NULL COMMENT '角色权限标识',
            `role_sort` int DEFAULT '0' COMMENT '显示顺序',
            `data_scope` char(1) DEFAULT '3' COMMENT '数据范围(1-全部/2-自定义/3-本部门/4-本部门及以下/5-仅本人)',
            `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
            `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
            `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
            `merchant_id` bigint NOT NULL COMMENT '所属运营商ID',
            `parent_role_id` bigint DEFAULT NULL COMMENT '继承的系统角色ID(关联sys_role.role_id)',
            `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0存在 1删除)',
            `description` varchar(500) DEFAULT NULL COMMENT '角色描述',
            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
            PRIMARY KEY (`dept_role_id`),
            UNIQUE KEY `uk_dept_role_key` (`dept_id`,`role_key`,`del_flag`),
            KEY `idx_dept` (`dept_id`),
            KEY `idx_merchant` (`merchant_id`),
            KEY `idx_parent_role` (`parent_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门角色表(租户或运营商(顶级组织)自定义角色)';


# 运营商组织表(顶级组织下可以有多个子组织,子组织下也可以有多个组织)
CREATE TABLE `sys_merchant_org` (
      `org_id` bigint NOT NULL AUTO_INCREMENT COMMENT '组织ID',
      `parent_id` bigint DEFAULT '0' COMMENT '父组织ID',
      `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
      `org_name` varchar(100) NOT NULL COMMENT '组织名称',
      `org_type` tinyint NOT NULL DEFAULT '1' COMMENT '组织类型(1-总部/2-分公司/3-门店/4-代理商)',
      `merchant_id` bigint NOT NULL COMMENT '所属运营商ID',
      `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
      `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
      `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
      `order_num` int DEFAULT '0' COMMENT '显示顺序',
      `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
      `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
      `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
      `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
      `update_time` datetime DEFAULT NULL COMMENT '更新时间',
      `remark` varchar(500) DEFAULT NULL COMMENT '备注',
      PRIMARY KEY (`org_id`),
      KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营商组织表(顶级组织下可以有多个子组织,子组织下也可以有多个组织)';

# 用户组织关联表
CREATE TABLE `sys_user_org` (
      `user_id` bigint NOT NULL COMMENT '用户ID',
      `org_id` bigint NOT NULL COMMENT '组织ID',
      `is_primary` tinyint DEFAULT '0' COMMENT '是否组织负责人(0-否/1-是)',
      `merchant_id` bigint NOT NULL COMMENT '运营商ID',
      PRIMARY KEY (`user_id`,`org_id`),
      KEY `idx_org` (`org_id`),
      KEY `idx_tenant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织关联表';

# 用户部门角色关联表(将用户分配到部门级角色)
CREATE TABLE `sys_user_dept_role` (
             `user_id` bigint NOT NULL COMMENT '用户ID',
             `dept_role_id` bigint NOT NULL COMMENT '部门角色身份ID(关联sys_dept_role.dept_role_id)',
             `dept_id` bigint NOT NULL COMMENT '所属部门ID(冗余,便于查询)',
             `merchant_id` bigint NOT NULL COMMENT '运营商ID',
             `is_primary` tinyint DEFAULT '0' COMMENT '是否主角色(0-否/1-是)',
             PRIMARY KEY (`user_id`, `dept_role_id`),
             KEY `idx_dept_role` (`dept_role_id`),
             KEY `idx_dept` (`dept_id`),
             KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与部门角色关联表';


