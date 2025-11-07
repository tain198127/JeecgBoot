package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * @Description: 搜索请求实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 查询词
     */
    private String query;

    /**
     * 过滤条件
     */
    private Map<String, Object> filters;

    /**
     * 返回结果数量
     */
    private Integer topN;

    /**
     * 融合方法（RRF： reciprocal rank fusion，LINEAR：线性加权，LEARNED：学习排序）
     */
    private String fuseMethod;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 是否启用提示词优化
     */
    private Boolean enableQueryRewrite;

    /**
     * 是否启用严谨模式
     */
    private Boolean strictMode;
}
