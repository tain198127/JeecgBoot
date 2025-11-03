package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.Metadata;
import org.jeecg.modules.system.mapper.MetadataMapper;
import org.jeecg.modules.system.service.MetadataService;
import org.jeecg.modules.system.util.ChineseToEnglishUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 元数据管理表Service实现类
 * @Author: jeecg-boot
 * @Date: 2025-06-26
 * @Version: V1.0
 */
@Service
public class MetadataServiceImpl extends ServiceImpl<MetadataMapper, Metadata> implements MetadataService {

    /**
     * 分页查询元数据
     * @param page 分页对象
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<Metadata> pageList(IPage<Metadata> page, QueryWrapper<Metadata> queryWrapper) {
        return baseMapper.selectPage(page, queryWrapper);
    }

    /**
     * 保存元数据（新增或修改）
     * @param metadata 元数据对象
     * @return 保存结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> saveMetadata(Metadata metadata) {
        // 检查必填字段
        if (StringUtils.isBlank(metadata.getMetadataCnName()) ||
                StringUtils.isBlank(metadata.getMetadataEnName()) ||
                StringUtils.isBlank(metadata.getMetadataCnMeaning()) ||
                StringUtils.isBlank(metadata.getMetadataEnMeaning()) ||
                StringUtils.isBlank(metadata.getJavaFieldName()) ||
                StringUtils.isBlank(metadata.getDbFieldName()) ||
                StringUtils.isBlank(metadata.getJavaDataType()) ||
                StringUtils.isBlank(metadata.getMysqlDataType()) ||
                metadata.getMaxLength() == null ||
                StringUtils.isBlank(metadata.getBusinessDesc()) ||
                StringUtils.isBlank(metadata.getBusinessDomain())) {
            return Result.error("必填字段不能为空");
        }

        // 检查元数据是否重复
        if (checkDuplicate(metadata)) {
            return Result.error("同一业务域下已存在相同的中文名称或英文名称的生效元数据");
        }

        // 设置默认值
        if (metadata.getMinLength() == null) {
            metadata.setMinLength(0);
        }
        if (StringUtils.isBlank(metadata.getStatus())) {
            metadata.setStatus("1"); // 默认生效
        }

        // 保存元数据
        boolean saveOrUpdate = this.saveOrUpdate(metadata);
        if (saveOrUpdate) {
            return Result.OK("保存成功");
        } else {
            return Result.error("保存失败");
        }
    }

    /**
     * 删除元数据
     * @param id 元数据ID
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteMetadata(String id) {
        boolean remove = this.removeById(id);
        if (remove) {
            return Result.OK("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 启用/失效元数据
     * @param id 元数据ID
     * @param status 状态（1：生效，0：失效）
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateStatus(String id, String status) {
        Metadata metadata = this.getById(id);
        if (metadata == null) {
            return Result.error("元数据不存在");
        }

        // 如果要启用，检查是否有重复的生效元数据
        if ("1".equals(status)) {
            if (checkDuplicate(metadata)) {
                return Result.error("同一业务域下已存在相同的中文名称或英文名称的生效元数据");
            }
        }

        metadata.setStatus(status);
        boolean update = this.updateById(metadata);
        if (update) {
            return Result.OK("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 导出元数据为Excel
     * @param response HttpServletResponse
     * @param queryWrapper 查询条件
     * @throws IOException IO异常
     */
    @Override
    public void exportExcel(HttpServletResponse response, QueryWrapper<Metadata> queryWrapper) throws IOException {
        List<Metadata> list = baseMapper.selectList(queryWrapper);
        ExportParams exportParams = new ExportParams("元数据管理表", "元数据管理表数据");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        JeecgEntityExcelView view = new JeecgEntityExcelView();
        view.setExportParams(exportParams);
        view.setClazz(Metadata.class);
        view.render(list, response, null);
    }

