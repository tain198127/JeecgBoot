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
 * 问题实体
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("srs_issue")
public class Issue implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 接口ID */
    private String endpointId;
    /** 问题类型：0-缺少@Operation注解，1-模型生成失败，2-其他 */
    private Integer issueType;
    /** 问题描述 */
    private String description;
    /** 所在类 */
    private String className;
    /** 所在行 */
    private Integer lineNumber;
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