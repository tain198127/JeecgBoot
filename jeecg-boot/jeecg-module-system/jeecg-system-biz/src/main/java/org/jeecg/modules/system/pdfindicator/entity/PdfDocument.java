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
 * @Description: PDF文档表
 * @Author: jeecg-boot
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@TableName("pdf_document")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="pdf_document对象", description="PDF文档表")
public class PdfDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 文件名
     */
    @Excel(name = "文件名", width = 15)
    @ApiModelProperty(value = "文件名")
    private String fileName;
    /**
     * 存储路径
     */
    @Excel(name = "存储路径", width = 30)
    @ApiModelProperty(value = "存储路径")
    private String filePath;
    /**
     * 文件大小（字节）
     */
    @Excel(name = "文件大小", width = 15)
    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;
    /**
     * 页数
     */
    @Excel(name = "页数", width = 10)
    @ApiModelProperty(value = "页数")
    private Integer pageCount;
    /**
     * 上传人
     */
    @Excel(name = "上传人", width = 15)
    @ApiModelProperty(value = "上传人")
    private String uploadBy;
    /**
     * 上传时间
     */
    @Excel(name = "上传时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "上传时间")
    private Date uploadTime;
    /**
     * 解析状态（0:未解析,1:解析中,2:解析成功,3:解析失败）
     */
    @Excel(name = "解析状态", width = 15, dicCode = "pdf_parse_status")
    @Dict(dicCode = "pdf_parse_status")
    @ApiModelProperty(value = "解析状态（0:未解析,1:解析中,2:解析成功,3:解析失败）")
    private Integer parseStatus;
    /**
     * 错误信息
     */
    @Excel(name = "错误信息", width = 30)
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;
    /**
     * 租户ID
     */
    @Excel(name = "租户ID", width = 15)
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}