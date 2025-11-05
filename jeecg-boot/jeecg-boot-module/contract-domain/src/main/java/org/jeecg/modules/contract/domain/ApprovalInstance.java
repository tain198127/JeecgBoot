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
 * 审批实例实体类
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Data
@TableName("contract_approval_instance")
@Schema(description = "审批实例")
public class ApprovalInstance {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    /** 合同ID */
    @Excel(name = "合同ID", width = 20)
    @Schema(description = "合同ID")
    private String contractId;

    /** 流程定义ID */
    @Excel(name = "流程定义ID", width = 20)
    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    /** 节点栈 */
    @Excel(name = "节点栈", width = 50)
    @Schema(description = "节点栈")
    private String nodeStack;

    /** 当前处理人 */
    @Excel(name = "当前处理人", width = 20)
    @Schema(description = "当前处理人")
    private String currentAssignee;

    /** 抄送列表 */
    @Excel(name = "抄送列表", width = 50)
    @Schema(description = "抄送列表")
    private String ccList;

    /** 状态 */
    @Excel(name = "状态", width = 20)
    @Schema(description = "状态")
    private String status;

    /** 审批意见 */
    @Excel(name = "审批意见", width = 50)
    @Schema(description = "审批意见")
    private String comments;

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