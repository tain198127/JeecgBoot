package org.jeecg.modules.airag.docsearch.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.io.Serializable;

/**
 * @Description: 文档上传请求实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
public class DocumentUploadRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文档文件
     */
    private MultipartFile file;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 切片策略配置
     */
    private SliceConfig sliceConfig;

    /**
     * 是否启用干跑模式（不入库只统计）
     */
    private Boolean dryRun;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 业务标签
     */
    private String tags;
}
