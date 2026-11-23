package com.hospital.knowledge;

import com.hospital.setting.SettingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手问答服务（检索式问答）
 *
 * 实现思路（答辩讲法）：基于知识库的关键词加权匹配——
 *   1. 把用户问题与每条知识的"关键词列表"做包含匹配
 *   2. 命中一个关键词得"关键词长度"分（长词权重高，减少误命中）
 *   3. 取得分最高的知识条目作答；零分则返回兜底话术
 * 知识库由管理员在后台维护（知识库管理），新增条目即时生效，无需改代码——
 * 这正是"数据驱动"的思想：问答能力随知识库增长。
 */
@Service
public class AiService {

    private final KnowledgeMapper knowledgeMapper;
    private final SettingService settingService;

    public AiService(KnowledgeMapper knowledgeMapper, SettingService settingService) {
        this.knowledgeMapper = knowledgeMapper;
        this.settingService = settingService;
    }

    /** 问答入口：返回回答文本 */
    public Map<String, String> ask(String question) {
        Map<String, String> result = new HashMap<>();
        if (question == null || question.trim().isEmpty()) {
            result.put("answer", "请输入您想咨询的问题，例如：怎么挂号？挂号费多少钱？");
            result.put("matched", "");
            return result;
        }
        List<Knowledge> all = knowledgeMapper.selectList(null);
        Knowledge best = null;
        int bestScore = 0;
        for (Knowledge k : all) {
            int score = 0;
            for (String kw : k.getKeywords().split("[,，\\s、]+")) {
                if (kw.isEmpty()) continue;
                if (question.contains(kw)) {
                    score += kw.length();   // 长关键词权重更高
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = k;
            }
        }
        if (best != null) {
            result.put("answer", best.getAnswer());
            result.put("matched", best.getCategory());
        } else {
            String phone = settingService.get("service_phone", "服务台");
            result.put("answer", "抱歉，小医暂时没有理解您的问题。您可以换个说法再问一次，" +
                    "或拨打客服电话 " + phone + "，也可以到门诊大厅导诊台咨询。");
            result.put("matched", "");
        }
        return result;
    }
}
