package com.hospital.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /** 管理端：医生列表联查（含账号姓名与科室名） */
    @Select("SELECT d.id, d.title, d.title_fee AS titleFee, d.dept_id AS deptId, " +
            "u.real_name AS doctorName, u.username, u.phone, dept.name AS deptName " +
            "FROM doctor d JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "ORDER BY dept.id, d.id")
    List<Map<String, Object>> listDoctorViews();

    /** 医生端：按登录用户 id 查自己的医生档案 */
    @Select("SELECT d.* FROM doctor d WHERE d.user_id = #{userId} LIMIT 1")
    Doctor findByUserId(@Param("userId") Long userId);

    /** 医生端：查看自己的排班（号源）列表，按日期倒序 */
    @Select("SELECT s.id, s.source_no AS sourceNo, s.work_date AS workDate, s.time_slot AS timeSlot, " +
            "s.total_count AS totalCount, s.remain_count AS remainCount, s.status " +
            "FROM schedule s WHERE s.doctor_id = #{doctorId} " +
            "ORDER BY s.work_date DESC, s.time_slot LIMIT 60")
    List<Map<String, Object>> listMySchedules(@Param("doctorId") Long doctorId);

    /** 管理端统计：每科室挂号量 / 就诊量 / 挂号收入 */
    @Select("SELECT dept.name AS deptName, " +
            "       COUNT(r.id) AS regCount, " +
            "       SUM(CASE WHEN r.status = 'VISITED' THEN 1 ELSE 0 END) AS visitedCount, " +
            "       COALESCE(SUM(r.fee), 0) AS totalFee " +
            "FROM department dept " +
            "LEFT JOIN doctor d ON d.dept_id = dept.id " +
            "LEFT JOIN schedule s ON s.doctor_id = d.id " +
            "LEFT JOIN registration r ON r.schedule_id = s.id " +
            "GROUP BY dept.id, dept.name ORDER BY dept.id")
    List<Map<String, Object>> countDeptStats();
}
