-- SRS Inspector Tables
-- Create by jeecg-boot 2025-01-01

-- ----------------------------
-- Table structure for srs_scan_task
-- ----------------------------
DROP TABLE IF EXISTS `srs_scan_task`;
CREATE TABLE `srs_scan_task` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `task_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `scan_path` varchar(500) DEFAULT NULL COMMENT '扫描路径',
  `status` int(11) DEFAULT NULL COMMENT '扫描状态：0-待执行，1-执行中，2-完成，3-失败',
  `endpoint_count` int(11) DEFAULT NULL COMMENT '接口数量',
  `issue_count` int(11) DEFAULT NULL COMMENT '问题数量',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称',
  `model_params` text DEFAULT NULL COMMENT '模型参数',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描任务表';

-- ----------------------------
-- Table structure for srs_endpoint
-- ----------------------------
DROP TABLE IF EXISTS `srs_endpoint`;
CREATE TABLE `srs_endpoint` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `task_id` varchar(32) DEFAULT NULL COMMENT '扫描任务ID',
  `controller_name` varchar(255) DEFAULT NULL COMMENT '控制器类名',
  `controller_path` varchar(500) DEFAULT NULL COMMENT '控制器路径',
  `method_name` varchar(255) DEFAULT NULL COMMENT '方法名',
  `http_method` varchar(20) DEFAULT NULL COMMENT 'HTTP方法',
  `path` varchar(500) DEFAULT NULL COMMENT '接口路径',
  `logic_description` text DEFAULT NULL COMMENT '逻辑说明',
  `code_snippet` text DEFAULT NULL COMMENT '代码片段',
  `doc_comment` text DEFAULT NULL COMMENT 'Javadoc注释',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口表';

-- ----------------------------
-- Table structure for srs_parameter
-- ----------------------------
DROP TABLE IF EXISTS `srs_parameter`;
CREATE TABLE `srs_parameter` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `endpoint_id` varchar(32) DEFAULT NULL COMMENT '接口ID',
  `param_type` int(11) DEFAULT NULL COMMENT '参数类型：0-入参，1-出参',
  `field_name` varchar(255) DEFAULT NULL COMMENT '字段名',
  `field_type` varchar(100) DEFAULT NULL COMMENT '字段类型',
  `required` tinyint(1) DEFAULT NULL COMMENT '是否必填',
  `default_value` varchar(255) DEFAULT NULL COMMENT '默认值',
  `min_length` int(11) DEFAULT NULL COMMENT '最小长度',
  `max_length` int(11) DEFAULT NULL COMMENT '最大长度',
  `pattern` varchar(255) DEFAULT NULL COMMENT '正则表达式',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_endpoint_id` (`endpoint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数表';

-- ----------------------------
-- Table structure for srs_call_chain
-- ----------------------------
DROP TABLE IF EXISTS `srs_call_chain`;
CREATE TABLE `srs_call_chain` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `endpoint_id` varchar(32) DEFAULT NULL COMMENT '接口ID',
  `level` int(11) DEFAULT NULL COMMENT '调用层级',
  `call_type` int(11) DEFAULT NULL COMMENT '调用类型：0-Controller，1-Service，2-Mapper，3-Other',
  `class_name` varchar(255) DEFAULT NULL COMMENT '类名',
  `method_name` varchar(255) DEFAULT NULL COMMENT '方法名',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_endpoint_id` (`endpoint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调用链表';

-- ----------------------------
-- Table structure for srs_issue
-- ----------------------------
DROP TABLE IF EXISTS `srs_issue`;
CREATE TABLE `srs_issue` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `endpoint_id` varchar(32) DEFAULT NULL COMMENT '接口ID',
  `issue_type` int(11) DEFAULT NULL COMMENT '问题类型：0-缺少@Operation注解，1-模型生成失败，2-其他',
  `description` varchar(500) DEFAULT NULL COMMENT '问题描述',
  `class_name` varchar(255) DEFAULT NULL COMMENT '所在类',
  `line_number` int(11) DEFAULT NULL COMMENT '所在行',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_endpoint_id` (`endpoint_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题表';
