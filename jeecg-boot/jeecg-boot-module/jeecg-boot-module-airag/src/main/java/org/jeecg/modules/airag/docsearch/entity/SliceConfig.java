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
 * @Description: 切片策略配置实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("airag_doc_search_slice_config")
public class SliceConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否按段落切片（true：按段落，false：按字数）
     */
    private Boolean paragraphMode;

    /**
     * 重叠窗口大小
     */
    private Integer overlapWindow;

    /**
     * 重叠窗口单位（paragraph：段落数，char：字符数）
     */
    private String overlapUnit;

    /**
     * 单切片最大字符数
     */
    private Integer maxCharsPerSlice;

    /**
     * 清洗等级（1：轻度，2：中度，3：深度）
     */
    private Integer cleaningLevel;

    /**
     * 是否保留章节结构
     */
    private Boolean preserveChapterStructure;

    /**
     * 是否保留表格结构
     */
    private Boolean preserveTableStructure;

    /**
     * 是否处理脚注尾注
     */
    private Boolean processFootnotes;

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
}
