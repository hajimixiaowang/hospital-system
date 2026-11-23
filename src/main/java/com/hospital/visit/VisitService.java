package com.hospital.visit;

import com.hospital.admin.Doctor;
import com.hospital.admin.DoctorMapper;
import com.hospital.common.BizException;
import com.hospital.notify.EventType;
import com.hospital.notify.RegistrationEventPublisher;
import com.hospital.registration.Registration;
import com.hospital.registration.RegistrationMapper;
import com.hospital.registration.RegistrationView;
import com.hospital.registration.factory.RegistrationFactory;
import com.hospital.visit.template.GeneralVisitProcess;
import com.hospital.visit.template.SpecialistVisitProcess;
import com.hospital.visit.template.EmergencyVisitProcess;
import com.hospital.visit.template.VisitProcessTemplate;
import com.hospital.visit.template.VisitStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 就诊服务 —— 负责人：陈梓健
 *
 * 模板方法 + 观察者的协作点（答辩演示重点）：
 *   1. 按挂号类型选择对应流程模板（NORMAL→普通、EXPERT→专家、EMERGENCY→急诊）
 *   2. 就诊 FINISH 环节完成后，广播 VISIT_FINISHED 事件，
 *      由 RemindListener 给患者发"就诊完成"通知——流程模块不写任何通知代码
 */
@Service
public class VisitService {

    private final Map<String, VisitProcessTemplate> templates = new HashMap<>();
    private final VisitRecordMapper visitRecordMapper;
    private final RegistrationMapper registrationMapper;
    private final DoctorMapper doctorMapper;
    private final RegistrationEventPublisher eventPublisher;

    public VisitService(GeneralVisitProcess general,
                        SpecialistVisitProcess specialist,
                        EmergencyVisitProcess emergency,
                        VisitRecordMapper visitRecordMapper,
                        RegistrationMapper registrationMapper,
                        DoctorMapper doctorMapper,
                        RegistrationEventPublisher eventPublisher) {
        templates.put(Registration.TYPE_NORMAL, general);
        templates.put(Registration.TYPE_EXPERT, specialist);
        templates.put(Registration.TYPE_EMERGENCY, emergency);
        this.visitRecordMapper = visitRecordMapper;
        this.registrationMapper = registrationMapper;
        this.doctorMapper = doctorMapper;
        this.eventPublisher = eventPublisher;
    }

    /** 推进一个就诊环节（模板方法入口） */
    @Transactional
    public void advance(Long doctorUserId, Long registrationId, VisitStep step,
                         String diagnosis, String prescription) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null) {
            throw new BizException("挂号单不存在");
        }
        if (!Registration.STATUS_BOOKED.equals(reg.getStatus())
                && !Registration.STATUS_PAID.equals(reg.getStatus())) {
            throw new BizException("挂号单状态异常，无法接诊");
        }

        VisitProcessTemplate template = templates.get(reg.getRegType());
        if (template == null) {
            throw new BizException("未找到对应的就诊流程模板：" + reg.getRegType());
        }

        VisitRecord record = visitRecordMapper.findByRegistrationId(registrationId);
        if (record == null) {
            // 首次推进（签到）时创建就诊记录
            Doctor doctor = doctorMapper.findByUserId(doctorUserId);
            if (doctor == null) {
                throw new BizException("医生档案缺失");
            }
            record = new VisitRecord();
            record.setRegistrationId(registrationId);
            record.setDoctorId(doctor.getId());
        }

        // 模板方法：内部完成顺序校验与环节落地
        template.advance(record, step, diagnosis, prescription);

        if (record.getId() == null) {
            visitRecordMapper.insert(record);
        } else {
            visitRecordMapper.updateById(record);
        }

        // 就诊完成：更新挂号单状态 + 广播事件（观察者接入点）
        if (step == VisitStep.FINISH) {
            reg.setStatus(Registration.STATUS_VISITED);
            registrationMapper.updateById(reg);
            RegistrationView view = registrationMapper.findViewById(registrationId);
            eventPublisher.publish(EventType.VISIT_FINISHED, view);
        }
    }

    /** 就诊操作台需要的数据：挂号详情 + 流程模板 + 当前记录 */
    public Map<String, Object> loadConsole(Long registrationId) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg == null) {
            throw new BizException("挂号单不存在");
        }
        VisitProcessTemplate template = templates.get(reg.getRegType());
        VisitRecord record = visitRecordMapper.findByRegistrationId(registrationId);

        Map<String, Object> model = new HashMap<>();
        model.put("reg", registrationMapper.findViewById(registrationId));
        model.put("steps", template.steps());
        model.put("templateName", template.describe());
        model.put("record", record);
        model.put("nextStep", template.nextStep(record == null ? blank() : record));
        return model;
    }

    private VisitRecord blank() {
        return new VisitRecord();
    }

    /** 医生端：今日预约列表（急诊置顶已在 SQL 完成） */
    public List<RegistrationView> listTodayByDoctorUser(Long doctorUserId) {
        return registrationMapper.listTodayByDoctorUser(doctorUserId);
    }

    /** 医生端：进行中的接诊 */
    public List<Map<String, Object>> listOngoingByDoctorUser(Long doctorUserId) {
        return visitRecordMapper.listOngoingByDoctorUser(doctorUserId);
    }

    /** 患者端：已完成就诊记录 */
    public List<Map<String, Object>> listRecordsByPatient(Long patientId) {
        return visitRecordMapper.listFinishedByPatient(patientId);
    }

    /** 供页面判断急诊优先 */
    public boolean isPriorityTemplate(String regType) {
        VisitProcessTemplate t = templates.get(regType);
        return t != null && t.isPriority();
    }
}
