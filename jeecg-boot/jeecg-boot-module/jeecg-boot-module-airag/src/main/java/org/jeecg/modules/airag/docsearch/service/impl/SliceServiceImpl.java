package org.jeecg.modules.airag.docsearch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.airag.docsearch.entity.Chapter;
import org.jeecg.modules.airag.docsearch.entity.Paragraph;
import org.jeecg.modules.airag.docsearch.entity.Slice;
import org.jeecg.modules.airag.docsearch.entity.SliceConfig;
import org.jeecg.modules.airag.docsearch.mapper.SliceMapper;
import org.jeecg.modules.airag.docsearch.service.SliceService;
import org.jeecg.modules.airag.llm.document.TikaDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Description: 文档切片服务实现类
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Service
public class SliceServiceImpl extends ServiceImpl<SliceMapper, Slice> implements SliceService {

    private static final Logger logger = LoggerFactory.getLogger(SliceServiceImpl.class);

    @Autowired
    private SliceMapper sliceMapper;

    /**
     * 对文档进行切片
     * @param docId 文档唯一标识
     * @param sliceConfig 切片配置
     * @return 切片列表
     */
    @Override
    public List<Slice> sliceDocument(String docId, SliceConfig sliceConfig) {
        logger.info("开始对文档 {} 进行切片，配置：{}", docId, sliceConfig);
        long startTime = System.currentTimeMillis();

        List<Slice> slices = new ArrayList<>();

        try {
            // TODO: 从数据库或文件系统获取文档内容
            // 这里需要根据docId获取文档的结构化内容（章节、段落等）
            // 目前先模拟文档内容
            List<Chapter> chapters = getChaptersByDocId(docId);
            List<Paragraph> paragraphs = getParagraphsByDocId(docId);

            if (sliceConfig.getParagraphMode()) {
                // 按段落切片
                slices = sliceByParagraph(docId, chapters, paragraphs, sliceConfig);
            } else {
                // 按字数切片
                slices = sliceByCharacterCount(docId, chapters, paragraphs, sliceConfig);
            }

            // 保存切片到数据库
            saveBatch(slices);

            long endTime = System.currentTimeMillis();
            logger.info("文档 {} 切片完成，共生成 {} 个切片，耗时 {} ms", docId, slices.size(), endTime - startTime);

        } catch (Exception e) {
            logger.error("文档 {} 切片失败", docId, e);
            throw new RuntimeException("文档切片失败", e);
        }

        return slices;
    }

