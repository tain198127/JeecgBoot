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
 * 合同相对方实体类
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Data
@TableName("contract_party")
@Schema(description = "合同相对方")
public class Party {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    /** 类型（企业/个人） */
    @Excel(name = "类型", width = 10)
    @Schema(description = "类型（企业/个人）")
    private String type;

    /** 名称 */
    @Excel(name = "名称", width = 30)
    @Schema(description = "名称")
    private String name;

    /** 统一社会信用代码/证件号 */
    @Excel(name = "统一社会信用代码/证件号", width = 30)
    @Schema(description = "统一社会信用代码/证件号")
    private String identifier;

    /** 联系人 */
    @Excel(name = "联系人", width = 20)
    @Schema(description = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话", width = 20)
    @Schema(description = "联系电话")
    private String contactPhone;

    /** 风控标签 */
    @Excel(name = "风控标签", width = 50)
    @Schema(description = "风控标签")
    private String riskTags;

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