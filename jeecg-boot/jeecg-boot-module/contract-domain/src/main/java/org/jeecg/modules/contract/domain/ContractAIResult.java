package org.jeecg.modules.contract.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

/**
 * AI解析结果实体类
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Data
@TableName("contract_ai_result")
@Schema(description = "AI解析结果")
public class ContractAIResult {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    /** 合同ID */
    @Excel(name = "合同ID", width = 20)
    @Schema(description = "合同ID")
    private String contractId;

    /** 文件ID */
    @Excel(name = "文件ID", width = 20)
    @Schema(description = "文件ID")
    private String fileId;

    /** 版式类型 */
    @Excel(name = "版式类型", width = 20)
    @Schema(description = "版式类型")
    private String layoutType;

    /** 页块坐标 */
    @Excel(name = "页块坐标", width = 50)
    @Schema(description = "页块坐标")
    private String pageBlockCoordinates;

    /** 字段字典 */
    @Excel(name = "字段字典", width = 50)
    @Schema(description = "字段字典")
    private String fieldDictionary;

    /** 置信度 */
    @Excel(name = "置信度", width = 10)
    @Schema(description = "置信度")
    private Double confidence;

    /** 纠错轨迹 */
    @Excel(name = "纠错轨迹", width = 50)
    @Schema(description = "纠错轨迹")
    private String correctionTrace;

    /** 解析版本 */
    @Excel(name = "解析版本", width = 10)
    @Schema(description = "解析版本")
    private String parseVersion;

    /** 租户ID */
    @Schema(description = "租户ID")
    private String tenantId;

    /** 创建人 */
    @Schema(description = "创建人")
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createdTime;

    /** 更新人 */
    @Schema(description = "更新人")
    private String updatedBy;

    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updatedTime;

    /** 操作轨迹ID */
    @Schema(description = "操作轨迹ID")
    private String opTraceId;

    /** 乐观锁 */
    @Schema(description = "乐观锁")
    private Integer revision;
}