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
 * @Description: 文档切片实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("airag_doc_search_slice")
public class Slice implements Serializable {
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
     * 切片唯一标识
     */
    private String sliceId;

    /**
     * 切片索引
     */
    private Integer sliceIndex;

    /**
     * 切片起始偏移量
     */
    private Integer offsetStart;

    /**
     * 切片结束偏移量
     */
    private Integer offsetEnd;

    /**
     * 原始内容
     */
    private String originalContent;

    /**
     * 清洗后的内容
     */
    private String cleanedContent;

    /**
     * 切片上下文元数据（JSON格式）
     */
    private String metadata;

    /**
     * 向量ID（Milvus中存储的向量标识）
     */
    private String vectorId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否为脏切片
     */
    private Boolean isDirty;

    /**
     * 脏切片原因
     */
    private String dirtyReason;
}
