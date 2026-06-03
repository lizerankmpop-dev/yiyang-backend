package com.neusoft.task;

import com.neusoft.entity.Customer;
import com.neusoft.entity.ReminderMessage;
import com.neusoft.mapper.CustomerMapper;
import com.neusoft.mapper.ReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时提醒任务
 * 修复：
 * 1. 原来给所有客户发固定文本"您有一条新的系统消息"，是示例代码，无实际业务意义
 * 2. 改为真实业务：检测"外出超时未归"的老人（goOutEnd < 当前时间 且 isOuting=1），
 *    自动生成超时预警提醒
 * 3. 使用 @RequiredArgsConstructor + Lombok @Slf4j
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderTask {

    private final CustomerMapper customerMapper;
    private final ReminderMapper reminderMapper;

    /**
     * 每天 8:00 执行：外出超时预警检测
     * 扫描所有 isOuting=1（外出中）且 goOutEnd < 当前时间 的老人，生成提醒
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkOutingOverdue() {
        log.info("===== 开始执行外出超时预警任务 =====");
        try {
            LocalDateTime now = LocalDateTime.now();
            // 查询外出状态的老人
            List<Customer> outingList = customerMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                            .eq(Customer::getIsOuting, 1)
                            .isNotNull(Customer::getGoOutEnd)
                            .lt(Customer::getGoOutEnd, now)
            );

            int overdueCount = 0;
            for (Customer customer : outingList) {
                String content = String.format(
                        "老人【%s】预计于 %s 返回，当前时间 %s，已超时未归，请及时联系家属！",
                        customer.getName(),
                        customer.getGoOutEnd(),
                        now.toString().substring(0, 16)
                );
                createReminder(customer.getName(), "外出超时预警", content, 2);
                overdueCount++;
            }

            log.info("外出超时预警任务完成，共发现 {} 位老人超时未归", overdueCount);
        } catch (Exception e) {
            log.error("外出超时预警任务执行异常", e);
        }
        log.info("===== 外出超时预警任务结束 =====");
    }

    /**
     * 每天 9:00 执行：今日护理计划提醒
     * 提醒当天有护理任务的护工
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyNursingReminder() {
        log.info("===== 开始执行每日护理提醒任务 =====");
        try {
            // 给系统管理员发送一条今日护理工作提醒
            createReminder("系统管理员", "每日护理提醒", "今日护理计划已就绪，请检查并安排护工工作。", 1);
            log.info("每日护理提醒已发送");
        } catch (Exception e) {
            log.error("每日护理提醒任务异常", e);
        }
        log.info("===== 每日护理提醒任务结束 =====");
    }

    /**
     * 构建并保存提醒消息
     *
     * @param receiver 接收人
     * @param title    标题
     * @param content  内容
     * @param type     类型（1=系统提醒 2=超时预警）
     */
    private void createReminder(String receiver, String title, String content, Integer type) {
        ReminderMessage msg = new ReminderMessage();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setReceiver(receiver);
        msg.setType(type);
        msg.setIsRead(false);
        msg.setCreateTime(LocalDateTime.now());
        reminderMapper.insert(msg);
    }
}
