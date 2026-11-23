package com.hospital.registration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    /** 患者端：我的挂号列表（联查科室/医生/号源信息） */
    @Select("SELECT r.id, r.patient_id AS patientId, r.reg_no AS regNo, r.reg_type AS regType, r.fee, r.status, r.create_time AS createTime, " +
            "s.work_date AS workDate, s.time_slot AS timeSlot, s.source_no AS sourceNo, " +
            "u.real_name AS doctorName, dept.name AS deptName, d.title " +
            "FROM registration r " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE r.patient_id = #{patientId} " +
            "ORDER BY r.create_time DESC")
    List<RegistrationView> listByPatient(@Param("patientId") Long patientId);

    /** 医生端：某医生名下的预约列表，急诊置顶 */
    @Select("SELECT r.id, r.patient_id AS patientId, r.reg_no AS regNo, r.reg_type AS regType, r.fee, r.status, r.create_time AS createTime, " +
            "s.work_date AS workDate, s.time_slot AS timeSlot, s.source_no AS sourceNo, " +
            "u.real_name AS patientName, u.phone AS patientPhone, u.patient_type AS patientType, " +
            "dept.name AS deptName, d.title " +
            "FROM registration r " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user u ON r.patient_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE d.user_id = #{doctorUserId} AND s.work_date = CURRENT_DATE() " +
            "ORDER BY CASE WHEN r.reg_type = 'EMERGENCY' THEN 0 ELSE 1 END, r.create_time")
    List<RegistrationView> listTodayByDoctorUser(@Param("doctorUserId") Long doctorUserId);

    /** 单条详情联查（就诊操作台用） */
    @Select("SELECT r.id, r.patient_id AS patientId, r.reg_no AS regNo, r.reg_type AS regType, r.fee, r.status, r.create_time AS createTime, " +
            "s.work_date AS workDate, s.time_slot AS timeSlot, s.source_no AS sourceNo, " +
            "u.real_name AS patientName, u.phone AS patientPhone, u.patient_type AS patientType, " +
            "dept.name AS deptName, d.title " +
            "FROM registration r " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user u ON r.patient_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE r.id = #{id}")
    RegistrationView findViewById(@Param("id") Long id);

    /** 管理端：全部挂号订单（按下单时间倒序），支持按状态筛选 */
    @Select("<script>SELECT r.id, r.patient_id AS patientId, r.reg_no AS regNo, r.reg_type AS regType, " +
            "r.fee, r.status, r.create_time AS createTime, s.work_date AS workDate, s.time_slot AS timeSlot, " +
            "u.real_name AS patientName, u.phone AS patientPhone, " +
            "du.real_name AS doctorName, dept.name AS deptName, d.title " +
            "FROM registration r " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user du ON d.user_id = du.id " +
            "JOIN user u ON r.patient_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "<if test='status != null and status != \"\"'> WHERE r.status = #{status} </if>" +
            "ORDER BY r.create_time DESC LIMIT 100</script>")
    java.util.List<RegistrationView> listAllOrders(@Param("status") String status);
}
