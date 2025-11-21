package org.jeecg.modules.system.pdfindicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.system.pdfindicator.entity.PdfDocument;
import org.jeecg.modules.system.pdfindicator.mapper.PdfDocumentMapper;
import org.jeecg.modules.system.pdfindicator.service.IPdfDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * @Description: PDF文档表
 * @Author: jeecg-boot
 * @Date:   2025-06-13
 * @Version: V1.0
 */
@Service
public class PdfDocumentServiceImpl extends ServiceImpl<PdfDocumentMapper, PdfDocument> implements IPdfDocumentService {

    @Value("${jeecg.pdf.upload.path}")
    private String pdfUploadPath;

    @Autowired
    private PdfDocumentMapper pdfDocumentMapper;

    @Override
    public PdfDocument uploadPdf(MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的PDF文件不能为空");
        }

        // 检查文件类型是否为PDF
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("只能上传PDF类型的文件");
        }

        // 创建上传目录（如果不存在）
        File uploadDir = new File(pdfUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".pdf";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // 保存文件到本地
        Path filePath = Paths.get(pdfUploadPath, uniqueFilename);
        Files.write(filePath, file.getBytes());

        // 创建PDF文档记录
        PdfDocument pdfDocument = new PdfDocument();
        pdfDocument.setFileName(originalFilename);
        pdfDocument.setFilePath(filePath.toString());
        pdfDocument.setFileSize(file.getSize());
        pdfDocument.setUploadTime(new Date());
        pdfDocument.setParseStatus(0);
        pdfDocument.setErrorMessage(null);

        // 保存到数据库
        save(pdfDocument);

        return pdfDocument;
    }

    @Override
    public Map<String, Object> parsePdfContent(String pdfId) throws Exception {
        // 根据ID查询PDF文档
        PdfDocument pdfDocument = getById(pdfId);
        if (pdfDocument == null) {
            throw new IllegalArgumentException("PDF文档不存在");
        }

        String filePath = pdfDocument.getDocumentPath();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("PDF文件不存在：" + filePath);
        }

        // 使用Apache PDFBox解析PDF内容
        String content = "";
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                content = stripper.getText(document);
            } else {
                throw new IOException("PDF文件已加密，无法解析：" + filePath);
            }
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("pdfId", pdfId);
        result.put("fileName", pdfDocument.getFileName());
        result.put("fileSize", pdfDocument.getFileSize());
        result.put("parseStatus", "success");
        result.put("content", content);
        result.put("message", "PDF解析成功");

        // 更新PDF文档状态为已解析
        pdfDocument.setParseStatus(2);
        updateById(pdfDocument);

        return result;
    }

    @Override
    public Map<String, Object> extractIndicators(String pdfId) throws Exception {
        // 根据ID查询PDF文档
        PdfDocument pdfDocument = getById(pdfId);
        if (pdfDocument == null) {
            throw new IllegalArgumentException("PDF文档不存在");
        }

        if (pdfDocument.getParseStatus() != 2) {
            throw new IllegalStateException("PDF文档未解析，无法抽取指标：" + pdfId);
        }

        String filePath = pdfDocument.getDocumentPath();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("PDF文件不存在：" + filePath);
        }

        // 使用Apache PDFBox解析PDF内容
        String content = "";
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                content = stripper.getText(document);
            } else {
                throw new IOException("PDF文件已加密，无法解析：" + filePath);
            }
        }

        if (content == null || content.isEmpty()) {
            throw new IOException("PDF文档内容为空，无法抽取指标：" + pdfId);
        }

        // 抽取指标 - 使用正则表达式从PDF内容中提取关键指标
        Map<String, String> indicators = new HashMap<>();

        // 示例：提取合同金额（假设格式为"合同金额：XXX元"或"总金额：XXX元"）
        Pattern amountPattern = Pattern.compile("(合同金额|总金额\\d,]+\\.?\\d*)元");
        Matcher amountMatcher = amountPattern.matcher(content);
        if (amountMatcher.find()) {
            indicators.put("contractAmount", amountMatcher.group(2).replace(",", ""));
        }

        // 示例：提取合同编号（假设格式为"合同编号：XXX"或"编号：XXX"）
        Pattern contractNoPattern = Pattern.compile("(合同编号|编号\\w+)");
        Matcher contractNoMatcher = contractNoPattern.matcher(content);
        if (contractNoMatcher.find()) {
            indicators.put("contractNo", contractNoMatcher.group(2));
        }

        // 示例：提取签订日期（假设格式为"签订日期：XXXX年XX月XX日"）
        Pattern datePattern = Pattern.compile("签订日期：(\\d{4}年\\d{1,2}月\\d{1,2}日)");
        Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
            indicators.put("signingDate", dateMatcher.group(1));
        }

        // 示例：提取甲方（假设格式为"甲方：XXX"）
        Pattern partyAPattern = Pattern.compile("甲方：([^\n]+)");
        Matcher partyAMatcher = partyAPattern.matcher(content);
        if (partyAMatcher.find()) {
            indicators.put("partyA", partyAMatcher.group(1).trim());
        }

        // 示例：提取乙方（假设格式为"乙方：XXX"）
        Pattern partyBPattern = Pattern.compile("乙方：([^\n]+)");
        Matcher partyBMatcher = partyBPattern.matcher(content);
        if (partyBMatcher.find()) {
            indicators.put("partyB", partyBMatcher.group(1).trim());
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("pdfId", pdfId);
        result.put("fileName", pdfDocument.getFileName());
        result.put("extractStatus", "success");
        result.put("indicators", indicators);
        result.put("message", "指标抽取成功");

        // 更新PDF文档状态为已抽取
        pdfDocument.setParseStatus(3);
        updateById(pdfDocument);

        return result;
    }

    @Override
    public Map<String, Object> validateIndicators(String pdfId) throws Exception {
        PdfDocument pdfDocument = this.getById(pdfId);
        if (pdfDocument == null) {
            throw new IllegalArgumentException("PDF文档不存在：" + pdfId);
        }

        if (pdfDocument.getParseStatus() != 3) {
            throw new IllegalStateException("PDF文档未抽取指标，无法进行智能校验：" + pdfId);
        }

        // 获取抽取的指标
        Map<String, Object> indicatorsResult = this.extractIndicators(pdfId);
        Map<String, Object> indicators = (Map<String, Object>) indicatorsResult.get("indicators");
        if (indicators == null || indicators.isEmpty()) {
            throw new IOException("PDF文档未抽取到指标，无法进行智能校验：" + pdfId);
        }

        // 智能校验 - 对抽取的指标进行校验
        Map<String, Object> validationResult = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 示例：校验合同金额是否为数字
        if (indicators.containsKey("contractAmount")) {
            String amountStr = (String) indicators.get("contractAmount");
            try {
                Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                errors.add("合同金额格式不正确：" + amountStr);
            }
        }

        // 示例：校验合同编号是否符合格式（假设格式为"CONTRACT-YYYY-MM"）
        if (indicators.containsKey("contractNo")) {
            String contractNo = (String) indicators.get("contractNo");
            Pattern contractNoPattern = Pattern.compile("CONTRACT-\\d{4}-\\d{2}");
            if (!contractNoPattern.matcher(contractNo).matches()) {
                warnings.add("合同编号格式可能不正确：" + contractNo);
            }
        }

        // 示例：校验签订日期是否符合格式（假设格式为"XXXX年XX月XX日"）
        if (indicators.containsKey("signingDate")) {
            String signingDate = (String) indicators.get("signingDate");
            Pattern datePattern = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日");
            if (!datePattern.matcher(signingDate).matches()) {
                errors.add("签订日期格式不正确：" + signingDate);
            }
        }

        // 示例：校验甲方和乙方是否不为空
        if (!indicators.containsKey("partyA") || indicators.get("partyA") == null || indicators.get("partyA").toString().isEmpty()) {
            errors.add("甲方不能为空");
        }

        if (!indicators.containsKey("partyB") || indicators.get("partyB") == null || indicators.get("partyB").toString().isEmpty()) {
            errors.add("乙方不能为空");
        }

        // 构建校验结果
        validationResult.put("pdfId", pdfId);
        validationResult.put("fileName", pdfDocument.getFileName());
        validationResult.put("indicators", indicators);
        validationResult.put("errors", errors);
        validationResult.put("warnings", warnings);
        validationResult.put("validateStatus", errors.isEmpty() ? "success" : "failure");
        validationResult.put("message", errors.isEmpty() ? "智能校验通过" : "智能校验发现错误");

        // 更新PDF文档状态为已校验
        pdfDocument.setParseStatus(4);
        this.updateById(pdfDocument);

        return validationResult;
    }
}
