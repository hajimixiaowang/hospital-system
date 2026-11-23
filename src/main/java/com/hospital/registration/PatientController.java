package com.hospital.registration;

import com.hospital.common.BizException;
import com.hospital.notify.NotificationService;
import com.hospital.patient.PatientService;
import com.hospital.payment.PaymentService;
import com.hospital.schedule.ScheduleService;
import com.hospital.schedule.ScheduleView;
import com.hospital.user.User;
import com.hospital.visit.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者端 —— 负责人：刘嘉钊
 * 首页/挂号/支付/评价/收藏/医生浏览/公告/个人中心
 */
@Controller
public class PatientController {

    private final ScheduleService scheduleService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final VisitService visitService;
    private final PatientService patientService;
    private final PaymentService paymentService;

    public PatientController(ScheduleService scheduleService,
                             RegistrationService registrationService,
                             NotificationService notificationService,
                             VisitService visitService,
                             PatientService patientService,
                             PaymentService paymentService) {
        this.scheduleService = scheduleService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.visitService = visitService;
        this.patientService = patientService;
        this.paymentService = paymentService;
    }

    private User user(HttpSession session) {
        return (User) session.getAttribute("loginUser");
    }

    // ===== 首页 =====

    @GetMapping("/patient")
    public String home(HttpSession session, Model model) {
        User u = user(session);
        List<RegistrationView> myRegs = registrationService.listByPatient(u.getId());
        long toPay = myRegs.stream().filter(r -> Registration.STATUS_BOOKED.equals(r.getStatus())).count();
        long visited = myRegs.stream().filter(r -> Registration.STATUS_VISITED.equals(r.getStatus())).count();

        model.addAttribute("user", u);
        model.addAttribute("announcements", patientService.listAnnouncements());
        model.addAttribute("unreadCount", notificationService.unreadCount(u.getId()));
        model.addAttribute("regCount", myRegs.size());
        model.addAttribute("toPay", toPay);
        model.addAttribute("visited", visited);
        model.addAttribute("favCount", patientService.listFavorites(u.getId()).size());
        model.addAttribute("recentNotices", notificationService.listByUser(u.getId()));
        model.addAttribute("favorites", patientService.listFavorites(u.getId()));
        return "patient/index";
    }

    // ===== 医生浏览 =====

    @GetMapping("/patient/doctors")
    public String doctors(HttpSession session, Model model,
                          @RequestParam(required = false) Long deptId) {
        User u = user(session);
        List<Map<String, Object>> doctors = patientService.listDoctors();
        if (deptId != null) {
            doctors.removeIf(d -> !deptId.equals(d.get("deptId")));
        }
        model.addAttribute("user", u);
        model.addAttribute("doctors", doctors);
        model.addAttribute("favSet", favoriteSet(u.getId()));
        return "patient/doctors";
    }

    // ===== 预约挂号（可约号源汇总，直接下单） =====

    @GetMapping("/patient/booking")
    public String booking(HttpSession session, Model model) {
        User u = user(session);
        List<ScheduleView> schedules = scheduleService.listAvailable();
        Map<Long, BigDecimal> feePreview = new HashMap<>();
        for (ScheduleView sv : schedules) {
            feePreview.put(sv.getId(), registrationService.previewFee(u, sv));
        }
        model.addAttribute("user", u);
        model.addAttribute("schedules", schedules);
        model.addAttribute("feePreview", feePreview);
        return "patient/booking";
    }

    // ===== 就诊须知 / AI 助手 =====

    @GetMapping("/patient/guide")
    public String guide(HttpSession session, Model model) {
        model.addAttribute("user", user(session));
        return "patient/guide";
    }

    @GetMapping("/patient/assistant")
    public String assistant(HttpSession session, Model model) {
        model.addAttribute("user", user(session));
        return "patient/assistant";
    }

    @GetMapping("/patient/doctor/{id}")
    public String doctorDetail(@PathVariable Long id, HttpSession session, Model model) {
        User u = user(session);
        Map<String, Object> doctor = patientService.getDoctor(id);
        // 该医生可约号源
        List<ScheduleView> schedules = scheduleService.listAvailable();
        schedules.removeIf(sv -> !id.equals(sv.getDoctorId()));
        Map<Long, BigDecimal> feePreview = new HashMap<>();
        for (ScheduleView sv : schedules) {
            feePreview.put(sv.getId(), registrationService.previewFee(u, sv));
        }
        model.addAttribute("user", u);
        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", schedules);
        model.addAttribute("feePreview", feePreview);
        model.addAttribute("reviews", patientService.doctorReviews(id));
        model.addAttribute("rating", patientService.doctorRating(id));
        model.addAttribute("favorited", patientService.isFavorited(u.getId(), id));
        return "patient/doctor-detail";
    }

