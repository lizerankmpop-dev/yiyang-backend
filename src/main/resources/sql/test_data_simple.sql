-- 东软颐养中心 - 扩充测试数据
-- 使用方法：在 MySQL Workbench 左侧双击选中 yiyang 数据库，然后执行此脚本

USE yiyang;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 房间
INSERT IGNORE INTO room (id, building_no, room_no, floor, room_type, bed_count, has_bathroom, remark) VALUES
(4, 'A', '103', 1, 'Double', 2, 1, 'Near nurse station'),
(5, 'A', '104', 1, 'Single', 1, 1, 'VIP room'),
(6, 'A', '201', 2, 'Double', 2, 1, 'Quiet environment'),
(7, 'A', '202', 2, 'Double', 2, 1, NULL),
(8, 'A', '203', 2, 'Triple', 3, 1, 'Spacious'),
(9, 'A', '301', 3, 'Double', 2, 1, 'Top floor quiet'),
(10, 'A', '302', 3, 'Single', 1, 1, 'VIP single'),
(11, 'B', '101', 1, 'Double', 2, 1, 'Ground floor convenient'),
(12, 'B', '102', 1, 'Double', 2, 1, NULL),
(13, 'B', '202', 2, 'Triple', 3, 1, 'Large room'),
(14, 'B', '203', 2, 'Double', 2, 1, NULL),
(15, 'B', '301', 3, 'Double', 2, 1, 'Good sunlight'),
(16, 'B', '302', 3, 'Single', 1, 1, 'Premium single'),
(17, 'C', '101', 1, 'Double', 2, 1, 'New building'),
(18, 'C', '102', 1, 'Double', 2, 1, NULL),
(19, 'C', '201', 2, 'Double', 2, 1, NULL),
(20, 'C', '202', 2, 'Triple', 3, 1, 'Activity nearby');

-- 2. 床位
INSERT IGNORE INTO bed (id, bed_no, room_no, floor, status, type) VALUES
(6, '103-1', '103', '1', 0, 'Standard'),
(7, '103-2', '103', '1', 0, 'Standard'),
(8, '104-1', '104', '1', 1, 'VIP'),
(9, '201-2', '201', '2', 0, 'Standard'),
(10, '202-1', '202', '2', 1, 'Standard'),
(11, '202-2', '202', '2', 0, 'Standard'),
(12, '203-1', '203', '2', 1, 'Standard'),
(13, '203-2', '203', '2', 0, 'Standard'),
(14, '203-3', '203', '2', 0, 'Standard'),
(15, '301-1', '301', '3', 1, 'Standard'),
(16, '301-2', '301', '3', 0, 'Standard'),
(17, '302-1', '302', '3', 0, 'VIP'),
(18, 'B101-1', '101', '1', 0, 'Standard'),
(19, 'B101-2', '101', '1', 1, 'Standard'),
(20, 'B102-1', '102', '1', 0, 'Standard'),
(21, 'B102-2', '102', '1', 0, 'Standard'),
(22, 'B202-1', '202', '2', 1, 'Standard'),
(23, 'B202-2', '202', '2', 0, 'Standard'),
(24, 'B202-3', '202', '2', 0, 'Standard'),
(25, 'B203-1', '203', '2', 0, 'Standard'),
(26, 'B203-2', '203', '2', 1, 'Standard'),
(27, 'B301-1', '301', '3', 0, 'Standard'),
(28, 'B301-2', '301', '3', 0, 'Standard'),
(29, 'B302-1', '302', '3', 1, 'VIP'),
(30, 'C101-1', '101', '1', 0, 'Standard'),
(31, 'C101-2', '101', '1', 0, 'Standard'),
(32, 'C102-1', '102', '1', 1, 'Standard'),
(33, 'C102-2', '102', '1', 0, 'Standard'),
(34, 'C201-1', '201', '2', 0, 'Standard'),
(35, 'C201-2', '201', '2', 0, 'Standard'),
(36, 'C202-1', '202', '2', 1, 'Standard'),
(37, 'C202-2', '202', '2', 0, 'Standard'),
(38, 'C202-3', '202', '2', 0, 'Standard');

