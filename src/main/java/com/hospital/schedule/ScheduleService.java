package com.hospital.schedule;

import com.hospital.admin.Doctor;
import com.hospital.admin.DoctorMapper;
import com.hospital.admin.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 号源服务 —— 负责人：冯冠丰
 * 建造者构建的排班计划在此落地：逐条调用单例生成器拿唯一编号后入库。
 * （建造者 + 单例两个模式的协作点，答辩演示重点）
 */
@Service
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;

    public ScheduleService(ScheduleMapper scheduleMapper, DoctorMapper doctorMapper) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
    }

    /**
     * 按排班计划批量放号。
     * 1. SchedulePlan.expand() 展开日期 x 时段明细
     * 2. 每条明细通过单例 NumberSourceGenerator 生成全局唯一编号
     */
    @Transactional
    public int generateSchedules(SchedulePlan plan) {
        Doctor doctor = doctorMapper.selectById(plan.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在：doctorId=" + plan.getDoctorId());
        }
        List<Schedule> details = plan.expand();
        NumberSourceGenerator generator = NumberSourceGenerator.getInstance();
        for (Schedule s : details) {
            s.setSourceNo(generator.nextSourceNo(s.getWorkDate(), doctor.getDeptId()));
            scheduleMapper.insert(s);
        }
        return details.size();
    }

    /** 患者端：可预约号源（含医生、科室信息） */
    public List<ScheduleView> listAvailable() {
        return scheduleMapper.listAvailable();
    }
}
