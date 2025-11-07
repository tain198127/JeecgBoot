package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;

import java.util.List;

/**
 * @Description: 文档切片结果
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class SliceResult {
    /**
     * 文档ID
     */
    private String docId;
    
    /**
     * 切片列表
     */
    private List<Slice> slices;
    
    /**
     * 切片统计信息
     */
    private SliceStatistics statistics;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 切片统计信息内部类
     */
    @Data
    public static class SliceStatistics {
        /**
         * 总切片数
         */
        private int totalSlices;
        
        /**
         * 平均切片长度
         */
        private int averageSliceLength;
        
        /**
         * 最大切片长度
         */
        private int maxSliceLength;
        
        /**
         * 最小切片长度
         */
        private int minSliceLength;
        
        /**
         * 重叠率
         */
        private double overlapRatio;
        
        /**
         * 覆盖率
         */
        private double coverageRatio;
    }
}