-- 3. 护工
INSERT IGNORE INTO nurse (id, name, phone, nursing_level_id, status) VALUES
(4, 'Zhao Liu', '13900139004', 2, 1),
(5, 'Sun Qi', '13900139005', 1, 1),
(6, 'Zhou Ba', '13900139006', 3, 1),
(7, 'Wu Jiu', '13900139007', 2, 1),
(8, 'Zheng Shi', '13900139008', 1, 1),
(9, 'Wang Shi', '13900139009', 2, 0),
(10, 'Feng Qi', '13900139010', 3, 1);

-- 4. 客户
INSERT IGNORE INTO customer (id, name, age, gender, bed_id, phone, check_in_date, level_id, is_check_in, is_outing, is_deleted) VALUES
(4, 'Liu Granny', 68, 'Female', 6, '13700137004', '2024-04-01', 1, 1, 0, 0),
(5, 'Chen Grandpa', 82, 'Male', 7, '13700137005', '2024-04-10', 2, 1, 0, 0),
(6, 'Yang Granny', 76, 'Female', 9, '13700137006', '2024-05-01', 3, 1, 0, 0),
(7, 'Huang Grandpa', 79, 'Male', 11, '13700137007', '2024-05-15', 2, 1, 0, 0),
(8, 'Zhao Granny', 71, 'Female', 13, '13700137008', '2024-06-01', 1, 1, 0, 0),
(9, 'Qian Grandpa', 85, 'Male', 15, '13700137009', '2024-06-10', 3, 1, 0, 0),
(10, 'Sun Granny', 73, 'Female', 16, '13700137010', '2024-07-01', 2, 1, 0, 0),
(11, 'Li Grandpa', 77, 'Male', 18, '13700137011', '2024-07-15', 1, 1, 0, 0),
(12, 'Zhou Granny', 80, 'Female', 20, '13700137012', '2024-08-01', 3, 1, 0, 0),
(13, 'Wu Grandpa', 74, 'Male', 21, '13700137013', '2024-08-15', 2, 1, 0, 0),
(14, 'Zheng Granny', 69, 'Female', 25, '13700137014', '2024-09-01', 1, 1, 0, 0),
(15, 'Wang Grandpa', 81, 'Male', 27, '13700137015', '2024-09-15', 3, 1, 0, 0),
(16, 'Feng Grandpa', 78, 'Male', 30, '13700137016', '2024-10-01', 2, 1, 1, 0),
(17, 'Chen Granny', 75, 'Female', 31, '13700137017', '2024-10-10', 1, 1, 1, 0),
(18, 'Chu Grandpa', 70, 'Male', NULL, '13700137018', NULL, 2, 0, 0, 0),
(19, 'Wei Granny', 72, 'Female', NULL, '13700137019', NULL, 1, 0, 0, 0),
(20, 'Jiang Grandpa', 76, 'Male', NULL, '13700137020', NULL, 3, 0, 0, 0);

-- 5. 客户家属
INSERT IGNORE INTO customer_family (id, customer_id, name, phone, relation) VALUES
(3, 4, 'Liu Qiang', '13800138040', 'Son'),
(4, 5, 'Chen Ming', '13800138050', 'Grandson'),
(5, 6, 'Yang Tao', '13800138060', 'Son'),
(6, 7, 'Huang Wei', '13800138070', 'Son'),
(7, 8, 'Zhao Gang', '13800138080', 'Son'),
(8, 9, 'Qian Xue', '13800138090', 'Granddaughter'),
(9, 10, 'Sun Li', '13800138100', 'Daughter'),
(10, 11, 'Li Peng', '13800138110', 'Son'),
(11, 12, 'Zhou Juan', '13800138120', 'Daughter'),
(12, 13, 'Wu Hao', '13800138130', 'Son'),
(13, 14, 'Zheng Lei', '13800138140', 'Son'),
(14, 15, 'Wang Fang', '13800138150', 'Granddaughter'),
(15, 16, 'Feng Yu', '13800138160', 'Son'),
(16, 17, 'Chen Jie', '13800138170', 'Daughter');

