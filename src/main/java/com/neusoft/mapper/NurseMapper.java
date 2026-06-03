package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.Nurse;
import com.neusoft.entity.StatisticsData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NurseMapper extends BaseMapper<Nurse> {

    @Select("SELECT n.name, COUNT(c.id) AS value " +
            "FROM nurse n " +
            "LEFT JOIN customer c ON n.id = c.user_id " +
            "GROUP BY n.id, n.name " +
            "ORDER BY value DESC")
    List<StatisticsData> countCustomersByNurse();
}