    /**
     * 按段落切片
     * @param docId 文档唯一标识
     * @param chapters 章节列表
     * @param paragraphs 段落列表
     * @param sliceConfig 切片配置
     * @return 切片列表
     */
    private List<Slice> sliceByParagraph(String docId, List<Chapter> chapters, List<Paragraph> paragraphs, SliceConfig sliceConfig) {
        List<Slice> slices = new ArrayList<>();
        int sliceIndex = 0;
        int overlapWindow = sliceConfig.getOverlapWindow();
        int maxCharsPerSlice = sliceConfig.getMaxCharsPerSlice();

        // 按段落分组，保留章节结构
        for (Chapter chapter : chapters) {
            List<Paragraph> chapterParagraphs = paragraphs.stream()
                    .filter(p -> p.getChapterId().equals(chapter.getChapterId()))
                    .sorted((p1, p2) -> Integer.compare(p1.getOrderNum(), p2.getOrderNum()))
                    .toList();

            for (int i = 0; i < chapterParagraphs.size(); i++) {
                Paragraph currentPara = chapterParagraphs.get(i);
                StringBuilder sliceContent = new StringBuilder(currentPara.getContent());
                int offsetStart = currentPara.getOffsetStart();
                int offsetEnd = currentPara.getOffsetEnd();

                // 处理重叠
                if (overlapWindow > 0) {
                    // 向前重叠
                    for (int j = Math.max(0, i - overlapWindow); j < i; j++) {
                        sliceContent.insert(0, chapterParagraphs.get(j).getContent() + "
");
                        offsetStart = chapterParagraphs.get(j).getOffsetStart();
                    }
                }

                // 检查是否需要二次切分（超长段落）
                if (sliceContent.length() > maxCharsPerSlice) {
                    // 对超长段落进行二次切分
                    List<String> subSlices = splitLongContent(sliceContent.toString(), maxCharsPerSlice);
                    int subOffset = offsetStart;
                    for (String subSlice : subSlices) {
                        Slice slice = createSlice(docId, chapter.getChapterId(), currentPara.getParagraphId(), sliceIndex++, subOffset, subOffset + subSlice.length(), subSlice, sliceConfig);
                        slices.add(slice);
                        subOffset += subSlice.length();
                    }
                } else {
                    // 正常切片
                    Slice slice = createSlice(docId, chapter.getChapterId(), currentPara.getParagraphId(), sliceIndex++, offsetStart, offsetEnd, sliceContent.toString(), sliceConfig);
                    slices.add(slice);
                }
            }
        }

        return slices;
    }

    /**
     * 按字数切片
     * @param docId 文档唯一标识
     * @param chapters 章节列表
     * @param paragraphs 段落列表
     * @param sliceConfig 切片配置
     * @return 切片列表
     */
    private List<Slice> sliceByCharacterCount(String docId, List<Chapter> chapters, List<Paragraph> paragraphs, SliceConfig sliceConfig) {
        List<Slice> slices = new ArrayList<>();
        int sliceIndex = 0;
        int windowSize = sliceConfig.getMaxCharsPerSlice();
        int step = windowSize - sliceConfig.getOverlapWindow();

        // 将所有段落内容合并为一个字符串，保留章节结构
        StringBuilder fullContent = new StringBuilder();
        for (Chapter chapter : chapters) {
            fullContent.append("#".repeat(chapter.getLevel())).append(" ").append(chapter.getTitle()).append("\n\n");
            List<Paragraph> chapterParagraphs = paragraphs.stream()
                    .filter(p -> p.getChapterId().equals(chapter.getChapterId()))
                    .sorted((p1, p2) -> Integer.compare(p1.getOrderNum(), p2.getOrderNum()))
                    .toList();
            for (Paragraph para : chapterParagraphs) {
                fullContent.append(para.getContent()).append("\n");
            }
        }

        String content = fullContent.toString();
        int contentLength = content.length();

        // 滑动窗口切片
        for (int i = 0; i < contentLength; i += step) {
            int end = Math.min(i + windowSize, contentLength);
            String sliceContent = content.substring(i, end);

            // 尝试对齐句子边界
            if (end < contentLength) {
                // 找到最近的句子结束符
                int lastPeriod = sliceContent.lastIndexOf('.');
                int lastExclamation = sliceContent.lastIndexOf('!');
                int lastQuestion = sliceContent.lastIndexOf('?');
                int lastNewLine = sliceContent.lastIndexOf('\n');

                int sentenceEnd = Math.max(Math.max(lastPeriod, lastExclamation), Math.max(lastQuestion, lastNewLine));
                if (sentenceEnd > sliceContent.length() * 0.5) {
                    // 如果在切片后半部分找到句子结束符，则调整切片结束位置
                    end = i + sentenceEnd + 1;
                    sliceContent = content.substring(i, end);
                }
            }

            Slice slice = createSlice(docId, null, null, sliceIndex++, i, end, sliceContent, sliceConfig);
            slices.add(slice);

            // 防止无限循环
            if (i + step >= contentLength) {
                break;
            }
        }

        return slices;
    }

    /**
     * 拆分超长内容
     * @param content 超长内容
     * @param maxCharsPerSlice 单切片最大字符数
     * @return 拆分后的内容列表
     */
    private List<String> splitLongContent(String content, int maxCharsPerSlice) {
        List<String> subSlices = new ArrayList<>();
        int contentLength = content.length();

        for (int i = 0; i < contentLength; i += maxCharsPerSlice) {
            int end = Math.min(i + maxCharsPerSlice, contentLength);
            String subSlice = content.substring(i, end);
            subSlices.add(subSlice);
        }

        return subSlices;
    }

    /**
     * 创建切片对象
     * @param docId 文档唯一标识
     * @param chapterId 章节唯一标识
     * @param paragraphId 段落唯一标识
     * @param sliceIndex 切片索引
     * @param offsetStart 起始偏移量
     * @param offsetEnd 结束偏移量
     * @param content 切片内容
     * @param sliceConfig 切片配置
     * @return 切片对象
     */
    private Slice createSlice(String docId, String chapterId, String paragraphId, int sliceIndex, int offsetStart, int offsetEnd, String content, SliceConfig sliceConfig) {
        Slice slice = new Slice();
        slice.setId(UUID.randomUUID().toString().replace("-", ""));
        slice.setDocId(docId);
        slice.setChapterId(chapterId);
        slice.setParagraphId(paragraphId);
        slice.setSliceId(docId + "_" + String.format("%05d", sliceIndex));
        slice.setSliceIndex(sliceIndex);
        slice.setOffsetStart(offsetStart);
        slice.setOffsetEnd(offsetEnd);
        slice.setOriginalContent(content);

        // 清洗内容
        String cleanedContent = cleanContent(content, sliceConfig.getCleaningLevel());
        slice.setCleanedContent(cleanedContent);

        // 设置元数据
        // TODO: 实现元数据生成逻辑
        slice.setMetadata("{}");

        return slice;
    }

    /**
     * 清洗内容
     * @param content 原始内容
     * @param cleaningLevel 清洗等级
     * @return 清洗后的内容
     */
    private String cleanContent(String content, int cleaningLevel) {
        // TODO: 实现内容清洗逻辑
        // 剥离样式与控制字符；保留有用结构标记；移除重复空白
        // cleaningLevel 1: 基本清洗（移除控制字符）
        // cleaningLevel 2: 中等清洗（移除重复空白，保留结构）
        // cleaningLevel 3: 深度清洗（移除所有样式，仅保留纯文本）

        String cleaned = content;

        // 移除控制字符
        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\\r\\n]]", "");

