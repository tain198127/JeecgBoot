package org.jeecg.modules.airag.docsearch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.airag.docsearch.entity.Document;
import org.jeecg.modules.airag.docsearch.entity.DocumentUploadRequest;
import org.jeecg.modules.airag.docsearch.entity.DocumentUploadResponse;
import org.jeecg.modules.airag.docsearch.entity.Slice;
import org.jeecg.modules.airag.docsearch.entity.SliceConfig;
import org.jeecg.modules.airag.docsearch.mapper.DocumentMapper;
import org.jeecg.modules.airag.docsearch.service.DocumentService;
import org.jeecg.modules.airag.docsearch.service.SliceService;
import org.jeecg.modules.airag.llm.document.TikaDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Description: 文档服务实现类
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Autowired
    private TikaDocumentParser tikaDocumentParser;
    
    @Autowired
    private SliceService sliceService;

    @Override
    public DocumentUploadResponse uploadAndParseDocument(DocumentUploadRequest uploadRequest) {
        DocumentUploadResponse response = new DocumentUploadResponse();
        MultipartFile file = uploadRequest.getFile();
        
        try {
            // 1. 生成唯一标识
            String docId = UUID.randomUUID().toString().replace("-", "");
            
            // 2. 保存文件到临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            String originalFilename = file.getOriginalFilename();
            String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            File tempFile = new File(tempDir, docId + "." + fileType);
            file.transferTo(tempFile);
            
            // 3. 创建文档实体
            Document document = new Document()
                    .setDocId(docId)
                    .setTitle(uploadRequest.getTitle() != null ? uploadRequest.getTitle() : originalFilename)
                    .setOriginalName(originalFilename)
                    .setFilePath(tempFile.getAbsolutePath())
                    .setFileSize(file.getSize())
                    .setFileType(fileType)
                    .setUserId(uploadRequest.getUserId())
                    .setTenantId(uploadRequest.getTenantId())
                    .setParseStatus(1) // 解析中
                    .setSliceStatus(0) // 未切片
                    .setIndexStatus(0) // 未索引
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            
            // 4. 保存文档信息到数据库
            save(document);
            
            // 5. 异步解析文档
            parseDocumentAsync(docId, uploadRequest.getSliceConfig());
            
            // 6. 构建响应
            response.setDocId(docId)
                    .setTitle(document.getTitle())
                    .setUploadStatus("success")
                    .setMessage("文档上传成功，正在后台解析");
            
        } catch (IOException e) {
            logger.error("文档上传失败: {}", e.getMessage(), e);
            response.setUploadStatus("failed")
                    .setMessage("文档上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("文档处理失败: {}", e.getMessage(), e);
            response.setUploadStatus("failed")
                    .setMessage("文档处理失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 异步解析文档
     * @param docId 文档ID
     * @param sliceConfig 切片配置
     */
    @Async
    private void parseDocumentAsync(String docId, SliceConfig sliceConfig) {
        try {
            // 1. 获取文档信息
            Document document = getDocumentByDocId(docId);
            if (document == null) {
                logger.error("文档不存在: {}", docId);
                return;
            }
            
            // 2. 解析文档
            File file = new File(document.getFilePath());
            dev.langchain4j.data.document.Document parsedDoc = tikaDocumentParser.parse(file);
            
            // 3. 更新文档解析状态
            document.setParseStatus(2) // 解析成功
                    .setUpdateTime(new Date());
            updateById(document);
            
            // 4. 文档切片
            List<Slice> slices = sliceService.sliceDocument(docId, sliceConfig);
            
            // 5. 更新文档切片状态
            document.setSliceStatus(2) // 切片成功
                    .setSliceCount(slices.size())
                    .setUpdateTime(new Date());
            updateById(document);
            
            logger.info("文档解析和切片完成: {}, 生成 {} 个切片", docId, slices.size());
            
        } catch (Exception e) {
            logger.error("文档解析失败: {}", docId, e);
            
            // 更新文档状态为解析失败
            Document document = new Document()
                    .setDocId(docId)
                    .setParseStatus(3) // 解析失败
                    .setUpdateTime(new Date());
            updateById(document);
        }
    }

    @Override
    public Document getDocumentByDocId(String docId) {
        return lambdaQuery()
                .eq(Document::getDocId, docId)
                .one();
    }

    @Override
    public boolean updateDocumentStatus(String docId, Integer status) {
        Document document = new Document()
                .setDocId(docId)
                .setParseStatus(status)
                .setUpdateTime(new Date());
        return updateById(document);
    }

    @Override
    public boolean deleteDocument(String docId) {
        // 1. 删除文档信息
        boolean deleted = remove(lambdaQuery().eq(Document::getDocId, docId));
        
        // 2. 删除相关切片
        // TODO: 实现删除切片逻辑
        
        return deleted;
    }

    @Override
    public int batchDeleteDocuments(String[] docIds) {
        // 1. 批量删除文档信息
        int deleted = remove(lambdaQuery().in(Document::getDocId, docIds)).intValue();
        
        // 2. 批量删除相关切片
        // TODO: 实现批量删除切片逻辑
        
        return deleted;
    }

    @Override
    public DocumentUploadResponse retryParseDocument(String docId) {
        // TODO: 实现重试解析逻辑
        return null;
    }
}