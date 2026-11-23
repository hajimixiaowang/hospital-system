package com.hospital.schedule;

import java.time.LocalDate;

/**
 * 【设计模式·单例】号源唯一生成器 —— 负责人：冯冠丰
 *
 * 为什么需要单例：
 *   号源编号必须全局唯一。如果生成器被多处实例化，各自的内存序号互相不可见，
 *   批量放号时会产出重号。全局唯一实例 + 按天序号段分配，才能保证不重号。
 *
 * 实现要点（答辩高频问题）：
 *   1. 构造器私有，杜绝外部 new
 *   2. volatile 禁止指令重排：防止"分配内存→初始化→赋值引用"被重排成
 *      "分配内存→赋值引用(未初始化)"，导致别的线程拿到半成品对象
 *   3. 双重检查锁：第一重避免每次都加锁，第二重防止竞态下重复创建
 *   4. 数据库 source_no 唯一索引兜底，单例 + 唯一约束双保险
 */
public class NumberSourceGenerator {

    /** 步骤2：volatile 禁止指令重排 */
    private static volatile NumberSourceGenerator instance;

    /** 内存序号段：key = yyyyMMdd-科室id，value = 当日已分配序号 */
    private final java.util.Map<String, Integer> sequence = new java.util.concurrent.ConcurrentHashMap<>();

    /** 步骤1：私有构造 */
    private NumberSourceGenerator() {
    }

    /** 双重检查锁获取全局唯一实例 */
    public static NumberSourceGenerator getInstance() {
        if (instance == null) {                 // 第一重检查：已有实例直接返回，无锁开销
            synchronized (NumberSourceGenerator.class) {
                if (instance == null) {         // 第二重检查：拿到锁后可能已被别的线程创建
                    instance = new NumberSourceGenerator();
                }
            }
        }
        return instance;
    }

    /**
     * 生成号源编号：NS + yyyyMMdd - 科室id(两位) - 4位序号
     * 例：NS20260101-01-0007
     */
    public String nextSourceNo(LocalDate workDate, Long deptId) {
        String date = workDate.toString().replace("-", "");
        String key = date + "-" + deptId;
        // ConcurrentHashMap 原子自增，同一 key 并发分配也不重号
        int seq = sequence.merge(key, 1, Integer::sum);
        return String.format("NS%s-%02d-%04d", date, deptId, seq);
    }

    /** 仅供单元测试观察内部序号段 */
    java.util.Map<String, Integer> getSequenceMapForTest() {
        return sequence;
    }
}
