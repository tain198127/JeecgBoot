package org.jeecg.modules.airag.docsearch.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.docsearch.entity.*;
import org.jeecg.modules.airag.docsearch.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 文档搜索控制器
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@RestController
@RequestMapping("/airag/docsearch")
public class DocSearchController {

    private static final Logger logger = LoggerFactory.getLogger(DocSearchController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private SliceService sliceService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GenerateService generateService;

    /**
     * 上传并解析文档
     * @param file 上传的文件
     * @param uploadRequest 上传请求参数
     * @return 上传响应
     */
    @PostMapping("/upload")
    public Result<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file, DocumentUploadRequest uploadRequest) {
        try {
            // 调用文档服务上传并解析文档
            DocumentUploadResponse response = documentService.uploadAndParseDocument(uploadRequest);
            return Result.OK(response);
        } catch (Exception e) {
            logger.error("文档上传失败: {}", e.getMessage(), e);
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 执行搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/search")
    public Result<SearchResult> search(@RequestBody SearchRequest searchRequest) {
        try {
            // 调用搜索服务执行搜索
            SearchResult result = searchService.search(searchRequest);
            return Result.OK(result);
        } catch (Exception e) {
            logger.error("搜索失败: {}", e.getMessage(), e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 生成内容
     * @param generateRequest 生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<GenerateResult> generate(@RequestBody GenerateRequest generateRequest) {
        try {
            // 调用生成服务生成内容
            GenerateResult result = generateService.generate(generateRequest);
            return Result.OK(result);
        } catch (Exception e) {
            logger.error("内容生成失败: {}", e.getMessage(), e);
            return Result.error("内容生成失败: " + e.getMessage());
        }
    }

    /**
     * 流式生成内容
     * @param generateRequest 生成请求
     * @return 流式生成结果
     */
    @PostMapping("/generate/stream")
    public void generateStream(@RequestBody GenerateRequest generateRequest, GenerateStreamHandler handler) {
        try {
            // 调用生成服务执行流式生成
            generateService.generateStream(generateRequest, handler);
        } catch (Exception e) {
            logger.error("流式生成失败: {}", e.getMessage(), e);
            handler.onError(e);
        }
    }

    /**
     * 根据文档ID获取文档信息
     * @param docId 文档唯一标识
     * @return 文档信息
     */
    @GetMapping("/document/{docId}")
    public Result<Document> getDocument(@PathVariable String docId) {
        try {
            // 调用文档服务获取文档信息
            Document document = documentService.getDocumentByDocId(docId);
            return Result.OK(document);
        } catch (Exception e) {
            logger.error("获取文档信息失败: {}", e.getMessage(), e);
            return Result.error("获取文档信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     * @param docId 文档唯一标识
     * @return 删除结果
     */
    @DeleteMapping("/document/{docId}")
    public Result<Boolean> deleteDocument(@PathVariable String docId) {
        try {
            // 调用文档服务删除文档
            boolean success = documentService.deleteDocument(docId);
            return Result.OK(success);
        } catch (Exception e) {
            logger.error("删除文档失败: {}", e.getMessage(), e);
            return Result.error("删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 重试文档解析
     * @param docId 文档唯一标识
     * @return 重试结果
     */
    @PostMapping("/document/{docId}/retry")
    public Result<DocumentUploadResponse> retryParseDocument(@PathVariable String docId) {
        try {
            // 调用文档服务重试解析
            DocumentUploadResponse response = documentService.retryParseDocument(docId);
            return Result.OK(response);
        } catch (Exception e) {
            logger.error("重试文档解析失败: {}", e.getMessage(), e);
            return Result.error("重试文档解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档切片
     * @param docId 文档唯一标识
     * @return 切片列表
     */
    @GetMapping("/slices/{docId}")
    public Result<List<Slice>> getSlices(@PathVariable String docId) {
        try {
            // 调用切片服务获取文档切片
            List<Slice> slices = sliceService.getSlicesByDocId(docId);
            return Result.OK(slices);
        } catch (Exception e) {
            logger.error("获取文档切片失败: {}", e.getMessage(), e);
            return Result.error("获取文档切片失败: " + e.getMessage());
        }
    }

    /**
     * 优化查询词
     * @param query 原始查询词
     * @return 优化结果
     */
    @PostMapping("/query/optimize")
    public Result<SearchResult.QueryRewriteResult> optimizeQuery(@RequestParam String query) {
        try {
            // 调用搜索服务优化查询词
            SearchResult.QueryRewriteResult result = searchService.optimizeQueryWithReason(query);
            return Result.OK(result);
        } catch (Exception e) {
            logger.error("查询词优化失败: {}", e.getMessage(), e);
            return Result.error("查询词优化失败: " + e.getMessage());
        }
    }
}