-- 6. 客户护理级别
INSERT IGNORE INTO customer_nursing_level (id, customer_id, level_id, start_date, remark) VALUES
(3, 4, 1, '2024-04-01', 'Self-care'),
(4, 5, 2, '2024-04-10', 'Needs assistance'),
(5, 6, 3, '2024-05-01', 'Full care'),
(6, 7, 2, '2024-05-15', 'Semi-care'),
(7, 8, 1, '2024-06-01', 'Self-care'),
(8, 9, 3, '2024-06-10', 'Full-care'),
(9, 10, 2, '2024-07-01', 'Semi-care'),
(10, 11, 1, '2024-07-15', 'Self-care'),
(11, 12, 3, '2024-08-01', 'Full-care'),
(12, 13, 2, '2024-08-15', 'Semi-care'),
(13, 14, 1, '2024-09-01', 'Self-care'),
(14, 15, 3, '2024-09-15', 'Full-care'),
(15, 16, 2, '2024-10-01', 'Semi-care, out'),
(16, 17, 1, '2024-10-10', 'Self-care, visiting');

-- 7. 护工客户关联
INSERT IGNORE INTO nurse_customer (id, nurse_id, customer_id, assign_date, remark) VALUES
(4, 1, 4, '2024-04-01', 'Primary'),
(5, 1, 7, '2024-05-15', 'Secondary'),
(6, 2, 2, '2024-02-20', 'Primary'),
(7, 2, 6, '2024-05-01', 'Full care'),
(8, 2, 9, '2024-06-10', 'Bedridden'),
(9, 2, 12, '2024-08-01', 'Full care'),
(10, 3, 3, '2024-03-10', 'Self-care'),
(11, 3, 8, '2024-06-01', 'Self-care'),
(12, 3, 11, '2024-07-15', 'Self-care'),
(13, 3, 14, '2024-09-01', 'Self-care'),
(14, 4, 5, '2024-04-10', 'Semi-care'),
(15, 4, 10, '2024-07-01', 'Semi-care'),
(16, 4, 13, '2024-08-15', 'Semi-care'),
(17, 4, 16, '2024-10-01', 'Semi-care, outing'),
(18, 5, 17, '2024-10-10', 'Self-care, visiting'),
(19, 6, 15, '2024-09-15', 'Full-care'),
(20, 7, 4, '2024-04-01', 'Co-caregiver'),
(21, 7, 7, '2024-05-15', 'Co-caregiver');

-- 8. 护理记录
INSERT IGNORE INTO nursing_record (id, nurse_id, customer_id, item_id, record_date, record_time, content) VALUES
(1, 1, 1, 1, '2024-10-25', '08:30:00', 'Bathing assistance'),
(2, 1, 1, 2, '2024-10-25', '12:00:00', 'Feeding assistance'),
(3, 2, 2, 1, '2024-10-25', '09:00:00', 'Bathing done'),
(4, 2, 6, 3, '2024-10-25', '10:00:00', 'Turning done'),
(5, 3, 3, 1, '2024-10-25', '08:00:00', 'Self bathing'),
(6, 1, 1, 1, '2024-10-26', '08:30:00', 'Bathing assistance'),
(7, 2, 2, 2, '2024-10-26', '12:00:00', 'Feeding assistance'),
(8, 2, 9, 3, '2024-10-26', '11:00:00', 'Turning and bathing'),
(9, 4, 5, 1, '2024-10-26', '09:30:00', 'Bathing assistance'),
(10, 4, 10, 2, '2024-10-26', '12:30:00', 'Feeding assistance'),
(11, 1, 4, 1, '2024-10-27', '08:00:00', 'Bathing done'),
(12, 3, 8, 1, '2024-10-27', '09:00:00', 'Self bathing OK'),
(13, 2, 12, 3, '2024-10-27', '10:30:00', 'Full care'),
(14, 6, 15, 3, '2024-10-27', '11:00:00', 'Full care turning'),
(15, 7, 4, 2, '2024-10-27', '12:00:00', 'Feeding assistance'),
(16, 1, 7, 1, '2024-10-28', '08:30:00', 'Bathing self'),
(17, 4, 13, 2, '2024-10-28', '12:00:00', 'Feeding assistance'),
(18, 3, 11, 1, '2024-10-28', '09:00:00', 'Self care OK'),
(19, 2, 6, 3, '2024-10-28', '10:00:00', 'Turning done'),
(20, 6, 15, 3, '2024-10-28', '11:30:00', 'Full care');

