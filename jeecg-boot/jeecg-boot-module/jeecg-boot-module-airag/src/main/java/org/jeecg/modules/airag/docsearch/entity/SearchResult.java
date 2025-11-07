package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 搜索结果实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 搜索结果列表
     */
    private List<SearchHit> hits;

    /**
     * 搜索耗时（毫秒）
     */
    private Long searchTime;

    /**
     * 融合耗时（毫秒）
     */
    private Long fuseTime;

    /**
     * 总命中数
     */
    private Long totalHits;

    /**
     * 优化后的查询词
     */
    private String optimizedQuery;

    /**
     * 查询优化理由
     */
    private String queryRewriteReason;

    /**
     * 搜索请求ID
     */
    private String requestId;

    /**
     * 搜索结果项
     */
    @Data
    public static class SearchHit implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 文档唯一标识
         */
        private String docId;

        /**
         * 切片唯一标识
         */
        private String sliceId;

        /**
         * 切片内容
         */
        private String content;

        /**
         * 原始分数
         */
        private Float score;

        /**
         * 融合后的排名
         */
        private Integer rank;

        /**
         * 来源（ES：Elasticsearch，MILVUS：Milvus，FUSED：融合结果）
         */
        private String source;

        /**
         * 元数据
         */
        private String metadata;

        /**
         * 章节信息
         */
        private String chapter;

        /**
         * 段落索引
         */
        private Integer paragraphIndex;

        /**
         * 相似度
         */
        private Float similarity;
    }

    /**
     * 查询重写结果
     */
    @Data
    public static class QueryRewriteResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 原始查询词
         */
        private String originalQuery;

        /**
         * 优化后的查询词
         */
        private String optimizedQuery;

        /**
         * 优化理由
         */
        private String reason;

        /**
         * 优化后的检索意图
         */
        private String intent;

        /**
         * 必须包含的关键词
         */
        private List<String> mustInclude;

        /**
         * 必须排除的关键词
         */
        private List<String> mustExclude;
    }
}
