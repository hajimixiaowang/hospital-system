package com.hospital.user;

import com.hospital.common.BizException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(String username, String password, HttpSession session) {
        User user = userService.login(username, password);
        session.setAttribute("loginUser", user);
        if (User.ROLE_DOCTOR.equals(user.getRole())) {
            return "redirect:/doctor";
        }
        if (User.ROLE_ADMIN.equals(user.getRole())) {
            return "redirect:/admin";
        }
        return "redirect:/patient";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(String username, String password, String realName,
                             String phone, String patientType, Model model) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new BizException("用户名和密码不能为空");
        }
        userService.register(username, password, realName, phone, patientType);
        model.addAttribute("msg", "注册成功，请登录");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
