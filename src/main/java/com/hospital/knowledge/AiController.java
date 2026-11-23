package com.hospital.knowledge;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * AI 助手问答接口（患者端 / 医生端共用）。
 * 前端聊天界面通过 AJAX 调用，返回 JSON。
 */
@Controller
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /** 问答接口：请求体 {"question": "..."}，响应 {"answer": "...", "matched": "分类"} */
    @PostMapping("/assistant/ask")
    @ResponseBody
    public Map<String, String> ask(@RequestBody Map<String, String> body) {
        return aiService.ask(body == null ? null : body.get("question"));
    }
}
