package com.hospital.notify;

import com.hospital.registration.RegistrationView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 提醒定时任务专用查询（观察者模块内部使用） */
@Mapper
public interface RemindQueryMapper {

    @Select("SELECT r.id, r.patient_id AS patientId, r.reg_no AS regNo, r.reg_type AS regType, " +
            "r.fee, r.status, r.create_time AS createTime, s.work_date AS workDate, " +
            "s.time_slot AS timeSlot, s.source_no AS sourceNo, u.real_name AS patientName, " +
            "u.patient_type AS patientType, dept.name AS deptName, d.title " +
            "FROM registration r " +
            "JOIN schedule s ON r.schedule_id = s.id " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user u ON r.patient_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE r.status = 'BOOKED' AND s.work_date = CURRENT_DATE()")
    List<RegistrationView> listTodayBooked();

    /** 幂等检查：该单号是否已发过 REMIND 通知（单号在 content 里） */
    @Select("SELECT COUNT(*) FROM notification WHERE type = 'REMIND' AND content LIKE CONCAT('%', #{regNo}, '%')")
    long existsRemindSent(@Param("regNo") String regNo);
}
