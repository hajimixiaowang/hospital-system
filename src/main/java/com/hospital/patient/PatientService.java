package com.hospital.patient;

import com.hospital.admin.DoctorQueryMapper;
import com.hospital.announcement.Announcement;
import com.hospital.announcement.AnnouncementMapper;
import com.hospital.common.BizException;
import com.hospital.common.PasswordUtil;
import com.hospital.favorite.Favorite;
import com.hospital.favorite.FavoriteMapper;
import com.hospital.registration.Registration;
import com.hospital.registration.RegistrationMapper;
import com.hospital.review.Review;
import com.hospital.review.ReviewMapper;
import com.hospital.user.User;
import com.hospital.user.UserMapper;
import com.hospital.visit.VisitRecord;
import com.hospital.visit.VisitRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 患者端业务聚合：医生浏览、评价、收藏、公告、个人资料与改密。
 * （挂号/支付/就诊仍在各自模块，本类只做患者侧的新增功能）
 */
@Service
public class PatientService {

    private final DoctorQueryMapper doctorQueryMapper;
    private final ReviewMapper reviewMapper;
    private final FavoriteMapper favoriteMapper;
    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;
    private final RegistrationMapper registrationMapper;
    private final VisitRecordMapper visitRecordMapper;

    public PatientService(DoctorQueryMapper doctorQueryMapper,
                          ReviewMapper reviewMapper,
                          FavoriteMapper favoriteMapper,
                          AnnouncementMapper announcementMapper,
                          UserMapper userMapper,
                          RegistrationMapper registrationMapper,
                          VisitRecordMapper visitRecordMapper) {
        this.doctorQueryMapper = doctorQueryMapper;
        this.reviewMapper = reviewMapper;
        this.favoriteMapper = favoriteMapper;
        this.announcementMapper = announcementMapper;
        this.userMapper = userMapper;
        this.registrationMapper = registrationMapper;
        this.visitRecordMapper = visitRecordMapper;
    }

    // ===== 医生浏览 =====

    public List<Map<String, Object>> listDoctors() {
        return doctorQueryMapper.listPublicDoctors();
    }

    public Map<String, Object> getDoctor(Long id) {
        Map<String, Object> doctor = doctorQueryMapper.findPublicDoctor(id);
        if (doctor == null) {
            throw new BizException("医生不存在");
        }
        return doctor;
    }

    public List<Map<String, Object>> doctorReviews(Long doctorId) {
        return reviewMapper.listByDoctor(doctorId);
    }

    public Map<String, Object> doctorRating(Long doctorId) {
        return reviewMapper.ratingSummary(doctorId);
    }

    // ===== 收藏 =====

    public boolean isFavorited(Long patientId, Long doctorId) {
        return favoriteMapper.find(patientId, doctorId) != null;
    }

    @Transactional
    public boolean toggleFavorite(Long patientId, Long doctorId) {
        Favorite f = favoriteMapper.find(patientId, doctorId);
        if (f != null) {
            favoriteMapper.deleteById(f.getId());
            return false;
        }
        Favorite nf = new Favorite();
        nf.setPatientId(patientId);
        nf.setDoctorId(doctorId);
        nf.setCreateTime(java.time.LocalDateTime.now());
        favoriteMapper.insert(nf);
        return true;
    }

    public List<Map<String, Object>> listFavorites(Long patientId) {
        return favoriteMapper.listByPatient(patientId);
    }

    // ===== 公告 =====

    public List<Announcement> listAnnouncements() {
        return announcementMapper.selectList(null);
    }

    // ===== 评价 =====

    public boolean hasReviewed(Long registrationId) {
        return reviewMapper.findByRegistrationId(registrationId) != null;
    }

    @Transactional
    public void submitReview(User patient, Long registrationId, Integer rating, String content) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null || !reg.getPatientId().equals(patient.getId())) {
            throw new BizException("挂号单不存在");
        }
        if (!Registration.STATUS_VISITED.equals(reg.getStatus())) {
            throw new BizException("仅就诊完成后可评价");
        }
        if (reviewMapper.findByRegistrationId(registrationId) != null) {
            throw new BizException("您已评价过本次就诊");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BizException("评分须在 1~5 星之间");
        }
        Review review = new Review();
        review.setRegistrationId(registrationId);
        review.setPatientId(patient.getId());
        review.setDoctorId(resolveDoctorId(reg));
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(java.time.LocalDateTime.now());
        reviewMapper.insert(review);
    }

    private Long resolveDoctorId(Registration reg) {
        VisitRecord record = visitRecordMapper.findByRegistrationId(reg.getId());
        if (record == null) {
            throw new BizException("未找到就诊记录，无法评价");
        }
        return record.getDoctorId();
    }

    // ===== 个人资料 / 改密 =====

    @Transactional
    public void updateProfile(User patient, String realName, String phone, String patientType) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new BizException("姓名不能为空");
        }
        patient.setRealName(realName);
        patient.setPhone(phone);
        if (patientType != null && !patientType.isEmpty()) {
            patient.setPatientType(patientType);
        }
        userMapper.updateById(patient);
    }

    @Transactional
    public void changePassword(User patient, String oldPwd, String newPwd) {
        User dbUser = userMapper.selectById(patient.getId());
        if (dbUser == null || !dbUser.getPassword().equals(PasswordUtil.sha256(oldPwd))) {
            throw new BizException("原密码错误");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        dbUser.setPassword(PasswordUtil.sha256(newPwd));
        userMapper.updateById(dbUser);
    }
}
