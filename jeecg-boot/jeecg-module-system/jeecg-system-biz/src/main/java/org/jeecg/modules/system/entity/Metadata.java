package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 元数据管理表
 * @Author: jeecg-boot
 * @Date: 2025-06-26
 * @Version: V1.0
 */
@Data
@TableName("metadata")
@Schema(name="Metadata对象", description="元数据管理表")
public class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @Excel(name = "主键", width = 15)
    @Schema(description = "主键")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**元数据中文名称*/
    @Excel(name = "元数据中文名称", width = 20)
    @Schema(description = "元数据中文名称")
    private String metadataCnName;
    /**元数据英文名称*/
    @Excel(name = "元数据英文名称", width = 20)
    @Schema(description = "元数据英文名称")
    private String metadataEnName;
    /**元数据中文含义*/
    @Excel(name = "元数据中文含义", width = 30)
    @Schema(description = "元数据中文含义")
    private String metadataCnMeaning;
    /**元数据英文含义*/
    @Excel(name = "元数据英文含义", width = 30)
    @Schema(description = "元数据英文含义")
    private String metadataEnMeaning;
    /**Java代码中的英文名称*/
    @Excel(name = "Java代码中的英文名称", width = 25)
    @Schema(description = "Java代码中的英文名称")
    private String javaFieldName;
    /**数据库表中的字段名称*/
    @Excel(name = "数据库表中的字段名称", width = 25)
    @Schema(description = "数据库表中的字段名称")
    private String dbFieldName;
    /**Java数据类型*/
    @Excel(name = "Java数据类型", width = 20)
    @Schema(description = "Java数据类型")
    private String javaDataType;
    /**MySQL数据类型*/
    @Excel(name = "MySQL数据类型", width = 20)
    @Schema(description = "MySQL数据类型")
    private String mysqlDataType;
    /**最大长度*/
    @Excel(name = "最大长度", width = 10)
    @Schema(description = "最大长度")
    private Integer maxLength;
    /**最小长度*/
    @Excel(name = "最小长度", width = 10)
    @Schema(description = "最小长度")
    private Integer minLength;
    /**取值范围（正则表达式）*/
    @Excel(name = "取值范围（正则表达式）", width = 30)
    @Schema(description = "取值范围（正则表达式）")
    private String valueRange;
    /**业务描述*/
    @Excel(name = "业务描述", width = 50)
    @Schema(description = "业务描述")
    private String businessDesc;
    /**所属业务域*/
    @Excel(name = "所属业务域", width = 20)
    @Schema(description = "所属业务域")
    private String businessDomain;
    /**录入时间*/
    @Excel(name = "录入时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "录入时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**最后修改时间*/
    @Excel(name = "最后修改时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**生效状态（1：生效，0：失效）*/
    @Excel(name = "生效状态", width = 10, dicCode = "sys_status")
    @Dict(dicCode = "sys_status")
    @Schema(description = "生效状态（1：生效，0：失效）")
    private String status;
}