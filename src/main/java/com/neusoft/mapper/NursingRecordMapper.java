package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.NursingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface NursingRecordMapper extends BaseMapper<NursingRecord> {

    // 统计今日记录数（你原来的代码，保留）
    @Select("SELECT COUNT(*) FROM nursing_record WHERE record_date = CURDATE()")
    Long selectTodayCount();

    // ↓↓↓ 新增这个方法！联表查询 + 姓名 + 分页 + 搜索
    @Select({
            "<script>",
            "SELECT nr.*, c.name AS customerName, n.name AS nurseName ",
            "FROM nursing_record nr ",
            "LEFT JOIN customer c ON nr.customer_id = c.id ",
            "LEFT JOIN nurse n ON nr.nurse_id = n.id ",
            "<if test='keyword != null and keyword != \"\"'>",
            "   WHERE c.name LIKE CONCAT('%', #{keyword}, '%') OR nr.content LIKE CONCAT('%', #{keyword}, '%') ",
            "</if>",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<NursingRecord> selectRecordList(
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    // 查询总数（分页用）
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM nursing_record nr ",
            "LEFT JOIN customer c ON nr.customer_id = c.id ",
            "<if test='keyword != null and keyword != \"\"'>",
            "   WHERE c.name LIKE CONCAT('%', #{keyword}, '%') OR nr.content LIKE CONCAT('%', #{keyword}, '%') ",
            "</if>",
            "</script>"
    })
    Long selectRecordCount(@Param("keyword") String keyword);
}