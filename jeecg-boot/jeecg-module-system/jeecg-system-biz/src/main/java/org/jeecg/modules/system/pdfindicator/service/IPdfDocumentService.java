package org.jeecg.modules.system.pdfindicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.pdfindicator.entity.PdfDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @Description: PDF文档表
 * @Author: jeecg-boot
 * @Date:   2025-06-13
 * @Version: V1.0
 */
public interface IPdfDocumentService extends IService<PdfDocument> {

    /**
     * 上传PDF文件并保存到数据库
     * @param file PDF文件
     * @return 保存后的PDF文档信息
     * @throws IOException 文件处理异常
     */
    PdfDocument uploadPdf(MultipartFile file) throws IOException;

    /**
     * 解析PDF文件内容
     * @param pdfId PDF文档ID
     * @return 解析后的PDF内容
     * @throws Exception 解析异常
     */
    Map<String, Object> parsePdfContent(String pdfId) throws Exception;

    /**
     * 从PDF中抽取指标金额
     * @param pdfId PDF文档ID
     * @return 抽取的指标金额结果
     * @throws Exception 抽取异常
     */
    Map<String, Object> extractIndicators(String pdfId) throws Exception;

    /**
     * 智能校验PDF指标金额
     * @param pdfId PDF文档ID
     * @return 校验结果
     * @throws Exception 校验异常
     */
    Map<String, Object> validateIndicators(String pdfId) throws Exception;

}
