package com.hospital.favorite;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorite WHERE patient_id = #{patientId} AND doctor_id = #{doctorId} LIMIT 1")
    Favorite find(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    @Select("SELECT f.doctor_id AS doctorId, u.real_name AS doctorName, d.title, dept.name AS deptName, " +
            "d.intro, d.good_at AS goodAt " +
            "FROM favorite f " +
            "JOIN doctor d ON f.doctor_id = d.id " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE f.patient_id = #{patientId} ORDER BY f.create_time DESC")
    List<Map<String, Object>> listByPatient(@Param("patientId") Long patientId);
}
