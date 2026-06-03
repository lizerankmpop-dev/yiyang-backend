package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.ReminderMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReminderMapper extends BaseMapper<ReminderMessage> {

    // 插入一条新的提醒消息
    @Insert("INSERT INTO reminder_message(title, content, receiver, type, is_read, create_time) " +
            "VALUES(#{title}, #{content}, #{receiver}, #{type}, #{isRead}, #{createTime})")
    void insertReminder(ReminderMessage message);

    // 根据接收人查询所有消息
    @Select("SELECT * FROM reminder_message WHERE receiver = #{receiver} ORDER BY create_time DESC")
    List<ReminderMessage> selectByReceiver(@Param("receiver") String receiver);

    // 查询指定接收人的未读消息
    @Select("SELECT * FROM reminder_message WHERE receiver = #{receiver} AND is_read = 0 ORDER BY create_time DESC")
    List<ReminderMessage> selectUnreadByReceiver(@Param("receiver") String receiver);

    // 给第二个文件兼容方法名
    @Select("SELECT * FROM reminder_message WHERE receiver = #{receiver} AND is_read = 0 ORDER BY create_time DESC")
    List<ReminderMessage> getUnreadReminders(@Param("receiver") String receiver);

    // 统计未读消息数量
    @Select("SELECT COUNT(*) FROM reminder_message WHERE receiver = #{receiver} AND is_read = 0")
    Integer countUnreadByReceiver(@Param("receiver") String receiver);

    // 标记单条为已读
    @Update("UPDATE reminder_message SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    // 标记全部为已读
    @Update("UPDATE reminder_message SET is_read = 1 WHERE receiver = #{receiver} AND is_read = 0")
    int markAllAsRead(@Param("receiver") String receiver);
}