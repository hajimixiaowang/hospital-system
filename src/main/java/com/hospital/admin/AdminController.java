package com.hospital.admin;

import com.hospital.common.BizException;
import com.hospital.schedule.Schedule;
import com.hospital.setting.SettingService;
import com.hospital.user.User;
import com.hospital.user.UserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 管理端 —— 负责人：冯冠丰：数据看板、科室医生排班公告患者管理、订单、知识库、系统设置 */
@Controller
public class AdminController {

    private final AdminService adminService;
    private final SettingService adminSettingService;
    private final UserMapper adminUserMapper;

    public AdminController(AdminService adminService, SettingService adminSettingService,
                           UserMapper adminUserMapper) {
        this.adminService = adminService;
        this.adminSettingService = adminSettingService;
        this.adminUserMapper = adminUserMapper;
    }

    private void common(Model model, HttpSession session) {
        model.addAttribute("user", (User) session.getAttribute("loginUser"));
    }

    // ===== 数据看板 =====

    @GetMapping("/admin")
    public String dashboard(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("overview", adminService.dashboardOverview());
        model.addAttribute("trend", adminService.regTrend7Days());
        model.addAttribute("deptStats", adminService.countDeptStats());
        return "admin/dashboard";
    }

    // ===== 科室管理 =====

    @GetMapping("/admin/departments")
    public String departments(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("depts", adminService.listDepartments());
        return "admin/departments";
    }

    @PostMapping("/admin/dept/save")
    public String saveDept(Long id, String name,
                           @RequestParam("baseFee") BigDecimal baseFee, String intro) {
        adminService.saveDepartment(id, name, baseFee, intro);
        return "redirect:/admin/departments";
    }

    @PostMapping("/admin/dept/del/{id}")
    public String delDept(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return "redirect:/admin/departments";
    }

    // ===== 医生管理 =====

    @GetMapping("/admin/doctors")
    public String doctors(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("depts", adminService.listDepartments());
        model.addAttribute("doctors", adminService.listDoctorViews());
        return "admin/doctors";
    }

    @PostMapping("/admin/doctor/save")
    public String saveDoctor(String username, String realName, Long deptId, String title,
                             @RequestParam(value = "titleFee", required = false) BigDecimal titleFee,
                             String phone, String intro, String goodAt) {
        adminService.saveDoctor(username, realName, deptId, title, titleFee, phone, intro, goodAt);
        return "redirect:/admin/doctors";
    }

    // ===== 排班放号（建造者）与排班列表 =====

    @GetMapping("/admin/schedules")
    public String schedules(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("doctors", adminService.listDoctorViews());
        model.addAttribute("schedules", adminService.listSchedules());
        return "admin/schedules";
    }

    @PostMapping("/admin/plan")
    public String makePlan(@RequestParam("doctorId") Long doctorId,
                           @RequestParam("startDate") String startDate,
                           @RequestParam("endDate") String endDate,
                           @RequestParam(value = "slots", required = false) List<String> slots,
                           @RequestParam(value = "countPerSlot", required = false) Integer countPerSlot,
                           @RequestParam(value = "skipWeekend", defaultValue = "false") boolean skipWeekend,
                           HttpSession session, Model model) {
        List<String> timeSlots = (slots == null) ? new ArrayList<>() : slots;
        try {
            int count = adminService.generateSchedulePlan(doctorId,
                    LocalDate.parse(startDate), LocalDate.parse(endDate),
                    timeSlots, countPerSlot, skipWeekend);
            model.addAttribute("msg", "放号成功，共生成 " + count + " 个号源");
        } catch (IllegalArgumentException e) {
            model.addAttribute("msg", "放号失败：" + e.getMessage());
        }
        return schedules(model, session);
    }

    @PostMapping("/admin/schedule/del/{id}")
    public String delSchedule(@PathVariable Long id) {
        adminService.deleteSchedule(id);
        return "redirect:/admin/schedules";
    }

    // ===== 公告管理 =====

    @GetMapping("/admin/announcements")
    public String announcements(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("announcements", adminService.listAnnouncements());
        return "admin/announcements";
    }

    @PostMapping("/admin/announcement/save")
    public String saveAnnouncement(String title, String content) {
        adminService.saveAnnouncement(title, content);
        return "redirect:/admin/announcements";
    }

    @PostMapping("/admin/announcement/del/{id}")
    public String delAnnouncement(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return "redirect:/admin/announcements";
    }

    // ===== 患者管理 =====

    @GetMapping("/admin/patients")
    public String patients(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("patients", adminService.listPatients());
        return "admin/patients";
    }

    @PostMapping("/admin/patient/toggle/{id}")
    public String togglePatient(@PathVariable Long id) {
        adminService.togglePatientStatus(id);
        return "redirect:/admin/patients";
    }

    // ===== 订单管理 =====

    @GetMapping("/admin/orders")
    public String orders(@RequestParam(required = false) String status,
                         Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("orders", adminService.listOrders(status));
        model.addAttribute("statusFilter", status == null ? "" : status);
        return "admin/orders";
    }

    @PostMapping("/admin/order/refund/{id}")
    public String refundOrder(@PathVariable Long id) {
        adminService.adminRefund(id);
        return "redirect:/admin/orders";
    }

    // ===== 知识库管理（AI 助手数据源） =====

    @GetMapping("/admin/knowledge")
    public String knowledge(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("items", adminService.listKnowledge());
        return "admin/knowledge";
    }

    @PostMapping("/admin/knowledge/save")
    public String saveKnowledge(Long id, String keywords, String answer, String category) {
        adminService.saveKnowledge(id, keywords, answer, category);
        return "redirect:/admin/knowledge";
    }

    @PostMapping("/admin/knowledge/del/{id}")
    public String delKnowledge(@PathVariable Long id) {
        adminService.deleteKnowledge(id);
        return "redirect:/admin/knowledge";
    }

    // ===== 系统设置 =====

    @GetMapping("/admin/settings")
    public String settings(Model model, HttpSession session) {
        common(model, session);
        model.addAttribute("settingList", adminSettingService.listAll());
        return "admin/settings";
    }

    @PostMapping("/admin/settings")
    public String saveSettings(javax.servlet.http.HttpServletRequest request) {
        java.util.Map<String, String> values = new java.util.HashMap<>();
        for (com.hospital.setting.Setting s : adminSettingService.listAll()) {
            String v = request.getParameter(s.getSettingKey());
            if (v != null) {
                values.put(s.getSettingKey(), v);
            }
        }
        adminSettingService.saveAll(values);
        return "redirect:/admin/settings";
    }

    // ===== 管理员个人中心 =====

    @GetMapping("/admin/profile")
    public String profile(Model model, HttpSession session) {
        common(model, session);
        return "admin/profile";
    }

    @PostMapping("/admin/password")
    public String changePassword(@RequestParam String oldPwd,
                                 @RequestParam String newPwd, HttpSession session) {
        User admin = (User) session.getAttribute("loginUser");
        User dbUser = adminUserMapper.selectById(admin.getId());
        if (dbUser == null || !dbUser.getPassword().equals(
                com.hospital.common.PasswordUtil.sha256(oldPwd))) {
            throw new BizException("原密码错误");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        dbUser.setPassword(com.hospital.common.PasswordUtil.sha256(newPwd));
        adminUserMapper.updateById(dbUser);
        return "redirect:/logout";
    }
}
