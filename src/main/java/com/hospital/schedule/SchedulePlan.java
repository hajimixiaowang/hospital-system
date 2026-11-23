package com.hospital.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 【设计模式·建造者】排班计划 —— 负责人：冯冠丰
 *
 * 为什么需要建造者：
 *   排班计划参数多且大部分可选（医生、日期范围、每周哪几天、时段集合、
 *   每时段号数、是否跳过周末）。用重叠构造器会产生大量重载，调用方容易
 *   把相邻的两个 int 参数传反，且强迫传 null。建造者链式设置可读性强，
 *   build() 时统一校验，构建与表示分离。
 *
 * 收益（写进报告）：
 *   1. 可选参数不再强迫传 null，未设置的项走默认值
 *   2. 参数含义一目了然，杜绝"连续同类型参数传反"的隐患
 *   3. 校验集中在 build()，非法计划根本无法被构造出来
 */
public class SchedulePlan {

    private final Long doctorId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String> timeSlots;      // 哪些时段放号
    private final int countPerSlot;            // 每时段号数
    private final boolean skipWeekend;         // 是否跳过周末

    /** 构造器私有：只能通过 Builder 创建，保证校验必然执行 */
    private SchedulePlan(Builder b) {
        this.doctorId = b.doctorId;
        this.startDate = b.startDate;
        this.endDate = b.endDate;
        this.timeSlots = b.timeSlots;
        this.countPerSlot = b.countPerSlot;
        this.skipWeekend = b.skipWeekend;
    }

    /** 展开计划：生成 放号日期 x 时段 的笛卡尔积明细（周末按需过滤） */
    public List<Schedule> expand() {
        List<Schedule> result = new ArrayList<>();
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            if (!(skipWeekend
                    && (d.getDayOfWeek() == DayOfWeek.SATURDAY
                    || d.getDayOfWeek() == DayOfWeek.SUNDAY))) {
                for (String slot : timeSlots) {
                    Schedule s = new Schedule();
                    s.setDoctorId(doctorId);
                    s.setWorkDate(d);
                    s.setTimeSlot(slot);
                    s.setTotalCount(countPerSlot);
                    s.setRemainCount(countPerSlot);
                    s.setStatus(Schedule.STATUS_OPEN);
                    result.add(s);
                }
            }
            d = d.plusDays(1);
        }
        return result;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<String> getTimeSlots() {
        return timeSlots;
    }

    public int getCountPerSlot() {
        return countPerSlot;
    }

    public boolean isSkipWeekend() {
        return skipWeekend;
    }

    public static class Builder {

        private Long doctorId;
        private LocalDate startDate;
        private LocalDate endDate;
        private final List<String> timeSlots = new ArrayList<>();
        private int countPerSlot = 20;          // 默认每时段 20 个号
        private boolean skipWeekend = false;

        public Builder doctor(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder dateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }

        public Builder addTimeSlot(String timeSlot) {
            this.timeSlots.add(timeSlot);
            return this;
        }

        public Builder countPerSlot(int countPerSlot) {
            this.countPerSlot = countPerSlot;
            return this;
        }

        public Builder skipWeekend(boolean skipWeekend) {
            this.skipWeekend = skipWeekend;
            return this;
        }

        /** 构建时统一校验，非法配置直接失败，绝不带病投产 */
        public SchedulePlan build() {
            if (doctorId == null) {
                throw new IllegalArgumentException("排班计划必须指定医生");
            }
            if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("排班日期范围不合法");
            }
            if (timeSlots.isEmpty()) {
                throw new IllegalArgumentException("至少指定一个放号时段");
            }
            if (countPerSlot <= 0 || countPerSlot > 200) {
                throw new IllegalArgumentException("每时段号数必须在 1~200 之间");
            }
            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("排班起始日期不能早于今天");
            }
            return new SchedulePlan(this);
        }
    }
}
