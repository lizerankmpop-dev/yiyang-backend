package com.neusoft.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckInRecordMapper {

    // 添加入住记录，对应Service中的addCheckInRecord方法
    @Insert("INSERT INTO check_in_record (bed_id, customer_id, check_in_time) " +
            "VALUES (#{bedId}, #{customerId}, NOW())")
    int addCheckInRecord(
            @Param("bedId") Integer bedId,
            @Param("customerId") Integer customerId
    );

    // 根据客户ID删除入住记录（删除客户时先清关联数据）
    @Delete("DELETE FROM check_in_record WHERE customer_id = #{customerId}")
    int deleteByCustomerId(@Param("customerId") Integer customerId);
}