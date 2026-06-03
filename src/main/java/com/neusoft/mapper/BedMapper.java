package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    // 统计空闲床位（status=0）
    @Select("SELECT COUNT(*) FROM bed WHERE status = 0 AND is_deleted = 0")
    int countAvailableBeds();

    // 👇 新增：统计已占用床位（status=1）
    @Select("SELECT COUNT(*) FROM bed WHERE status = 1 AND is_deleted = 0")
    int countOccupiedBeds();

    // 根据床位号查询床位
    @Select("SELECT * FROM bed WHERE bed_no = #{bedNo} AND is_deleted = 0")
    Bed selectByBedNo(String bedNo);
}