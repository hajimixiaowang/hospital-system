package com.hospital.schedule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 乐观扣号：只有余量 > 0 才扣成功。
     * 并发场景下两个请求同时约最后 1 个号，只有一个 UPDATE 影响行数为 1。
     */
    @Update("UPDATE schedule SET remain_count = remain_count - 1 " +
            "WHERE id = #{id} AND remain_count > 0 AND status = 'OPEN'")
    int deductRemain(@Param("id") Long id);

    /** 取消挂号后回滚号源 */
    @Update("UPDATE schedule SET remain_count = remain_count + 1 WHERE id = #{id}")
    int restoreRemain(@Param("id") Long id);

    /** 患者端联查：可预约号源 + 医生 + 科室 */
    @Select("SELECT s.id, s.source_no, s.work_date, s.time_slot, s.total_count, s.remain_count, " +
            "d.id AS doctorId, u.real_name AS doctorName, dept.name AS deptName, " +
            "d.title, dept.base_fee AS baseFee, d.title_fee AS titleFee, u.id AS doctorUserId " +
            "FROM schedule s " +
            "JOIN doctor d ON s.doctor_id = d.id " +
            "JOIN user u ON d.user_id = u.id " +
            "JOIN department dept ON d.dept_id = dept.id " +
            "WHERE s.work_date >= CURRENT_DATE() AND s.status = 'OPEN' AND s.remain_count > 0 " +
            "ORDER BY s.work_date, dept.id, s.time_slot")
    List<ScheduleView> listAvailable();

    /** 管理端统计：号源总量与剩余 */
    @Select("SELECT COUNT(*) FROM schedule")
    Long countAll();

    @Select("SELECT COALESCE(SUM(remain_count), 0) FROM schedule WHERE work_date >= CURRENT_DATE()")
    Long sumRemainTodayOn();
}
