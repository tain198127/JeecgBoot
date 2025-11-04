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
 * 扫描任务实体
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("srs_scan_task")
public class ScanTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 任务名称 */
    private String taskName;
    /** 扫描路径 */
    private String scanPath;
    /** 扫描状态：0-待执行，1-执行中，2-完成，3-失败 */
    private Integer status;
    /** 接口数量 */
    private Integer endpointCount;
    /** 问题数量 */
    private Integer issueCount;
    /** 开始时间 */
    private Date startTime;
    /** 结束时间 */
    private Date endTime;
    /** 模型名称 */
    private String modelName;
    /** 模型参数 */
    private String modelParams;
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