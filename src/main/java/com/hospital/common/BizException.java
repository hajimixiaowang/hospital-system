package com.hospital.common;

/** 业务异常：所有"可预期的业务错误"统一抛这个，由全局异常处理器转成提示页 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
