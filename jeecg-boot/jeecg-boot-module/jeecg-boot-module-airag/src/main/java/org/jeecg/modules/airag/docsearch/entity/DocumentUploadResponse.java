package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * @Description: 文档上传响应实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class DocumentUploadResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文档唯一标识
     */
    private String docId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 上传状态
     */
    private String status;

    /**
     * 解析统计信息
     */
    private ParseStatistics parseStats;

    /**
     * 切片统计信息
     */
    private SliceStatistics sliceStats;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 解析统计信息
     */
    @Data
    public static class ParseStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 总章节数
         */
        private Integer totalChapters;

        /**
         * 总段落数
         */
        private Integer totalParagraphs;

        /**
         * 总表格数
         */
        private Integer totalTables;

        /**
         * 总脚注数
         */
        private Integer totalFootnotes;

        /**
         * 无法解析的对象数
         */
        private Integer unparseableObjects;

        /**
         * 解析耗时（毫秒）
         */
        private Long parseDuration;
    }

    /**
     * 切片统计信息
     */
    @Data
    public static class SliceStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 总切片数
         */
        private Integer totalSlices;

        /**
         * 平均切片长度
         */
        private Integer avgSliceLength;

        /**
         * 最大切片长度
         */
        private Integer maxSliceLength;

        /**
         * 最小切片长度
         */
        private Integer minSliceLength;

        /**
         * 重叠率
         */
        private Float overlapRatio;

        /**
         * 切片耗时（毫秒）
         */
        private Long sliceDuration;
    }
}
