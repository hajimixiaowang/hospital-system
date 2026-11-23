package com.hospital.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 医生公开信息查询（患者端医生列表/详情、统计） */
@Mapper
public interface DoctorQueryMapper {

    /** 患者端医生列表（含评分、累计挂号量） */
    @Select("SELECT d.id, d.title, d.intro, d.good_at AS goodAt, d.title_fee AS titleFee, " +
            "u.real_name AS doctorName, dept.id AS deptId, dept.name AS deptName, dept.base_fee AS baseFee, " +
            "(SELECT COUNT(*) FROM review WHERE doctor_id = d.id) AS reviewCount, " +
            "(SELECT COALESCE(AVG(rating), 0) FROM review WHERE doctor_id = d.id) AS avgRating " +
            "FROM doctor d " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "ORDER BY dept.id, d.id")
    List<Map<String, Object>> listPublicDoctors();

    /** 医生详情 */
    @Select("SELECT d.id, d.title, d.intro, d.good_at AS goodAt, d.title_fee AS titleFee, " +
            "u.real_name AS doctorName, u.phone, dept.id AS deptId, dept.name AS deptName, dept.base_fee AS baseFee, " +
            "(SELECT COUNT(*) FROM review WHERE doctor_id = d.id) AS reviewCount, " +
            "(SELECT COALESCE(AVG(rating), 0) FROM review WHERE doctor_id = d.id) AS avgRating " +
            "FROM doctor d " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE d.id = #{id}")
    Map<String, Object> findPublicDoctor(@Param("id") Long id);

    /** 医生端：今日接诊统计 */
    /** 医生端：今日接诊统计 */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM visit_record vr JOIN doctor d2 ON vr.doctor_id = d2.id " +
            "   WHERE d2.user_id = #{doctorUserId} AND DATE(vr.finish_time) = CURRENT_DATE()) AS visitedToday, " +
            "(SELECT COUNT(*) FROM registration r JOIN schedule s ON r.schedule_id = s.id " +
            "   JOIN doctor d3 ON s.doctor_id = d3.id " +
            "   WHERE d3.user_id = #{doctorUserId} AND s.work_date = CURRENT_DATE() AND r.status IN ('BOOKED','PAID')) AS waitingToday, " +
            "(SELECT COALESCE(SUM(r.fee),0) FROM registration r JOIN schedule s ON r.schedule_id = s.id " +
            "   JOIN doctor d4 ON s.doctor_id = d4.id " +
            "   WHERE d4.user_id = #{doctorUserId} AND r.status IN ('PAID','VISITED')) AS incomeTotal")
    Map<String, Object> doctorTodayStats(@Param("doctorUserId") Long doctorUserId);

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM registration WHERE DATE(create_time) = CURRENT_DATE()) AS todayRegs, " +
            "(SELECT COUNT(*) FROM registration) AS totalRegs, " +
            "(SELECT COALESCE(SUM(fee),0) FROM registration WHERE status IN ('PAID','VISITED')) AS totalIncome, " +
            "(SELECT COUNT(*) FROM user WHERE role = 'PATIENT') AS totalPatients, " +
            "(SELECT COUNT(*) FROM doctor) AS totalDoctors")
    Map<String, Object> dashboardOverview();

    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS cnt " +
            "FROM registration " +
            "WHERE create_time >= DATE_SUB(CURRENT_DATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY day")
    List<Map<String, Object>> regTrend7Days();
}
