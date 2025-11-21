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
 * @Description: PDF指标抽取任务表
 * @Author: jeecg-boot
 * @Date: 2025-06-13
 * @Version: V1.0
 */
@Data
@TableName("pdf_indicator_task")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="pdf_indicator_task对象", description="PDF指标抽取任务表")
public class PdfIndicatorTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 任务ID
     */
    @Excel(name = "任务ID", width = 50)
    @ApiModelProperty(value = "任务ID")
    private String taskId;
    /**
     * PDF文档ID
     */
    @Excel(name = "PDF文档ID", width = 50)
    @ApiModelProperty(value = "PDF文档ID")
    private String pdfId;
    /**
     * 指标类型列表
     */
    @Excel(name = "指标类型列表", width = 50)
    @ApiModelProperty(value = "指标类型列表")
    private String indicatorTypes;
    /**
     * 任务状态
     */
    @Excel(name = "任务状态", width = 15, dicCode = "pdf_task_status")
    @Dict(dicCode = "pdf_task_status")
    @ApiModelProperty(value = "任务状态")
    private String taskStatus;
    /**
     * 任务结果
     */
    @Excel(name = "任务结果", width = 100)
    @ApiModelProperty(value = "任务结果")
    private String taskResult;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束时间")
    private Date endTime;
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 租户ID
     */
    @Excel(name = "租户ID", width = 15)
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
}