package org.jeecg.modules.contract.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同主档实体类
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Data
@TableName("contract_main")
@Schema(description = "合同主档")
public class Contract {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    /** 合同编号 */
    @Excel(name = "合同编号", width = 20)
    @Schema(description = "合同编号")
    private String contractNo;

    /** 合同标题 */
    @Excel(name = "合同标题", width = 50)
    @Schema(description = "合同标题")
    private String contractTitle;

    /** 甲方ID */
    @Excel(name = "甲方ID", width = 20)
    @Schema(description = "甲方ID")
    private String partyAId;

    /** 乙方ID */
    @Excel(name = "乙方ID", width = 20)
    @Schema(description = "乙方ID")
    private String partyBId;

    /** 签署时间 */
    @Excel(name = "签署时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "签署时间")
    private Date signTime;

    /** 生效时间 */
    @Excel(name = "生效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效时间")
    private Date effectiveTime;

    /** 失效时间 */
    @Excel(name = "失效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "失效时间")
    private Date expireTime;

    /** 币种 */
    @Excel(name = "币种", width = 10)
    @Schema(description = "币种")
    private String currency;

    /** 合同金额 */
    @Excel(name = "合同金额", width = 20)
    @Schema(description = "合同金额")
    private BigDecimal amount;

    /** 敏感等级 */
    @Excel(name = "敏感等级", width = 10)
    @Schema(description = "敏感等级")
    private Integer sensitiveLevel;

    /** 当前状态 */
    @Excel(name = "当前状态", width = 20)
    @Schema(description = "当前状态")
    private String status;

    /** 归档标识 */
    @Excel(name = "归档标识", width = 10)
    @Schema(description = "归档标识")
    private Boolean archived;

    /** 版本号 */
    @Excel(name = "版本号", width = 10)
    @Schema(description = "版本号")
    private Integer version;

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