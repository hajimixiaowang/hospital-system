package com.hospital.common;

import com.hospital.user.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录与角色拦截器：
 * 1. 未登录一律踢回登录页
 * 2. /patient/** 只允许患者，/doctor/** 只允许医生，/admin/** 只允许管理员
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        Object loginUser = request.getSession().getAttribute("loginUser");
        String uri = request.getRequestURI();

        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        User user = (User) loginUser;
        if (uri.startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            response.sendError(403, "无权限访问管理员页面");
            return false;
        }
        if (uri.startsWith("/doctor") && !"DOCTOR".equals(user.getRole())) {
            response.sendError(403, "无权限访问医生页面");
            return false;
        }
        if (uri.startsWith("/patient") && !"PATIENT".equals(user.getRole())) {
            response.sendError(403, "无权限访问患者页面");
            return false;
        }
        return true;
    }
}
