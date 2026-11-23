package com.hospital.admin;

import com.hospital.announcement.Announcement;
import com.hospital.announcement.AnnouncementMapper;
import com.hospital.common.BizException;
import com.hospital.common.PasswordUtil;
import com.hospital.knowledge.Knowledge;
import com.hospital.knowledge.KnowledgeMapper;
import com.hospital.payment.Payment;
import com.hospital.payment.PaymentMapper;
import com.hospital.registration.Registration;
import com.hospital.registration.RegistrationMapper;
import com.hospital.registration.RegistrationView;
import com.hospital.schedule.Schedule;
import com.hospital.schedule.ScheduleMapper;
import com.hospital.schedule.SchedulePlan;
import com.hospital.schedule.ScheduleService;
import com.hospital.user.User;
import com.hospital.user.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理端服务 —— 负责人：冯冠丰
 * 科室/医生维护 + 建造者批量放号 + 数据看板 + 公告/患者/排班/订单/知识库/系统设置管理
 */
@Service
public class AdminService {

    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;
    private final AnnouncementMapper announcementMapper;
    private final DoctorQueryMapper doctorQueryMapper;
    private final RegistrationMapper registrationMapper;
    private final PaymentMapper paymentMapper;
    private final KnowledgeMapper knowledgeMapper;

    public AdminService(DepartmentMapper departmentMapper, DoctorMapper doctorMapper,
                        UserMapper userMapper, ScheduleService scheduleService,
                        ScheduleMapper scheduleMapper, AnnouncementMapper announcementMapper,
                        DoctorQueryMapper doctorQueryMapper, RegistrationMapper registrationMapper,
                        PaymentMapper paymentMapper, KnowledgeMapper knowledgeMapper) {
        this.departmentMapper = departmentMapper;
        this.doctorMapper = doctorMapper;
        this.userMapper = userMapper;
        this.scheduleService = scheduleService;
        this.scheduleMapper = scheduleMapper;
        this.announcementMapper = announcementMapper;
        this.doctorQueryMapper = doctorQueryMapper;
        this.registrationMapper = registrationMapper;
        this.paymentMapper = paymentMapper;
        this.knowledgeMapper = knowledgeMapper;
    }

    public List<Department> listDepartments() {
        return departmentMapper.selectList(null);
    }

    public List<Map<String, Object>> listDoctorViews() {
        return doctorMapper.listDoctorViews();
    }

    public void saveDepartment(Long id, String name, BigDecimal baseFee, String intro) {
        if (name == null || name.trim().isEmpty()) {
            throw new BizException("科室名称不能为空");
        }
        if (baseFee == null || baseFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("基础挂号费不合法");
        }
        Department dept = (id == null) ? new Department() : departmentMapper.selectById(id);
        if (dept == null) {
            throw new BizException("科室不存在");
        }
        dept.setName(name);
        dept.setBaseFee(baseFee);
        dept.setIntro(intro);
        if (id == null) {
            departmentMapper.insert(dept);
        } else {
            departmentMapper.updateById(dept);
        }
    }