    /**
     * 导入元数据Excel
     * @param file Excel文件
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> importExcel(MultipartFile file) {
        ImportParams params = new ImportParams();
        params.setTitleRows(1);
        params.setHeadRows(1);
        params.setNeedSave(true);
        params.setSaveUrl("metadata");
        params.setImportFields(new String[]{
                "metadataCnName", "metadataEnName", "metadataCnMeaning", "metadataEnMeaning",
                "javaFieldName", "dbFieldName", "javaDataType", "mysqlDataType",
                "maxLength", "minLength", "valueRange", "businessDesc", "businessDomain", "status"
        });

        try {
            List<Metadata> list = ExcelImportUtil.importExcel(file.getInputStream(), Metadata.class, params);
            if (list != null && list.size() > 0) {
                // 检查重复
                List<String> errorMessages = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Metadata metadata = list.get(i);
                    if (checkDuplicate(metadata)) {
                        errorMessages.add("第" + (i + 2) + "行：同一业务域下已存在相同的中文名称或英文名称的生效元数据");
                    }
                }

                if (!errorMessages.isEmpty()) {
                    return Result.error("导入失败：" + StringUtils.join(errorMessages, ", "));
                }

                // 设置默认值
                for (Metadata metadata : list) {
                    if (metadata.getMinLength() == null) {
                        metadata.setMinLength(0);
                    }
                    if (StringUtils.isBlank(metadata.getStatus())) {
                        metadata.setStatus("1"); // 默认生效
                    }
                }

                boolean saveBatch = this.saveBatch(list);
                if (saveBatch) {
                    return Result.OK("导入成功，共导入" + list.size() + "条数据");
                } else {
                    return Result.error("导入失败");
                }
            } else {
                return Result.error("导入数据为空");
            }
        } catch (Exception e) {
            log.error("导入失败：", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 下载元数据导入模板
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<Metadata> list = new ArrayList<>();
        ExportParams exportParams = new ExportParams("元数据导入模板", "元数据导入模板");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + "metadata_import_template.xlsx");
        JeecgEntityExcelView view = new JeecgEntityExcelView();
        view.setExportParams(exportParams);
        view.setClazz(Metadata.class);
        view.render(list, response, null);
    }

    /**
     * AI自动生成元数据英文名称、Java字段名和数据库字段名
     * @param cnName 元数据中文名称
     * @param businessDomain 业务域
     * @return 生成结果
     */
    @Override
    public Result<Map<String, String>> generateMetadataFields(String cnName, String businessDomain) {
        if (StringUtils.isBlank(cnName) || StringUtils.isBlank(businessDomain)) {
            return Result.error("中文名称和业务域不能为空");
        }

        try {
            // 使用中文转英文工具类生成字段名
            String enName = ChineseToEnglishUtil.chineseToUnderlineCase(cnName);
            String javaFieldName = ChineseToEnglishUtil.chineseToJavaFieldName(cnName);
            String dbFieldName = ChineseToEnglishUtil.chineseToDbColumnName(cnName);

            // 如果业务域不为空，添加业务域前缀
            if (StringUtils.isNotBlank(businessDomain)) {
                String domainPrefix = ChineseToEnglishUtil.chineseToUnderlineCase(businessDomain);
                enName = domainPrefix + "_" + enName;
                javaFieldName = StringUtils.lowerFirst(domainPrefix) + StringUtils.upperFirst(javaFieldName);
                dbFieldName = domainPrefix + "_" + dbFieldName;
            }

            Map<String, String> result = new java.util.HashMap<>();
            result.put("metadataEnName", enName);
            result.put("javaFieldName", javaFieldName);
            result.put("dbFieldName", dbFieldName);

            return Result.OK("生成成功", result);
        } catch (Exception e) {
            log.error("生成元数据字段失败：", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 检查元数据是否重复（同一业务域下生效的中文名称或英文名称重复）
     * @param metadata 元数据对象
     * @return 检查结果
     */
    @Override
    public boolean checkDuplicate(Metadata metadata) {
        QueryWrapper<Metadata> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("business_domain", metadata.getBusinessDomain());
        queryWrapper.eq("status", "1"); // 只检查生效状态的元数据
        queryWrapper.and(wrapper -> wrapper.eq("metadata_cn_name", metadata.getMetadataCnName())
                .or().eq("metadata_en_name", metadata.getMetadataEnName()));

        // 如果是修改，排除自身
        if (StringUtils.isNotBlank(metadata.getId())) {
            queryWrapper.ne("id", metadata.getId());
        }

        return this.count(queryWrapper) > 0;
    }
}