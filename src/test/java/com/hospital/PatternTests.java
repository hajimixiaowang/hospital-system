package com.hospital;

import com.hospital.registration.fee.FeeCalculator;
import com.hospital.registration.fee.FeeContext;
import com.hospital.registration.fee.InsuranceFeeStrategy;
import com.hospital.registration.fee.NightFeeStrategy;
import com.hospital.registration.fee.NormalFeeStrategy;
import com.hospital.registration.fee.StudentFeeStrategy;
import com.hospital.schedule.Schedule;
import com.hospital.schedule.SchedulePlan;
import com.hospital.schedule.NumberSourceGenerator;
import com.hospital.visit.template.EmergencyVisitProcess;
import com.hospital.visit.template.GeneralVisitProcess;
import com.hospital.visit.template.SpecialistVisitProcess;
import com.hospital.visit.template.VisitProcessTemplate;
import com.hospital.visit.template.VisitStep;
import com.hospital.visit.VisitRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设计模式单元测试（纯 POJO，无需数据库与 Spring 上下文）
 * 报告"测试与分析"章节可直接引用本类的测试结论。
 */
class PatternTests {

    // ========== 一、策略模式：挂号费多因素计算 ==========

    private FeeCalculator fullCalculator() {
        return new FeeCalculator(Arrays.asList(
                new NormalFeeStrategy(),
                new InsuranceFeeStrategy(),
                new StudentFeeStrategy(),
                new NightFeeStrategy()));
    }

    private FeeContext ctxOf(String slot, String patientType) {
        // 模拟：内科(base 10) + 主任医师(title 30) => 基数 40 元
        FeeContext ctx = new FeeContext();
        ctx.setBaseFee(new BigDecimal("10"));
        ctx.setTitleFee(new BigDecimal("30"));
        ctx.setTimeSlot(slot);
        ctx.setPatientType(patientType);
        return ctx;
    }

    @Test
    void 普通患者白天全额计费_基数40元() {
        assertEquals(new BigDecimal("40.00"),
                fullCalculator().calcFee(ctxOf(Schedule.SLOT_MORNING, "NORMAL")));
    }

    @Test
    void 医保患者打8折_40元变32元() {
        assertEquals(new BigDecimal("32.00"),
                fullCalculator().calcFee(ctxOf(Schedule.SLOT_MORNING, "INSURANCE")));
    }

    @Test
    void 学生患者打5折_40元变20元() {
        assertEquals(new BigDecimal("20.00"),
                fullCalculator().calcFee(ctxOf(Schedule.SLOT_MORNING, "STUDENT")));
    }

    @Test
    void 夜间时段上浮20_40元变48元_且优先于患者类型() {
        assertEquals(new BigDecimal("48.00"),
                fullCalculator().calcFee(ctxOf(Schedule.SLOT_NIGHT, "NORMAL")));
        assertEquals(new BigDecimal("48.00"),
                fullCalculator().calcFee(ctxOf(Schedule.SLOT_NIGHT, "STUDENT")));
    }

    // ========== 二、单例模式：号源编号唯一性 ==========