    /** 新增医生：同时创建登录账号（初始密码 123456）与医生档案 */
    @Transactional
    public void saveDoctor(String username, String realName, Long deptId,
                           String title, BigDecimal titleFee, String phone,
                           String intro, String goodAt) {
        if (username == null || username.trim().isEmpty()) {
            throw new BizException("医生登录名不能为空");
        }
        if (deptId == null || departmentMapper.selectById(deptId) == null) {
            throw new BizException("请选择有效科室");
        }
        if (userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("username", username)) > 0) {
            throw new BizException("登录名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.sha256("123456"));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(User.ROLE_DOCTOR);
        userMapper.insert(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        doctor.setDeptId(deptId);
        doctor.setTitle(title);
        doctor.setTitleFee(titleFee == null ? BigDecimal.ZERO : titleFee);
        doctor.setIntro(intro);
        doctor.setGoodAt(goodAt);
        doctorMapper.insert(doctor);
    }

    /**
     * 排班放号：演示建造者模式 —— 调用方链式描述计划，校验集中在 build()
     */
    public int generateSchedulePlan(Long doctorId, LocalDate startDate, LocalDate endDate,
                                    List<String> timeSlots, Integer countPerSlot,
                                    boolean skipWeekend) {
        SchedulePlan.Builder builder = new SchedulePlan.Builder()
                .doctor(doctorId)
                .dateRange(startDate, endDate);
        for (String slot : timeSlots) {
            builder.addTimeSlot(slot);
        }
        if (countPerSlot != null) {
            builder.countPerSlot(countPerSlot);
        }
        builder.skipWeekend(skipWeekend);

        SchedulePlan plan = builder.build();   // 非法配置在此统一失败
        return scheduleService.generateSchedules(plan);
    }

    /** 删除科室：科室下还有医生时由外键约束挡住，转为友好提示 */
    public void deleteDepartment(Long id) {
        if (doctorMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Doctor>()
                .eq("dept_id", id)) > 0) {
            throw new BizException("该科室下仍有医生，无法删除");
        }
        departmentMapper.deleteById(id);
    }

    public List<Map<String, Object>> countDeptStats() {
        return doctorMapper.countDeptStats();
    }

    // ===== 数据看板 =====

    /** 看板概览：今日挂号、总挂号、总收入、总患者、总医生 */
    public Map<String, Object> dashboardOverview() {
        return doctorQueryMapper.dashboardOverview();
    }

    /** 近7日挂号趋势 */
    public List<Map<String, Object>> regTrend7Days() {
        return doctorQueryMapper.regTrend7Days();
    }

    // ===== 公告管理 =====

    public List<Announcement> listAnnouncements() {
        return announcementMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Announcement>()
                        .orderByDesc("create_time"));
    }

    public void saveAnnouncement(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new BizException("公告标题不能为空");
        }
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setCreateTime(java.time.LocalDateTime.now());
        announcementMapper.insert(a);
    }

    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    // ===== 患者管理 =====

    public List<User> listPatients() {
        return userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("role", User.ROLE_PATIENT).orderByDesc("create_time"));
    }

    public void togglePatientStatus(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException("患者不存在");
        }
        u.setStatus(User.STATUS_DISABLED.equals(u.getStatus())
                ? User.STATUS_ACTIVE : User.STATUS_DISABLED);
        userMapper.updateById(u);
    }

    // ===== 排班管理 =====

    public List<Schedule> listSchedules() {
        return scheduleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Schedule>()
                .orderByDesc("work_date").orderByDesc("time_slot"));
    }

    public void deleteSchedule(Long id) {
        scheduleMapper.deleteById(id);
    }

    // ===== 订单管理 =====

    public List<RegistrationView> listOrders(String status) {
        return registrationMapper.listAllOrders(status);
    }

    /** 管理员强制退单：未就诊订单取消 + 退款 + 回滚号源 */
    @Transactional
    public void adminRefund(Long registrationId) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null) {
            throw new BizException("订单不存在");
        }
        if (Registration.STATUS_VISITED.equals(reg.getStatus())
                || Registration.STATUS_CANCELLED.equals(reg.getStatus())) {
            throw new BizException("该订单当前状态不可退单");
        }
        reg.setStatus(Registration.STATUS_CANCELLED);
        registrationMapper.updateById(reg);
        scheduleMapper.restoreRemain(reg.getScheduleId());
        Payment p = paymentMapper.findByRegistrationId(registrationId);
        if (p != null) {
            p.setStatus(Payment.STATUS_REFUNDED);
            paymentMapper.updateById(p);
        }
    }

    // ===== 知识库管理 =====

    public List<Knowledge> listKnowledge() {
        return knowledgeMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Knowledge>()
                .orderByDesc("create_time"));
    }

    public void saveKnowledge(Long id, String keywords, String answer, String category) {
        if (keywords == null || keywords.trim().isEmpty()
                || answer == null || answer.trim().isEmpty()) {
            throw new BizException("关键词与回答不能为空");
        }
        Knowledge k = (id == null) ? new Knowledge()
                : knowledgeMapper.selectById(id);
        if (k == null) {
            k = new Knowledge();
        }
        k.setKeywords(keywords);
        k.setAnswer(answer);
        k.setCategory(category == null || category.isEmpty() ? "通用" : category);
        k.setCreateTime(java.time.LocalDateTime.now());
        if (id == null) {
            knowledgeMapper.insert(k);
        } else {
            knowledgeMapper.updateById(k);
        }
    }

    public void deleteKnowledge(Long id) {
        knowledgeMapper.deleteById(id);
    }
}