        if (cleaningLevel >= 2) {
            // 移除重复空白
            cleaned = cleaned.replaceAll("\\s+", " ");
        }

        if (cleaningLevel >= 3) {
            // 移除所有样式标记（如Markdown格式）
            cleaned = cleaned.replaceAll("#+", "");
            cleaned = cleaned.replaceAll("\\*+", "");
            cleaned = cleaned.replaceAll("_+", "");
        }

        return cleaned;
    }

    /**
     * 根据文档ID获取所有切片
     * @param docId 文档唯一标识
     * @return 切片列表
     */
    @Override
    public List<Slice> getSlicesByDocId(String docId) {
        return sliceMapper.selectList(Wrappers.<Slice>lambdaQuery().eq(Slice::getDocId, docId));
    }

    /**
     * 根据切片ID获取切片信息
     * @param sliceId 切片唯一标识
     * @return 切片信息
     */
    @Override
    public Slice getSliceBySliceId(String sliceId) {
        return sliceMapper.selectOne(Wrappers.<Slice>lambdaQuery().eq(Slice::getSliceId, sliceId));
    }

    /**
     * 批量获取切片信息
     * @param sliceIds 切片唯一标识列表
     * @return 切片列表
     */
    @Override
    public List<Slice> getSlicesBySliceIds(List<String> sliceIds) {
        return sliceMapper.selectList(Wrappers.<Slice>lambdaQuery().in(Slice::getSliceId, sliceIds));
    }

    /**
     * 删除文档的所有切片
     * @param docId 文档唯一标识
     * @return 删除成功的数量
     */
    @Override
    public int deleteSlicesByDocId(String docId) {
        return sliceMapper.delete(Wrappers.<Slice>lambdaQuery().eq(Slice::getDocId, docId));
    }

    /**
     * 批量删除切片
     * @param sliceIds 切片唯一标识列表
     * @return 删除成功的数量
     */
    @Override
    public int batchDeleteSlices(List<String> sliceIds) {
        return sliceMapper.delete(Wrappers.<Slice>lambdaQuery().in(Slice::getSliceId, sliceIds));
    }

    /**
     * 验证切片质量
     * @param slices 切片列表
     * @param sliceConfig 切片配置
     * @return 验证结果
     */
    @Override
    public boolean validateSlices(List<Slice> slices, SliceConfig sliceConfig) {
        // TODO: 实现切片质量验证逻辑
        // 检查切片长度、重叠率、覆盖率等
        if (slices == null || slices.isEmpty()) {
            return false;
        }

        // 检查切片长度
        for (Slice slice : slices) {
            if (slice.getCleanedContent().length() > sliceConfig.getMaxCharsPerSlice()) {
                return false;
            }
        }

        // 检查重叠率
        // TODO: 实现重叠率检查

        // 检查覆盖率
        // TODO: 实现覆盖率检查

        return true;
    }

    /**
     * 模拟从数据库获取章节列表
     * @param docId 文档唯一标识
     * @return 章节列表
     */
    private List<Chapter> getChaptersByDocId(String docId) {
        List<Chapter> chapters = new ArrayList<>();

        Chapter chapter1 = new Chapter();
        chapter1.setDocId(docId);
        chapter1.setChapterId(docId + "_chapter_001");
        chapter1.setTitle("第一章 文档介绍");
        chapter1.setLevel(1);
        chapter1.setParentChapterId(null);
        chapter1.setOrderNum(1);
        chapters.add(chapter1);

        Chapter chapter2 = new Chapter();
        chapter2.setDocId(docId);
        chapter2.setChapterId(docId + "_chapter_002");
        chapter2.setTitle("第二章 文档结构");
        chapter2.setLevel(1);
        chapter2.setParentChapterId(null);
        chapter2.setOrderNum(2);
        chapters.add(chapter2);

        return chapters;
    }

    /**
     * 模拟从数据库获取段落列表
     * @param docId 文档唯一标识
     * @return 段落列表
     */
    private List<Paragraph> getParagraphsByDocId(String docId) {
        List<Paragraph> paragraphs = new ArrayList<>();

        Paragraph para1 = new Paragraph();
        para1.setDocId(docId);
        para1.setChapterId(docId + "_chapter_001");
        para1.setParagraphId(docId + "_para_001");
        para1.setContent("这是文档的第一段内容，介绍了文档的基本情况。");
        para1.setOrderNum(1);
        para1.setOffsetStart(0);
        para1.setOffsetEnd(20);
        para1.setType("text");
        paragraphs.add(para1);

        Paragraph para2 = new Paragraph();
        para2.setDocId(docId);
        para2.setChapterId(docId + "_chapter_001");
        para2.setParagraphId(docId + "_para_002");
        para2.setContent("这是文档的第二段内容，详细说明了文档的目的和范围。");
        para2.setOrderNum(2);
        para2.setOffsetStart(21);
        para2.setOffsetEnd(50);
        para2.setType("text");
        paragraphs.add(para2);

        Paragraph para3 = new Paragraph();
        para3.setDocId(docId);
        para3.setChapterId(docId + "_chapter_002");
        para3.setParagraphId(docId + "_para_003");
        para3.setContent("这是文档的第三段内容，介绍了文档的结构和组织方式。");
        para3.setOrderNum(1);
        para3.setOffsetStart(51);
        para3.setOffsetEnd(80);
        para3.setType("text");
        paragraphs.add(para3);

        return paragraphs;
    }
}