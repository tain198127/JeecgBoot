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
 * @Description: 文档实体
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("airag_doc_search_document")
public class Document implements Serializable {
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
     * 文档标题
     */
    private String title;

    /**
     * 文档原始名称
     */
    private String originalName;

    /**
     * 文档存储路径
     */
    private String filePath;

    /**
     * 文档大小（字节）
     */
    private Long fileSize;

    /**
     * 文档类型（docx）
     */
    private String fileType;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 解析状态（0：未解析，1：解析中，2：解析成功，3：解析失败）
     */
    private Integer parseStatus;

    /**
     * 切片状态（0：未切片，1：切片中，2：切片成功，3：切片失败）
     */
    private Integer sliceStatus;

    /**
     * 索引状态（0：未索引，1：索引中，2：索引成功，3：索引失败）
     */
    private Integer indexStatus;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 解析耗时（毫秒）
     */
    private Long parseDuration;

    /**
     * 切片耗时（毫秒）
     */
    private Long sliceDuration;

    /**
     * 索引耗时（毫秒）
     */
    private Long indexDuration;

    /**
     * 总切片数
     */
    private Integer totalSlices;
}
