package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 生成请求实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class GenerateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 优化后的提示词
     */
    private String promptOptimized;

    /**
     * 上下文切片列表
     */
    private List<SearchResult.SearchHit> contextSlices;

    /**
     * 生成模式（strict：严谨模式，creative：创意模式）
     */
    private String mode;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 是否启用流式返回
     */
    private Boolean stream;

    /**
     * 生成类型（summary：摘要，qa：问答，points：要点列举，instruction：指令化输出）
     */
    private String generateType;

    /**
     * 语言要求
     */
    private String language;

    /**
     * 风格要求
     */
    private String style;
}
