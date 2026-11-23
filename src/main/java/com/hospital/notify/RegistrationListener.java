package com.hospital.notify;

/**
 * 【设计模式·观察者】抽象观察者 —— 负责人：陈梓健
 *
 * 接口刻意最小（接口隔离原则）：只有一个 onEvent。
 * 新增通知渠道 = 新增一个实现类并注册进 Spring 容器，发布方零感知（开闭原则）。
 */
public interface RegistrationListener {

    /**
     * 收到挂号相关事件。
     * @return 处理了几条通知（日志统计用）
     */
    int onEvent(RegistrationEvent event);
}
