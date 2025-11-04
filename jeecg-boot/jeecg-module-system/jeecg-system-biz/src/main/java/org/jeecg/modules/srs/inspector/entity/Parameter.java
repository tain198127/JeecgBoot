package org.jeecg.modules.srs.inspector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 参数实体
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("srs_parameter")
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 接口ID */
    private String endpointId;
    /** 参数类型：0-入参，1-出参 */
    private Integer paramType;
    /** 字段名 */
    private String fieldName;
    /** 字段类型 */
    private String fieldType;
    /** 是否必填 */
    private Boolean required;
    /** 默认值 */
    private String defaultValue;
    /** 最小长度 */
    private Integer minLength;
    /** 最大长度 */
    private Integer maxLength;
    /** 正则表达式 */
    private String pattern;
    /** 描述 */
    private String description;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private Date createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    private Date updateTime;
    /** 备注 */
    private String remark;
}