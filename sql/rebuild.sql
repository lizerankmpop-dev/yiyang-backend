-- =====================================================
-- 东软颐养中心管理系统 - 数据库重建脚本
-- 清空旧表，按最新代码结构重建
-- =====================================================

USE `yiyang`;

-- 禁用外键检查（防止删除顺序问题）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有旧表
DROP TABLE IF EXISTS `admin`;
DROP TABLE IF EXISTS `backdown`;
DROP TABLE IF EXISTS `bed`;
DROP TABLE IF EXISTS `bed_history`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `customer_family`;
DROP TABLE IF EXISTS `customer_nurse_item`;
DROP TABLE IF EXISTS `customer_nursing_level`;
DROP TABLE IF EXISTS `food`;
DROP TABLE IF EXISTS `health_record`;
DROP TABLE IF EXISTS `login_log`;
DROP TABLE IF EXISTS `meal`;
DROP TABLE IF EXISTS `meal_food`;
DROP TABLE IF EXISTS `menu`;
DROP TABLE IF EXISTS `nurse`;
DROP TABLE IF EXISTS `nurse_customer`;
DROP TABLE IF EXISTS `nurse_item`;
DROP TABLE IF EXISTS `nursing_level`;
DROP TABLE IF EXISTS `nursing_level_item`;
DROP TABLE IF EXISTS `nursing_record`;
DROP TABLE IF EXISTS `nursing_task`;
DROP TABLE IF EXISTS `outward`;
DROP TABLE IF EXISTS `preference`;
DROP TABLE IF EXISTS `reminder_message`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `role_menu`;
DROP TABLE IF EXISTS `room`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `nursecontent`;
DROP TABLE IF EXISTS `nurselevel`;
DROP TABLE IF EXISTS `nurselevelitem`;
DROP TABLE IF EXISTS `nurserecord`;

SET FOREIGN_KEY_CHECKS = 1;
