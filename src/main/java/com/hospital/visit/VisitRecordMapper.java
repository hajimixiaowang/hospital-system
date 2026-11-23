package com.hospital.visit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisitRecordMapper extends BaseMapper<VisitRecord> {

    @Select("SELECT * FROM visit_record WHERE registration_id = #{registrationId} LIMIT 1")
    VisitRecord findByRegistrationId(@Param("registrationId") Long registrationId);

    /** 患者端：我的就诊记录（已完成的） */
    @Select("SELECT vr.registration_id AS registrationId, vr.diagnosis, vr.prescription, " +
            "vr.finish_time AS finishTime, r.reg_no AS regNo, r.fee, " +
            "s.work_date AS workDate, s.time_slot AS timeSlot, " +
            "u.real_name AS doctorName, dept.name AS deptName " +
            "FROM visit_record vr " +
            "JOIN registration r ON vr.registration_id = r.id " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON vr.doctor_id = d.id " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE r.patient_id = #{patientId} AND vr.finish_time IS NOT NULL " +
            "ORDER BY vr.finish_time DESC")
    List<java.util.Map<String, Object>> listFinishedByPatient(@Param("patientId") Long patientId);

    /** 医生端：我的接诊记录（进行中的，就诊操作台跳转用） */
    @Select("SELECT vr.registration_id AS registrationId, vr.diagnosis, r.reg_no AS regNo, " +
            "r.reg_type AS regType, r.status, u.real_name AS patientName, s.time_slot AS timeSlot " +
            "FROM visit_record vr " +
            "JOIN registration r ON vr.registration_id = r.id " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON vr.doctor_id = d.id " +
            "JOIN user u ON r.patient_id = u.id " +
            "WHERE d.user_id = #{doctorUserId} AND vr.finish_time IS NULL " +
            "ORDER BY vr.sign_in_time")
    List<java.util.Map<String, Object>> listOngoingByDoctorUser(@Param("doctorUserId") Long doctorUserId);
}