-- 9. 外出记录
INSERT IGNORE INTO outward (id, customer_id, go_out_date, expected_return_date, actual_return_date, reason, status, applicant_name) VALUES
(1, 16, '2024-10-20', '2024-10-27', NULL, 'Visiting family', 0, 'admin'),
(2, 17, '2024-10-22', '2024-10-29', NULL, 'Medical appointment', 0, 'admin'),
(3, 1, '2024-09-15', '2024-09-16', '2024-09-16', 'Hospital checkup', 1, 'admin'),
(4, 8, '2024-08-10', '2024-08-12', '2024-08-11', 'Family visit', 1, 'admin');

-- 10. 入住记录
INSERT IGNORE INTO check_in_record (id, customer_id, bed_id, check_in_time, remark) VALUES
(1, 1, 2, '2024-01-15 10:00:00', 'Initial'),
(2, 2, 4, '2024-02-20 10:00:00', NULL),
(3, 3, 5, '2024-03-10 10:00:00', NULL),
(4, 4, 6, '2024-04-01 10:00:00', NULL),
(5, 5, 7, '2024-04-10 10:00:00', NULL),
(6, 6, 9, '2024-05-01 10:00:00', 'Full care'),
(7, 7, 11, '2024-05-15 10:00:00', NULL),
(8, 8, 13, '2024-06-01 10:00:00', NULL),
(9, 9, 15, '2024-06-10 10:00:00', 'Bedridden'),
(10, 10, 16, '2024-07-01 10:00:00', NULL);

-- 11. 退住记录
INSERT IGNORE INTO backdown (id, customer_id, customer_name, applicant_id, applicant_name, apply_date, check_out_date, reason, backdown_type, status, auditor_id, auditor_name, audit_time, audit_remark, settlement_amount) VALUES
(1, 1, 'Zhang Grandpa', 1, 'admin', '2024-06-01', '2024-06-15', 'Family relocation', 1, 1, 1, 'admin', '2024-06-02 10:00:00', 'Approved', 5000.00),
(2, 3, 'Wang Grandpa', 1, 'admin', '2024-08-01', '2024-08-20', 'Health recovery', 2, 1, 1, 'admin', '2024-08-02 10:00:00', 'Approved', 3000.00),
(3, 5, 'Zhao Granny', 2, 'nurse', '2024-09-01', '2024-09-10', 'Family request', 1, 0, NULL, NULL, NULL, NULL, NULL),
(4, 7, 'Zhou Granny', 1, 'admin', '2024-10-01', '2024-10-05', 'Transfer to hospital', 2, 1, 1, 'admin', '2024-10-02 09:00:00', 'Approved', 2000.00);

-- 12. 健康档案
INSERT IGNORE INTO health_record (id, customer_id, systolic_pressure, diastolic_pressure, blood_sugar, measure_time, recorder_id, remark) VALUES
(1, 1, 140, 90, 5.2, '2024-10-01 09:00:00', 1, 'Stable'),
(2, 2, 150, 95, 6.1, '2024-10-05 09:00:00', 1, 'High BP'),
(3, 3, 130, 80, 4.8, '2024-10-10 09:00:00', 1, 'Good'),
(4, 4, 135, 85, 5.0, '2024-10-12 09:00:00', 1, NULL),
(5, 5, 145, 92, 5.5, '2024-10-15 09:00:00', 1, 'Monitor'),
(6, 6, 160, 100, 7.2, '2024-10-18 09:00:00', 1, 'High BP'),
(7, 7, 128, 78, 4.9, '2024-10-20 09:00:00', 1, 'Good'),
(8, 8, 138, 82, 5.1, '2024-10-22 09:00:00', 1, NULL),
(9, 9, 155, 98, 6.8, '2024-10-25 09:00:00', 1, 'Bedridden');

