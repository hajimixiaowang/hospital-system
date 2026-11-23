package com.hospital.setting;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 系统设置读写 */
@Service
public class SettingService {

    private final SettingMapper settingMapper;

    public SettingService(SettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    /** 全部设置项 */
    public List<Setting> listAll() {
        return settingMapper.selectList(null);
    }

    /** 取单项设置值（不存在返回默认值） */
    public String get(String key, String defaultValue) {
        List<Setting> all = settingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Setting>()
                        .eq("setting_key", key));
        return all.isEmpty() ? defaultValue : all.get(0).getSettingValue();
    }

    /** 批量保存（页面提交 key=value 的表单） */
    public void saveAll(Map<String, String> values) {
        for (Map.Entry<String, String> e : values.entrySet()) {
            List<Setting> found = settingMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Setting>()
                            .eq("setting_key", e.getKey()));
            if (!found.isEmpty()) {
                Setting s = found.get(0);
                s.setSettingValue(e.getValue());
                settingMapper.updateById(s);
            }
        }
    }

    /** 快捷访问（供全局品牌注入） */
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        for (Setting s : listAll()) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }
}
