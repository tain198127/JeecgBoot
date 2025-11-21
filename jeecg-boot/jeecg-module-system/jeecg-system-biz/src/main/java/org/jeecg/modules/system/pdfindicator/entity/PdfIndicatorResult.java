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
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: PDF指标抽取结果表
 * @Author: jeecg-boot
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Data
@TableName("pdf_indicator_result")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="pdf_indicator_result对象", description="PDF指标抽取结果表")
public class PdfIndicatorResult implements Serializable {
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
    @Excel(name = "任务ID", width = 30)
    @ApiModelProperty(value = "任务ID")
    private String taskId;
    /**
     * 指标编码
     */
    @Excel(name = "指标编码", width = 20)
    @ApiModelProperty(value = "指标编码")
    private String indicatorCode;
    /**
     * 指标名称
     */
    @Excel(name = "指标名称", width = 20)
    @ApiModelProperty(value = "指标名称")
    private String indicatorName;
    /**
     * 最终金额（纯数字，单位：元）
     */
    @Excel(name = "最终金额（元）", width = 20, numFormat = "#,##0.00")
    @ApiModelProperty(value = "最终金额（纯数字，单位：元）")
    private BigDecimal finalAmount;
    /**
     * 原始金额字符串
     */
    @Excel(name = "原始金额字符串", width = 20)
    @ApiModelProperty(value = "原始金额字符串")
    private String originalAmountStr;
    /**
     * 原始单位
     */
    @Excel(name = "原始单位", width = 10)
    @ApiModelProperty(value = "原始单位")
    private String originalUnit;
    /**
     * 合并状态（0:稳定,1:冲突,2:缺失）
     */
    @Excel(name = "合并状态", width = 15, dicCode = "pdf_merge_status")
    @Dict(dicCode = "pdf_merge_status")
    @ApiModelProperty(value = "合并状态（0:稳定,1:冲突,2:缺失）")
    private Integer mergeStatus;
    /**
     * 校验状态（0:待校验,1:通过,2:可疑,3:需要人工）
     */
    @Excel(name = "校验状态", width = 15, dicCode = "pdf_validation_status")
    @Dict(dicCode = "pdf_validation_status")
    @ApiModelProperty(value = "校验状态（0:待校验,1:通过,2:可疑,3:需要人工）")
    private Integer validationStatus;
    /**
     * 三轮结果（JSON格式）
     */
    @Excel(name = "三轮结果", width = 50)
    @ApiModelProperty(value = "三轮结果（JSON格式）")
    private String roundResults;
    /**
     * 切片列表（JSON格式）
     */
    @Excel(name = "切片列表", width = 50)
    @ApiModelProperty(value = "切片列表（JSON格式）")
    private String sliceIds;
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
    /**
     * 租户ID
     */
    @Excel(name = "租户ID", width = 15)
    @ApiModelProperty(value = "租户ID")
    private String tenantId;
}