-- 13. 膳食
INSERT IGNORE INTO food (id, food_name, category, description, nutrition_info, status) VALUES
(1, 'Steamed Egg', 'High Protein', 'Easy digest', 'Protein 13g, Fat 10g', 1),
(2, 'Rice Porridge', 'Staple', 'Soft food', 'Carb 25g, Protein 2.5g', 1),
(3, 'Vegetable Soup', 'Soup', 'Low salt', 'Fiber 5g, Vit C 10mg', 1),
(4, 'Steamed Fish', 'Protein', 'Omega-3', 'Protein 25g, Fat 8g', 1),
(5, 'Tofu Dish', 'Protein', 'Vegetarian', 'Protein 10g, Calcium 150mg', 1),
(6, 'Milk', 'Dairy', 'High calcium', 'Protein 8g, Calcium 300mg', 1),
(7, 'Banana', 'Fruit', 'Potassium', 'Carb 23g, Potassium 400mg', 1),
(8, 'Oatmeal', 'Staple', 'High fiber', 'Fiber 8g, Protein 5g', 1);

INSERT IGNORE INTO meal (id, meal_type, meal_date, description, status) VALUES
(1, 'Breakfast', '2024-10-25', 'Rice porridge + milk', 1),
(2, 'Lunch', '2024-10-25', 'Steamed fish + vegetable soup', 1),
(3, 'Dinner', '2024-10-25', 'Steamed egg + banana', 1),
(4, 'Breakfast', '2024-10-26', 'Oatmeal + milk', 1),
(5, 'Lunch', '2024-10-26', 'Tofu dish + vegetable soup', 1),
(6, 'Dinner', '2024-10-26', 'Steamed fish + banana', 1),
(7, 'Breakfast', '2024-10-27', 'Rice porridge + steamed egg', 1);

-- 14. 排班
INSERT IGNORE INTO schedule (id, nurse_id, nurse_name, schedule_date, shift_type, duty_area, remark) VALUES
(1, 1, 'Zhang San', '2024-10-25', 'Morning', 'Floor A', NULL),
(2, 2, 'Li Si', '2024-10-25', 'Night', 'Floor B', NULL),
(3, 3, 'Wang Wu', '2024-10-25', 'Morning', 'Floor C', NULL),
(4, 4, 'Zhao Liu', '2024-10-25', 'Afternoon', 'Floor A', NULL),
(5, 1, 'Zhang San', '2024-10-26', 'Afternoon', 'Floor B', NULL),
(6, 2, 'Li Si', '2024-10-26', 'Morning', 'Floor A', NULL),
(7, 3, 'Wang Wu', '2024-10-26', 'Night', 'Floor B', NULL),
(8, 5, 'Sun Qi', '2024-10-26', 'Morning', 'Floor C', NULL),
(9, 1, 'Zhang San', '2024-10-27', 'Morning', 'Floor A', NULL),
(10, 6, 'Zhou Ba', '2024-10-27', 'Night', 'Floor C', NULL);

-- 15. 提醒消息
INSERT IGNORE INTO reminder_message (id, title, content, receiver, type, is_read) VALUES
(1, 'BP Check', 'Blood pressure check due for Zhang Grandpa', 'Nurse Zhang', 1, 0),
(2, 'Medication Review', 'Medication review for Li Grandpa', 'Nurse Li', 1, 0),
(3, 'Bathing Schedule', 'Bathing schedule for Yang Granny', 'Nurse Li', 1, 0),
(4, 'Turning Due', 'Turning due for Qian Grandpa', 'Nurse Li', 1, 0),
(5, 'Return Reminder', 'Expected return for Feng Grandpa', 'Nurse Zhao', 1, 0);

SET FOREIGN_KEY_CHECKS = 1;

-- 数据汇总
SELECT 'Customer' AS t, COUNT(*) AS c FROM customer
UNION ALL SELECT 'Bed', COUNT(*) FROM bed
UNION ALL SELECT 'Nurse', COUNT(*) FROM nurse
UNION ALL SELECT 'Room', COUNT(*) FROM room
UNION ALL SELECT 'NurseCustomer', COUNT(*) FROM nurse_customer
UNION ALL SELECT 'NursingRecord', COUNT(*) FROM nursing_record
UNION ALL SELECT 'Outward', COUNT(*) FROM outward
UNION ALL SELECT 'HealthRecord', COUNT(*) FROM health_record
UNION ALL SELECT 'Meal', COUNT(*) FROM meal
UNION ALL SELECT 'Food', COUNT(*) FROM food
UNION ALL SELECT 'Schedule', COUNT(*) FROM schedule
UNION ALL SELECT 'Reminder', COUNT(*) FROM reminder_message
UNION ALL SELECT 'Backdown', COUNT(*) FROM backdown;
