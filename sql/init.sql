-- =====================================================
-- 东软颐养中心管理系统 - 数据库初始化脚本
-- 数据库：yiyang
-- 字符集：utf8mb4
-- 生成时间：2026-05-28
-- =====================================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS `yiyang`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `yiyang`;

-- =====================================================
-- 1. 管理员表
-- =====================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '管理员账号',
    `password`    VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt加密）',
    `role`        VARCHAR(20)  DEFAULT 'admin'         COMMENT '角色：admin-超级管理员',
    `status`      TINYINT(1)   DEFAULT 1               COMMENT '状态：0-禁用 1-启用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';


-- =====================================================
-- 2. 老人（客户）表
-- =====================================================
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
    `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`           VARCHAR(50)  DEFAULT NULL           COMMENT '老人姓名',
    `age`            INT          DEFAULT NULL           COMMENT '年龄',
    `gender`         VARCHAR(10)  DEFAULT NULL           COMMENT '性别：男/女',
    `bed_id`         INT          DEFAULT NULL           COMMENT '床位ID',
    `phone`          VARCHAR(20)  DEFAULT NULL           COMMENT '联系电话',
    `tags`           VARCHAR(255) DEFAULT NULL           COMMENT '标签（逗号分隔）',
    `avatar`         VARCHAR(255) DEFAULT NULL           COMMENT '头像路径',
    `report`         VARCHAR(255) DEFAULT NULL           COMMENT '体检报告路径',
    `check_in_date`  DATE         DEFAULT NULL           COMMENT '入住日期',
    `check_out_time` DATETIME     DEFAULT NULL           COMMENT '退住时间',
    `go_out_start`   DATETIME     DEFAULT NULL           COMMENT '外出开始时间',
    `go_out_end`     DATETIME     DEFAULT NULL           COMMENT '外出预计返回时间',
    `is_outing`      TINYINT(1)   DEFAULT 0              COMMENT '是否外出：0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_bed_id` (`bed_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人（客户）表';


