package com.hospital.visit;

import com.hospital.admin.Doctor;
import com.hospital.admin.DoctorMapper;
import com.hospital.admin.DoctorQueryMapper;
import com.hospital.announcement.Announcement;
import com.hospital.announcement.AnnouncementMapper;
import com.hospital.common.BizException;
import com.hospital.registration.RegistrationView;
import com.hospital.user.User;
import com.hospital.visit.template.VisitStep;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 医生端 —— 负责人：陈梓健
 * 工作台 / 今日患者列表 / 接诊管理 / 排班查看 / AI 助手 / 公告查看 / 医生信息管理
 */
@Controller
public class DoctorController {

    private final VisitService visitService;
    private final DoctorMapper doctorMapper;
    private final DoctorQueryMapper doctorQueryMapper;
    private final AnnouncementMapper announcementMapper;

    public DoctorController(VisitService visitService, DoctorMapper doctorMapper,
                            DoctorQueryMapper doctorQueryMapper, AnnouncementMapper announcementMapper) {
        this.visitService = visitService;
        this.doctorMapper = doctorMapper;
        this.doctorQueryMapper = doctorQueryMapper;
        this.announcementMapper = announcementMapper;
    }

    private Doctor self(User user) {
        Doctor d = doctorMapper.findByUserId(user.getId());
        if (d == null) {
            throw new BizException("医生档案缺失，请联系管理员");
        }
        return d;
    }

    @GetMapping("/doctor")
    public String doctorHome(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        Doctor self = self(user);
        List<RegistrationView> today = visitService.listTodayByDoctorUser(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("doctor", self);
        model.addAttribute("todayRegs", today);
        model.addAttribute("ongoing", visitService.listOngoingByDoctorUser(user.getId()));
        model.addAttribute("stats", doctorQueryMapper.doctorTodayStats(user.getId()));
        return "doctor/index";
    }

    /** 今日患者列表（从工作台拆出的独立页） */
    @GetMapping("/doctor/today")
    public String today(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("user", user);
        model.addAttribute("todayRegs", visitService.listTodayByDoctorUser(user.getId()));
        return "doctor/today";
    }

    /** 接诊管理：进行中 + 已完成 */
    @GetMapping("/doctor/consult")
    public String consult(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("user", user);
        model.addAttribute("ongoing", visitService.listOngoingByDoctorUser(user.getId()));
        return "doctor/consult";
    }

    /** 排班查看：自己的号源列表 */
    @GetMapping("/doctor/schedules")
    public String schedules(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        Doctor self = self(user);
        model.addAttribute("user", user);
        model.addAttribute("schedules", doctorMapper.listMySchedules(self.getId()));
        return "doctor/schedules";
    }

    /** AI 助手 */
    @GetMapping("/doctor/assistant")
    public String assistant(HttpSession session, Model model) {
        model.addAttribute("user", (User) session.getAttribute("loginUser"));
        return "doctor/assistant";
    }

    /** 公告查看 */
    @GetMapping("/doctor/announcements")
    public String announcements(HttpSession session, Model model) {
        model.addAttribute("user", (User) session.getAttribute("loginUser"));
        model.addAttribute("announcements", announcementMapper.selectList(null));
        return "doctor/announcements";
    }

    /** 医生信息管理（编辑自己的简介与擅长） */
    @GetMapping("/doctor/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("user", user);
        model.addAttribute("doctor", self(user));
        return "doctor/profile";
    }

    @PostMapping("/doctor/profile")
    public String updateProfile(String intro, String goodAt, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        Doctor self = self(user);
        self.setIntro(intro);
        self.setGoodAt(goodAt);
        doctorMapper.updateById(self);
        return "redirect:/doctor/profile";
    }

    @GetMapping("/doctor/visit/{registrationId}")
    public String visitConsole(@PathVariable Long registrationId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        Map<String, Object> console = visitService.loadConsole(registrationId);
        RegistrationView reg = (RegistrationView) console.get("reg");
        if (reg == null) {
            throw new BizException("挂号单不存在");
        }
        model.addAttribute("console", console);
        return "doctor/visit";
    }

    @PostMapping("/doctor/visit/step")
    public String advanceStep(@RequestParam Long registrationId,
                              @RequestParam String step,
                              @RequestParam(required = false) String diagnosis,
                              @RequestParam(required = false) String prescription,
                              HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        VisitStep visitStep;
        try {
            visitStep = VisitStep.valueOf(step);
        } catch (IllegalArgumentException e) {
            throw new BizException("非法的就诊环节：" + step);
        }
        try {
            visitService.advance(user.getId(), registrationId, visitStep, diagnosis, prescription);
            return "redirect:/doctor/visit/" + registrationId;
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("console", visitService.loadConsole(registrationId));
            return "doctor/visit";
        }
    }

    /** 查看某患者的历史就诊记录 */
    @GetMapping("/doctor/patient/{patientId}/records")
    public String patientRecords(@PathVariable Long patientId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("user", user);
        model.addAttribute("records", visitService.listRecordsByPatient(patientId));
        return "doctor/patient-records";
    }
}
