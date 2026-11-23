package com.hospital.common;

import com.hospital.setting.SettingService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局 Model 注入：所有页面自动获得系统设置里的品牌信息。
 * （ hospitalName / servicePhone / hospitalAddress 供侧边栏、页脚等直接引用 ）
 */
@ControllerAdvice
public class GlobalModelConfig {

    private final SettingService settingService;

    public GlobalModelConfig(SettingService settingService) {
        this.settingService = settingService;
    }

    @ModelAttribute
    public void addCommonAttrs(Model model) {
        model.addAttribute("hospitalName", settingService.get("hospital_name", "仁和医院"));
        model.addAttribute("servicePhone", settingService.get("service_phone", ""));
        model.addAttribute("hospitalAddress", settingService.get("hospital_address", ""));
    }
}
