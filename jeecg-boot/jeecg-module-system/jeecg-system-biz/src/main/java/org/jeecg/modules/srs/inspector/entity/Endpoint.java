package org.jeecg.modules.srs.inspector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口实体
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("srs_endpoint")
public class Endpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 扫描任务ID */
    private String taskId;
    /** 控制器类名 */
    private String controllerName;
    /** 控制器路径 */
    private String controllerPath;
    /** 方法名 */
    private String methodName;
    /** HTTP方法 */
    private String httpMethod;
    /** 接口路径 */
    private String path;
    /** 逻辑说明 */
    private String logicDescription;
    /** 代码片段 */
    private String codeSnippet;
    /** Javadoc注释 */
    private String docComment;
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
    /**
     * 该接口下所有复杂度综合
     */
    private Long sumAllComplexScore;

    private List<CallChain> callChainList;
}