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
 * 合同文件实体类
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Data
@TableName("contract_file")
@Schema(description = "合同文件")
public class ContractFile {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    /** 合同ID */
    @Excel(name = "合同ID", width = 20)
    @Schema(description = "合同ID")
    private String contractId;

    /** 文件类型（原件/附件） */
    @Excel(name = "文件类型", width = 10)
    @Schema(description = "文件类型（原件/附件）")
    private String fileType;

    /** 对象存储Key */
    @Excel(name = "对象存储Key", width = 50)
    @Schema(description = "对象存储Key")
    private String objectKey;

    /** 文件哈希 */
    @Excel(name = "文件哈希", width = 50)
    @Schema(description = "文件哈希")
    private String hash;

    /** 页数 */
    @Excel(name = "页数", width = 10)
    @Schema(description = "页数")
    private Integer pageCount;

    /** MIME类型 */
    @Excel(name = "MIME类型", width = 20)
    @Schema(description = "MIME类型")
    private String mimeType;

    /** 转换状态 */
    @Excel(name = "转换状态", width = 20)
    @Schema(description = "转换状态")
    private String convertStatus;

    /** 预览地址 */
    @Excel(name = "预览地址", width = 50)
    @Schema(description = "预览地址")
    private String previewUrl;

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