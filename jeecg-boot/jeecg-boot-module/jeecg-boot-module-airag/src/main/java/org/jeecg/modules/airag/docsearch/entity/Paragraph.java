package org.jeecg.modules.airag.docsearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 文档段落实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("airag_doc_search_paragraph")
public class Paragraph implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 文档唯一标识
     */
    private String docId;

    /**
     * 章节唯一标识
     */
    private String chapterId;

    /**
     * 段落唯一标识
     */
    private String paragraphId;

    /**
     * 段落内容
     */
    private String content;

    /**
     * 段落顺序
     */
    private Integer orderNum;

    /**
     * 段落起始偏移量
     */
    private Integer offsetStart;

    /**
     * 段落结束偏移量
     */
    private Integer offsetEnd;

    /**
     * 段落类型（正文、表格、页眉、页脚、批注等）
     */
    private String type;

    /**
     * 表格信息（JSON格式，当type为表格时存储表格结构）
     */
    private String tableInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
