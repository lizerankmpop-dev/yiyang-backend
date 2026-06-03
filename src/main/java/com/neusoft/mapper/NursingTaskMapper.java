package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.NursingTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NursingTaskMapper extends BaseMapper<NursingTask> {

    // 联表查询：护工/老人/护理级别/护理项目 姓名
    @Select({
            "<script>",
            "SELECT t.*, n.name nurseName, c.name customerName, ",
            "l.level_name levelName, i.item_name itemName ",
            "FROM nursing_task t ",
            "LEFT JOIN nurse n ON t.nurse_id = n.id ",
            "LEFT JOIN customer c ON t.customer_id = c.id ",
            "LEFT JOIN nursing_level l ON t.level_id = l.id ",
            "LEFT JOIN nurse_item i ON t.item_id = i.id ",
            "<if test='keyword!=null and keyword!=\"\"'>",
            "   WHERE c.name LIKE CONCAT('%',#{keyword},'%') OR n.name LIKE CONCAT('%',#{keyword},'%') ",
            "</if>",
            "LIMIT #{offset},#{size}",
            "</script>"
    })
    List<NursingTask> selectTaskList(
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    // 更新任务状态
    @Update("UPDATE nursing_task SET status = #{status} WHERE id = #{id}")
    int updateTaskStatus(@Param("id") Integer id, @Param("status") String status);
}