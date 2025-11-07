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
 * @Description: 文档章节实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("airag_doc_search_chapter")
public class Chapter implements Serializable {
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
     * 章节标题
     */
    private String title;

    /**
     * 章节级别（1: 章, 2: 节, 3: 小节, ...）
     */
    private Integer level;

    /**
     * 父章节ID
     */
    private String parentId;

    /**
     * 章节顺序
     */
    private Integer orderNum;

    /**
     * 章节内容（可选，用于存储章节的完整文本）
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