    @Test
    void 并发100线程获取实例_全局仅1个对象() throws InterruptedException {
        Set<NumberSourceGenerator> seen = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                seen.add(NumberSourceGenerator.getInstance());
                latch.countDown();
            }).start();
        }
        latch.await();
        assertEquals(1, seen.size());
    }

    @Test
    void 并发生成号源编号_无一重号() throws InterruptedException {
        NumberSourceGenerator generator = NumberSourceGenerator.getInstance();
        int threads = 16, perThread = 50;
        Set<String> numbers = java.util.Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger conflicts = new AtomicInteger();
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    String no = generator.nextSourceNo(LocalDate.now(), 1L);
                    if (!numbers.add(no)) {
                        conflicts.incrementAndGet();
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        assertEquals(0, conflicts.get(), "并发下不允许出现重号");
        assertEquals(threads * perThread, numbers.size());
    }

    // ========== 三、建造者模式：排班计划构建与校验 ==========

    @Test
    void 链式构建排班计划_3天2时段跳过周末_展开数量正确() {
        // 2026-11-09(周一)~2026-11-11(周三)，3 个工作日 x 2 时段 = 6 条号源
        SchedulePlan plan = new SchedulePlan.Builder()
                .doctor(1L)
                .dateRange(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 11))
                .addTimeSlot(Schedule.SLOT_MORNING)
                .addTimeSlot(Schedule.SLOT_AFTERNOON)
                .countPerSlot(20)
                .skipWeekend(true)
                .build();
        assertEquals(6, plan.expand().size());
        assertEquals(20, plan.getCountPerSlot());
    }

    @Test
    void 不指定医生时构建直接失败() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new SchedulePlan.Builder()
                        .dateRange(LocalDate.now(), LocalDate.now().plusDays(1))
                        .addTimeSlot(Schedule.SLOT_MORNING)
                        .build());
        assertTrue(ex.getMessage().contains("医生"));
    }

    @Test
    void 结束日期早于开始日期时构建失败() {
        assertThrows(IllegalArgumentException.class,
                () -> new SchedulePlan.Builder()
                        .doctor(1L)
                        .dateRange(LocalDate.now().plusDays(1), LocalDate.now())
                        .addTimeSlot(Schedule.SLOT_MORNING)
                        .build());
    }

    // ========== 四、模板方法模式：就诊流程骨架与顺序纪律 ==========

    @Test
    void 普通门诊流程无检查环节_跳过签到直接问诊应被拒绝() {
        VisitProcessTemplate template = new GeneralVisitProcess();
        VisitRecord record = new VisitRecord();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> template.advance(record, VisitStep.INQUIRY, null, null));
        assertTrue(ex.getMessage().contains("流程顺序错误"));
        assertEquals(VisitStep.SIGN_IN, template.nextStep(record));
    }

    @Test
    void 专家门诊含检查环节_普通门诊执行检查应被拒绝() {
        VisitProcessTemplate specialist = new SpecialistVisitProcess();
        VisitRecord record = new VisitRecord();
        record.setSignInTime(java.time.LocalDateTime.now());
        record.setInquiryTime(java.time.LocalDateTime.now());
        // 专家：问诊后应先检查
        assertEquals(VisitStep.EXAM, specialist.nextStep(record));

        VisitProcessTemplate general = new GeneralVisitProcess();
        assertThrows(IllegalStateException.class,
                () -> general.advance(record, VisitStep.EXAM, null, null));
    }

    @Test
    void 完整走通专家流程_开方必须填诊断_完成后无下一环节() {
        VisitProcessTemplate specialist = new SpecialistVisitProcess();
        VisitRecord record = new VisitRecord();
        specialist.advance(record, VisitStep.SIGN_IN, null, null);
        specialist.advance(record, VisitStep.INQUIRY, null, null);
        specialist.advance(record, VisitStep.EXAM, null, null);
        // 开方不带诊断 -> 拒绝
        assertThrows(IllegalArgumentException.class,
                () -> specialist.advance(record, VisitStep.PRESCRIBE, "  ", null));
        specialist.advance(record, VisitStep.PRESCRIBE, "急性上呼吸道感染", "复方氨酚烷胺片");
        assertNotNull(record.getDiagnosis());
        specialist.advance(record, VisitStep.FINISH, null, null);
        assertNull(specialist.nextStep(record), "流程完成后应无下一环节");
    }

    @Test
    void 急诊模板优先置顶_普通与专家不置顶() {
        assertTrue(new EmergencyVisitProcess().isPriority());
        assertFalse(new GeneralVisitProcess().isPriority());
        assertFalse(new SpecialistVisitProcess().isPriority());
    }

    @Test
    void 三个模板的流程步骤序列正确() {
        List<VisitStep> general = new GeneralVisitProcess().steps();
        List<VisitStep> specialist = new SpecialistVisitProcess().steps();
        List<VisitStep> emergency = new EmergencyVisitProcess().steps();
        assertFalse(general.contains(VisitStep.EXAM));
        assertTrue(specialist.contains(VisitStep.EXAM));
        assertFalse(emergency.contains(VisitStep.EXAM));
        assertEquals(4, general.size());   // 签到/问诊/开方/完成
        assertEquals(5, specialist.size()); // +检查
    }
}
