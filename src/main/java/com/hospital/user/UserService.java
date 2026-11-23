package com.hospital.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.common.BizException;
import com.hospital.common.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 登录校验：成功返回用户（密码已置空，避免放session泄密） */
    public User login(String username, String password) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null || !user.getPassword().equals(PasswordUtil.sha256(password))) {
            throw new BizException("用户名或密码错误");
        }
        user.setPassword(null);
        return user;
    }

    /** 患者注册 */
    public void register(String username, String password, String realName,
                         String phone, String patientType) {
        Long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", username));
        if (count > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.sha256(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(User.ROLE_PATIENT);
        user.setPatientType(patientType == null || patientType.isEmpty()
                ? User.PATIENT_NORMAL : patientType);
        userMapper.insert(user);
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