-- =====================================================
-- 3. 床位表
-- =====================================================
DROP TABLE IF EXISTS `bed`;
CREATE TABLE `bed` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_number` VARCHAR(20) DEFAULT NULL           COMMENT '房间号',
    `bed_number`  VARCHAR(20) DEFAULT NULL           COMMENT '床位号',
    `status`      TINYINT(1)  DEFAULT 0              COMMENT '状态：0-空闲 1-占用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_bed` (`room_number`, `bed_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位表';


-- =====================================================
-- 4. 床位历史记录表
-- =====================================================
DROP TABLE IF EXISTS `bed_history`;
CREATE TABLE `bed_history` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bed_id`      INT          DEFAULT NULL           COMMENT '床位ID',
    `customer_id` INT          DEFAULT NULL           COMMENT '老人ID',
    `start_time`  DATETIME     DEFAULT NULL           COMMENT '开始时间',
    `end_time`    DATETIME     DEFAULT NULL           COMMENT '结束时间',
    `type`        VARCHAR(20)  DEFAULT NULL           COMMENT '类型：入住/退住/换床',
    `remark`      VARCHAR(255) DEFAULT NULL           COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_bed_id` (`bed_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位历史记录表';


-- =====================================================
-- 5. 家属信息表
-- =====================================================
DROP TABLE IF EXISTS `customer_family`;
CREATE TABLE `customer_family` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` INT         DEFAULT NULL           COMMENT '关联老人ID',
    `name`        VARCHAR(50) DEFAULT NULL           COMMENT '家属姓名',
    `phone`       VARCHAR(20) DEFAULT NULL           COMMENT '家属电话',
    `relation`    VARCHAR(20) DEFAULT NULL           COMMENT '与老人关系：子女/配偶/其他',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家属信息表';


-- =====================================================
-- 6. 老人护理等级关联表
-- =====================================================
DROP TABLE IF EXISTS `customer_nursing_level`;
CREATE TABLE `customer_nursing_level` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` INT          DEFAULT NULL           COMMENT '老人ID',
    `level_id`    INT          DEFAULT NULL           COMMENT '护理等级ID',
    `start_date`  VARCHAR(20)  DEFAULT NULL           COMMENT '开始日期',
    `remark`      VARCHAR(255) DEFAULT NULL           COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人护理等级关联表';


-- =====================================================
-- 7. 护理等级表
-- =====================================================
DROP TABLE IF EXISTS `nursing_level`;
CREATE TABLE `nursing_level` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name`  VARCHAR(50)  DEFAULT NULL           COMMENT '等级名称（一级/二级/三级/特级）',
    `description` VARCHAR(255) DEFAULT NULL           COMMENT '等级描述',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理等级表';


-- =====================================================
-- 8. 护理项目表
-- =====================================================
DROP TABLE IF EXISTS `nurse_item`;
CREATE TABLE `nurse_item` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_name`   VARCHAR(100) DEFAULT NULL           COMMENT '项目名称',
    `category`    VARCHAR(50)  DEFAULT NULL           COMMENT '项目分类',
    `price`       INT          DEFAULT 0              COMMENT '价格（分）',
    `unit`        VARCHAR(20)  DEFAULT NULL           COMMENT '单位：次/天/月',
    `description` VARCHAR(255) DEFAULT NULL           COMMENT '项目描述',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理项目表';


-- =====================================================
-- 9. 护理等级-项目关联表
-- =====================================================
DROP TABLE IF EXISTS `nursing_level_item`;
CREATE TABLE `nursing_level_item` (
    `id`       INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_id` INT DEFAULT NULL           COMMENT '护理等级ID',
    `item_id`  INT DEFAULT NULL           COMMENT '护理项目ID',
    PRIMARY KEY (`id`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理等级-项目关联表';


-- =====================================================
-- 10. 护工（护理员）表
-- =====================================================
DROP TABLE IF EXISTS `nurse`;
CREATE TABLE `nurse` (
    `id`               INT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`             VARCHAR(50) DEFAULT NULL           COMMENT '护工姓名',
    `phone`            VARCHAR(20) DEFAULT NULL           COMMENT '联系电话',
    `nursing_level_id` INT         DEFAULT NULL           COMMENT '所属护理等级ID',
    `status`           TINYINT(1)  DEFAULT 1              COMMENT '状态：1-在职 0-离职',
    PRIMARY KEY (`id`),
    KEY `idx_nursing_level_id` (`nursing_level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护工（护理员）表';


-- =====================================================
-- 11. 护工-老人分配表
-- =====================================================
DROP TABLE IF EXISTS `nurse_customer`;
CREATE TABLE `nurse_customer` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `nurse_id`    INT          DEFAULT NULL           COMMENT '护工ID',
    `customer_id` INT          DEFAULT NULL           COMMENT '老人ID',
    `assign_date` VARCHAR(20)  DEFAULT NULL           COMMENT '分配日期',
    `remark`      VARCHAR(255) DEFAULT NULL           COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_nurse_id` (`nurse_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护工-老人分配表';


-- =====================================================
-- 12. 护理记录表
-- =====================================================
DROP TABLE IF EXISTS `nursing_record`;
CREATE TABLE `nursing_record` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` INT          DEFAULT NULL           COMMENT '老人ID',
    `nurse_id`    INT          DEFAULT NULL           COMMENT '护工ID',
    `item_id`     INT          DEFAULT NULL           COMMENT '护理项目ID',
    `record_date` DATE         DEFAULT NULL           COMMENT '记录日期',
    `record_time` TIME         DEFAULT NULL           COMMENT '记录时间',
    `content`     VARCHAR(500) DEFAULT NULL           COMMENT '护理内容',
    `remark`      VARCHAR(255) DEFAULT NULL           COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理记录表';


-- =====================================================
-- 13. 登录日志表
-- =====================================================
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `admin_id`   INT          DEFAULT NULL           COMMENT '管理员ID',
    `username`   VARCHAR(50)  DEFAULT NULL           COMMENT '登录账号',
    `login_time` DATETIME     DEFAULT NULL           COMMENT '登录时间',
    `ip`         VARCHAR(50)  DEFAULT NULL           COMMENT '登录IP',
    `user_agent` VARCHAR(255) DEFAULT NULL           COMMENT '浏览器UA',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';


-- =====================================================
-- 14. 提醒消息表
-- =====================================================
DROP TABLE IF EXISTS `reminder_message`;
CREATE TABLE `reminder_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(100) DEFAULT NULL           COMMENT '消息标题',
    `content`     VARCHAR(500) DEFAULT NULL           COMMENT '消息内容',
    `receiver`    VARCHAR(50)  DEFAULT NULL           COMMENT '接收人',
    `type`        INT          DEFAULT 0              COMMENT '消息类型：0-系统 1-外出提醒 2-护理提醒',
    `is_read`     TINYINT(1)   DEFAULT 0              COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME     DEFAULT NULL           COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver` (`receiver`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒消息表';


-- =====================================================
-- 15. 健康档案表
-- =====================================================
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id`        BIGINT       DEFAULT NULL           COMMENT '老人ID',
    `systolic_pressure`  INT          DEFAULT NULL           COMMENT '收缩压（高压）',
    `diastolic_pressure` INT          DEFAULT NULL           COMMENT '舒张压（低压）',
    `blood_sugar`        DOUBLE       DEFAULT NULL           COMMENT '血糖值',
    `measure_time`       DATETIME     DEFAULT NULL           COMMENT '测量时间',
    `recorder_id`        BIGINT       DEFAULT NULL           COMMENT '记录人ID',
    `remark`             VARCHAR(255) DEFAULT NULL           COMMENT '备注',
    `create_time`        DATETIME     DEFAULT NULL           COMMENT '创建时间',
    `update_time`        DATETIME     DEFAULT NULL           COMMENT '更新时间',
    `is_deleted`         TINYINT(1)   DEFAULT 0              COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_measure_time` (`measure_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康档案表';


-- =====================================================
-- 16. 系统用户表（非管理员用户）
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`       INT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT NULL           COMMENT '用户名',
    `role`     VARCHAR(20) DEFAULT 'nurse'        COMMENT '角色：admin/nurse',
    `status`   TINYINT(1)  DEFAULT 1              COMMENT '状态：1-启用 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';


-- =====================================================
-- 初始化数据：默认管理员
-- 密码：admin123（BCrypt 加密）
-- =====================================================
INSERT INTO `admin` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'admin', 1);


-- =====================================================
-- 初始化数据：示例护理等级
-- =====================================================
INSERT INTO `nursing_level` (`level_name`, `description`) VALUES
('一级护理', '生活完全自理，仅需日常巡查'),
('二级护理', '生活部分自理，需协助日常生活'),
('三级护理', '生活不能自理，需全天候护理'),
('特级护理', '危重病人，需24小时专人监护');


-- =====================================================
-- 初始化数据：示例护理项目
-- =====================================================
INSERT INTO `nurse_item` (`item_name`, `category`, `price`, `unit`, `description`) VALUES
('血压测量', '健康监测', 1000, '次', '每日血压测量并记录'),
('血糖测量', '健康监测', 800, '次', '血糖检测'),
('协助进食', '生活护理', 2000, '次', '协助老人进食'),
('翻身拍背', '生活护理', 1500, '次', '预防褥疮护理'),
('药物管理', '医疗护理', 3000, '天', '按时给药并记录'),
('康复训练', '康复护理', 5000, '次', '协助康复运动训练'),
('卫生清洁', '生活护理', 2000, '天', '个人卫生护理'),
('心理疏导', '精神护理', 1500, '次', '心理健康关怀');


-- =====================================================
-- 初始化数据：示例床位
-- =====================================================
INSERT INTO `bed` (`room_number`, `bed_number`, `status`) VALUES
('101', 'A', 0), ('101', 'B', 0), ('101', 'C', 0),
('102', 'A', 0), ('102', 'B', 0), ('102', 'C', 0),
('201', 'A', 0), ('201', 'B', 0), ('201', 'C', 0),
('202', 'A', 0), ('202', 'B', 0), ('202', 'C', 0),
('301', 'A', 0), ('301', 'B', 0), ('301', 'C', 0);
