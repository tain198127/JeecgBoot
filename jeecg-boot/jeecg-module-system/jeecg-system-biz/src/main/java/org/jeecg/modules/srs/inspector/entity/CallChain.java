package org.jeecg.modules.srs.inspector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * 调用链实体
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("srs_call_chain")
public class CallChain implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /** 接口ID */
    private String endpointId;
    /** 调用层级 */
    private Integer level;
    /** 调用类型：0-Controller，1-Service，2-Mapper，3-Other */
    private Integer callType;
    /** 类名 */
    private String className;
    /**
     * 类的复杂度
     */
    private Long classComplexScore;
    /** 方法名 */
    private String methodName;
    /**
     * 方法复杂度
     */
    private Long methodComplexScore;
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
    /**
     * 调用链上
     */
    private String parentClassName;
    /** 备注 */
    private String remark;
    /**
     * 调用链中如果是mapper的话，对应的sql内容
     */
    private String sqlContent;
    /**
     * 方法中使用的枚举值列表，格式为 "枚举类全限定名.枚举值"
     * 例如: ["cn.nc.issuance.book.dcm.lib.enums.ErrorCodeEnum.SUCCESS", "cn.nc.issuance.book.dcm.lib.enums.StatusEnum.ACTIVE"]
     */
    private Set<String> enumUsages = new HashSet<>();
    /**
     * 调用链
     */
    private List<CallChain> callChainList  = new ArrayList<>();
}