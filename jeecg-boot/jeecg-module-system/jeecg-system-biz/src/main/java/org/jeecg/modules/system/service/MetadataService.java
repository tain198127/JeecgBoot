package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.Metadata;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 元数据管理表Service接口
 * @Author: jeecg-boot
 * @Date: 2025-06-26
 * @Version: V1.0
 */
public interface MetadataService extends IService<Metadata> {

    /**
     * 分页查询元数据
     * @param page 分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    IPage<Metadata> pageList(IPage<Metadata> page, QueryWrapper<Metadata> queryWrapper);

    /**
     * 保存元数据（新增或修改）
     * @param metadata 元数据对象
     * @return 保存结果
     */
    Result<?> saveMetadata(Metadata metadata);

    /**
     * 删除元数据
     * @param id 元数据ID
     * @return 删除结果
     */
    Result<?> deleteMetadata(String id);

    /**
     * 启用/失效元数据
     * @param id 元数据ID
     * @param status 状态（1：生效，0：失效）
     * @return 操作结果
     */
    Result<?> updateStatus(String id, String status);

    /**
     * 导出元数据为Excel
     * @param response HttpServletResponse
     * @param queryWrapper 查询条件
     * @throws IOException IO异常
     */
    void exportExcel(HttpServletResponse response, QueryWrapper<Metadata> queryWrapper) throws IOException;

    /**
     * 导入元数据Excel
     * @param file Excel文件
     * @return 导入结果
     */
    Result<?> importExcel(MultipartFile file);

    /**
     * 下载元数据导入模板
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * AI自动生成元数据英文名称、Java字段名和数据库字段名
     * @param cnName 元数据中文名称
     * @param businessDomain 业务域
     * @return 生成结果
     */
    Result<Map<String, String>> generateMetadataFields(String cnName, String businessDomain);

    /**
     * 检查元数据是否重复（同一业务域下生效的中文名称或英文名称重复）
     * @param metadata 元数据对象
     * @return 检查结果
     */
    boolean checkDuplicate(Metadata metadata);
}