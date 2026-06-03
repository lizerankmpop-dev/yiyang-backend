package com.neusoft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neusoft.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    // ========== 原有统计方法保留 ==========
    @Select("SELECT COUNT(*) FROM customer WHERE check_out_time IS NULL AND is_outing = 0")
    Long selectInHouseCount();

    @Select("SELECT COUNT(*) FROM customer WHERE check_out_time IS NULL AND is_outing = 1")
    Long countOutingCustomers();

    @Select("SELECT gender AS name, COUNT(*) AS value FROM customer WHERE is_deleted = 0 AND is_check_in = 1 GROUP BY gender")
    List<Map<String, Object>> countGenderDistribution();

    @Select("SELECT '60岁以下' AS name, COUNT(*) AS value FROM customer WHERE is_deleted = 0 AND is_check_in = 1 AND age < 60 " +
            "UNION ALL " +
            "SELECT '60-70岁' AS name, COUNT(*) AS value FROM customer WHERE is_deleted = 0 AND is_check_in = 1 AND age BETWEEN 60 AND 69 " +
            "UNION ALL " +
            "SELECT '70-80岁' AS name, COUNT(*) AS value FROM customer WHERE is_deleted = 0 AND is_check_in = 1 AND age BETWEEN 70 AND 79 " +
            "UNION ALL " +
            "SELECT '80岁以上' AS name, COUNT(*) AS value FROM customer WHERE is_deleted = 0 AND is_check_in = 1 AND age >= 80")
    List<Map<String, Object>> countAgeDistribution();

    @Select("SELECT COALESCE(nl.level_name, '未分配级别') AS name, COUNT(c.id) AS value " +
            "FROM customer c LEFT JOIN nursing_level nl ON c.level_id = nl.id " +
            "WHERE c.is_deleted = 0 AND c.is_check_in = 1 " +
            "GROUP BY nl.id, nl.level_name")
    List<Map<String, Object>> countNursingLevelDistribution();

    @Select("SELECT COUNT(*) FROM customer WHERE DATE(check_in_date) = #{date}")
    Integer countInByDate(@Param("date") String date);

    @Select("SELECT COUNT(*) FROM customer WHERE DATE(check_out_time) = #{date}")
    Integer countOutByDate(@Param("date") String date);

    // ========== 修复客户列表联表查询（核心） ==========
    @Select({
            "<script>",
            "SELECT c.*, b.bed_no AS bedNo, nl.level_name AS nurseLevel, ",
            "(SELECT cf.phone FROM customer_family cf WHERE cf.customer_id = c.id ORDER BY cf.id ASC LIMIT 1) AS emergencyContactPhone ",
            "FROM customer c ",
            "LEFT JOIN bed b ON c.bed_id = b.id ",
            "LEFT JOIN nursing_level nl ON c.level_id = nl.id ",
            "WHERE c.is_deleted = 0 ",
            "<if test='name != null and name != \"\"'>",
            "  AND c.name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "ORDER BY c.id DESC",
            "</script>"
    })
    List<Customer> selectCustomerList(@Param("name") String name);

    // 根据床位ID查询占用客户
    @Select("SELECT c.*, b.bed_no AS bedNo, nl.level_name AS nurseLevel " +
            "FROM customer c " +
            "LEFT JOIN bed b ON c.bed_id = b.id " +
            "LEFT JOIN nursing_level nl ON c.level_id = nl.id " +
            "WHERE c.bed_id = #{bedId} AND c.is_check_in = 1")
    Customer selectByBedId(@Param("bedId") Integer bedId);

    // 查询所有在住客户（不分页，供退住申请等下拉框使用）
    @Select("SELECT c.id, c.name, c.age, c.gender, c.level_id AS levelId, " +
            "c.check_in_date AS checkInDate, c.is_check_in AS isCheckIn, " +
            "b.bed_no AS bedNo, nl.level_name AS nurseLevel " +
            "FROM customer c " +
            "LEFT JOIN bed b ON c.bed_id = b.id " +
            "LEFT JOIN nursing_level nl ON c.level_id = nl.id " +
            "WHERE c.is_deleted = 0 AND c.is_check_in = 1 " +
            "ORDER BY c.name ASC")
    List<Customer> selectAllCheckedIn();

    // 统计截止某日的在住客户总数（已入住且未被退住）
    @Select("SELECT COUNT(*) FROM customer WHERE is_deleted = 0 AND is_check_in = 1 AND check_in_date <= #{date}")
    Integer countInHouseByDate(@Param("date") String date);
}