    @PostMapping("/patient/favorite/{doctorId}")
    public String toggleFavorite(@PathVariable Long doctorId, HttpSession session) {
        User u = user(session);
        patientService.toggleFavorite(u.getId(), doctorId);
        return "redirect:/patient/doctor/" + doctorId;
    }

    // ===== 挂号 / 支付 / 取消 =====

    @PostMapping("/patient/book")
    public String book(@RequestParam Long scheduleId,
                       @RequestParam(defaultValue = "NORMAL") String regType,
                       HttpSession session) {
        User u = user(session);
        ScheduleView target = null;
        for (ScheduleView sv : scheduleService.listAvailable()) {
            if (sv.getId().equals(scheduleId)) {
                target = sv;
                break;
            }
        }
        if (target == null) {
            throw new BizException("号源不存在或已不可预约");
        }
        registrationService.book(u, target, regType);
        return "redirect:/patient/registrations";
    }

    @PostMapping("/patient/pay/{id}")
    public String pay(@PathVariable Long id, HttpSession session) {
        User u = user(session);
        paymentService.pay(u.getId(), id, "BALANCE");
        return "redirect:/patient/registrations";
    }

    @PostMapping("/patient/cancel/{id}")
    public String cancel(@PathVariable Long id, HttpSession session) {
        registrationService.cancel(user(session), id);
        return "redirect:/patient/registrations";
    }

    // ===== 评价 =====

    @GetMapping("/patient/review/{id}")
    public String reviewPage(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("user", user(session));
        model.addAttribute("regId", id);
        return "patient/review";
    }

    @PostMapping("/patient/review/{id}")
    public String review(@PathVariable Long id,
                         @RequestParam Integer rating,
                         @RequestParam(required = false) String content,
                         HttpSession session) {
        patientService.submitReview(user(session), id, rating, content);
        return "redirect:/patient/records";
    }

    // ===== 列表页 =====

    @GetMapping("/patient/registrations")
    public String registrations(HttpSession session, Model model) {
        User u = user(session);
        List<RegistrationView> myRegs = registrationService.listByPatient(u.getId());
        Map<Long, String> payStatus = new HashMap<>();
        for (RegistrationView r : myRegs) {
            payStatus.put(r.getId(),
                    paymentService.findByRegistrationId(r.getId()) != null ? "已支付" : "未支付");
        }
        model.addAttribute("user", u);
        model.addAttribute("myRegs", myRegs);
        model.addAttribute("payStatus", payStatus);
        return "patient/registrations";
    }

    @GetMapping("/patient/records")
    public String records(HttpSession session, Model model) {
        User u = user(session);
        List<Map<String, Object>> records = visitService.listRecordsByPatient(u.getId());
        Map<Long, Boolean> reviewed = new HashMap<>();
        for (Map<String, Object> r : records) {
            Object regIdObj = r.get("registrationId");
            Long regId = regIdObj == null ? null : Long.valueOf(regIdObj.toString());
            if (regId != null) {
                reviewed.put(regId, patientService.hasReviewed(regId));
            }
        }
        model.addAttribute("user", u);
        model.addAttribute("records", records);
        model.addAttribute("reviewed", reviewed);
        return "patient/records";
    }

    @GetMapping("/patient/notifications")
    public String notifications(HttpSession session, Model model) {
        User u = user(session);
        model.addAttribute("user", u);
        model.addAttribute("notifications", notificationService.listByUser(u.getId()));
        return "patient/notifications";
    }

    @PostMapping("/patient/notify/read/{id}")
    public String readNotification(@PathVariable Long id, HttpSession session) {
        notificationService.markRead(id, user(session).getId());
        return "redirect:/patient/notifications";
    }

    @GetMapping("/patient/favorites")
    public String favorites(HttpSession session, Model model) {
        User u = user(session);
        model.addAttribute("user", u);
        model.addAttribute("favorites", patientService.listFavorites(u.getId()));
        return "patient/favorites";
    }

    // ===== 个人中心 =====

    @GetMapping("/patient/profile")
    public String profile(HttpSession session, Model model) {
        model.addAttribute("user", user(session));
        return "patient/profile";
    }

    @PostMapping("/patient/profile")
    public String updateProfile(String realName, String phone, String patientType,
                                HttpSession session) {
        User u = user(session);
        patientService.updateProfile(u, realName, phone, patientType);
        session.setAttribute("loginUser", u);
        return "redirect:/patient/profile";
    }

    @PostMapping("/patient/password")
    public String changePassword(@RequestParam String oldPwd,
                                 @RequestParam String newPwd, HttpSession session) {
        patientService.changePassword(user(session), oldPwd, newPwd);
        return "redirect:/logout";
    }

    private java.util.Set<Long> favoriteSet(Long patientId) {
        java.util.Set<Long> set = new java.util.HashSet<>();
        for (Map<String, Object> f : patientService.listFavorites(patientId)) {
            set.add(Long.valueOf(f.get("doctorId").toString()));
        }
        return set;
    }
}
