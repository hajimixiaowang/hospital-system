package com.hospital.common;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** 全局异常处理：业务异常转成友好提示页，未知异常统一兜底 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public String handleBiz(BizException e, Model model) {
        model.addAttribute("msg", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleOther(Exception e, Model model) {
        model.addAttribute("msg", "系统异常：" + e.getMessage());
        return "error";
    }
}
