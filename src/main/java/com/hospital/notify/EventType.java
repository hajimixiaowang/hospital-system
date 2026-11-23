package com.hospital.notify;

/** 事件类型：挂号业务只管广播，语义在此集中定义 */
public enum EventType {

    /** 预约成功 */
    BOOKED,

    /** 预约取消 */
    CANCELLED,

    /** 就诊提醒 */
    REMIND,

    /** 就诊完成 */
    VISIT_FINISHED
}
