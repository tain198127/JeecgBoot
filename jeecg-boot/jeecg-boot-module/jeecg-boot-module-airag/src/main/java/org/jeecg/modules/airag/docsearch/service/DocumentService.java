package org.jeecg.modules.airag.docsearch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.docsearch.entity.Document;
import org.jeecg.modules.airag.docsearch.entity.DocumentUploadRequest;
import org.jeecg.modules.airag.docsearch.entity.DocumentUploadResponse;

/**
 * @Description: 文档服务接口
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
public interface DocumentService extends IService<Document> {

    /**
     * 上传并解析文档
     * @param uploadRequest 文档上传请求
     * @return 文档上传响应
     */
    DocumentUploadResponse uploadAndParseDocument(DocumentUploadRequest uploadRequest);

    /**
     * 根据文档ID获取文档信息
     * @param docId 文档唯一标识
     * @return 文档信息
     */
    Document getDocumentByDocId(String docId);

    /**
     * 更新文档状态
     * @param docId 文档唯一标识
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateDocumentStatus(String docId, Integer status);

    /**
     * 删除文档
     * @param docId 文档唯一标识
     * @return 是否删除成功
     */
    boolean deleteDocument(String docId);

    /**
     * 批量删除文档
     * @param docIds 文档唯一标识列表
     * @return 删除成功的数量
     */
    int batchDeleteDocuments(String[] docIds);

    /**
     * 重试文档解析
     * @param docId 文档唯一标识
     * @return 文档上传响应
     */
    DocumentUploadResponse retryParseDocument(String docId);
}
