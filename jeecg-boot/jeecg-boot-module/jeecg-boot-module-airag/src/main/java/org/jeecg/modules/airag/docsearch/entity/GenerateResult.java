package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 生成结果实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class GenerateResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 生成结果内容
     */
    private String content;

    /**
     * 生成耗时（毫秒）
     */
    private Long generateTime;

    /**
     * 引用的切片列表
     */
    private List<ReferenceSlice> references;

    /**
     * 事实性验证结果
     */
    private FactCheckResult factCheckResult;

    /**
     * 生成请求ID
     */
    private String requestId;

    /**
     * 是否为流式返回的中间结果
     */
    private Boolean isPartial;

    /**
     * 引用切片
     */
    @Data
    public static class ReferenceSlice implements Serializable {
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
         * 引用内容
         */
        private String content;

        /**
         * 引用位置
         */
        private String position;

        /**
         * 相似度
         */
        private Float similarity;
    }

    /**
     * 事实性验证结果
     */
    @Data
    public static class FactCheckResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 验证状态（PASS：通过，FAIL：失败，WARNING：警告）
         */
        private String status;

        /**
         * 验证得分
         */
        private Float score;

        /**
         * 验证说明
         */
        private String explanation;

        /**
         * 冲突的切片列表
         */
        private List<String> conflictingSlices;
    }
}
