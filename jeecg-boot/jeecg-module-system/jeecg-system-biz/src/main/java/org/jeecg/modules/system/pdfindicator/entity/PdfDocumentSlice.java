package org.jeecg.modules.system.pdfindicator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: PDF文档切片表
 * @Author: jeecg-boot
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@TableName("pdf_document_slice")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="pdf_document_slice对象", description="PDF文档切片表")
public class PdfDocumentSlice implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 文档ID
     */
    @Excel(name = "文档ID", width = 30)
    @ApiModelProperty(value = "文档ID")
    private String documentId;
    /**
     * 切片ID
     */
    @Excel(name = "切片ID", width = 30)
    @ApiModelProperty(value = "切片ID")
    private String sliceId;
    /**
     * 页码范围（如：1-5）
     */
    @Excel(name = "页码范围", width = 15)
    @ApiModelProperty(value = "页码范围（如：1-5）")
    private String pageRange;
    /**
     * 偏移量
     */
    @Excel(name = "偏移量", width = 15)
    @ApiModelProperty(value = "偏移量")
    private Integer offset;
    /**
     * 文本摘要
     */
    @Excel(name = "文本摘要", width = 50)
    @ApiModelProperty(value = "文本摘要")
    private String textSummary;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 租户ID
     */
    @Excel(name = "租户ID", width = 15)
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
}