package com.hospital.review;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT * FROM review WHERE registration_id = #{registrationId} LIMIT 1")
    Review findByRegistrationId(@Param("registrationId") Long registrationId);

    /** 医生详情页评价列表（含患者姓名） */
    @Select("SELECT rv.rating, rv.content, rv.create_time AS createTime, u.real_name AS patientName, " +
            "dept.name AS deptName " +
            "FROM review rv " +
            "JOIN user u ON rv.patient_id = u.id " +
            "JOIN doctor d ON rv.doctor_id = d.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE rv.doctor_id = #{doctorId} ORDER BY rv.create_time DESC LIMIT 20")
    List<Map<String, Object>> listByDoctor(@Param("doctorId") Long doctorId);

    /** 医生评分统计 */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(AVG(rating), 0) AS avgRating " +
            "FROM review WHERE doctor_id = #{doctorId}")
    Map<String, Object> ratingSummary(@Param("doctorId") Long doctorId);
}
