package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.Metadata;
import org.jeecg.modules.system.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @Description: 元数据管理表
 * @Author: jeecg-boot
 * @Date: 2025-06-26
 * @Version: V1.0
 */
@RestController
@RequestMapping("/system/metadata")
@Tag(name = "元数据管理表", description = "元数据管理表接口")
public class MetadataController {
    @Autowired
    private MetadataService metadataService;

    /**
     * 分页列表查询
     *
     * @param metadata 元数据对象
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @param req      HttpServletRequest
     * @return Result<IPage<Metadata>>
     */
    @AutoLog(value = "元数据管理表-分页列表查询")
    @Operation(summary = "元数据管理表-分页列表查询", description = "元数据管理表-分页列表查询")
    @GetMapping(value = "/page")
    public Result<IPage<Metadata>> queryPageList(
            Metadata metadata,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        Result<IPage<Metadata>> result = new Result<>();
        QueryWrapper<Metadata> queryWrapper = new QueryWrapper<>();

        // 中文名称模糊查询
        if (StringUtils.isNotBlank(metadata.getMetadataCnName())) {
            queryWrapper.like("metadata_cn_name", metadata.getMetadataCnName());
        }
        // 英文名称模糊查询
        if (StringUtils.isNotBlank(metadata.getMetadataEnName())) {
            queryWrapper.like("metadata_en_name", metadata.getMetadataEnName());
        }
        // 业务域精确查询
        if (StringUtils.isNotBlank(metadata.getBusinessDomain())) {
            queryWrapper.eq("business_domain", metadata.getBusinessDomain());
        }

        Page<Metadata> page = new Page<>(pageNo, pageSize);
        IPage<Metadata> pageList = metadataService.pageList(page, queryWrapper);
        result.setSuccess(true);
        result.setData(pageList);
        return result;
    }

    /**
     * 保存元数据（新增或修改）
     *
     * @param metadata 元数据对象
     * @return Result<String>
     */
    @AutoLog(value = "元数据管理表-保存")
    @Operation(summary = "元数据管理表-保存", description = "元数据管理表-保存")
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody Metadata metadata) {
        return metadataService.saveMetadata(metadata);
    }

    /**
     * 通过id删除元数据
     *
     * @param id 元数据ID
     * @return Result<String>
     */
    @AutoLog(value = "元数据管理表-通过id删除")
    @Operation(summary = "元数据管理表-通过id删除", description = "元数据管理表-通过id删除")
    @DeleteMapping(value = "/delete/{id}")
    public Result<String> delete(@PathVariable(name = "id") String id) {
        return metadataService.deleteMetadata(id);
    }

    /**
     * 更新元数据状态
     *
     * @param id     元数据ID
     * @param status 状态（1：生效，0：失效）
     * @return Result<String>
     */
    @AutoLog(value = "元数据管理表-更新状态")
    @Operation(summary = "元数据管理表-更新状态", description = "元数据管理表-更新状态")
    @PutMapping(value = "/updateStatus/{id}/{status}")
    public Result<String> updateStatus(
            @PathVariable(name = "id") String id,
            @PathVariable(name = "status") String status) {
        return metadataService.updateStatus(id, status);
    }

    /**
     * 导出元数据Excel
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @AutoLog(value = "元数据管理表-导出Excel")
    @Operation(summary = "元数据管理表-导出Excel", description = "元数据管理表-导出Excel")
    @GetMapping(value = "/export")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String metadataCnName = request.getParameter("metadataCnName");
        String metadataEnName = request.getParameter("metadataEnName");
        String businessDomain = request.getParameter("businessDomain");

        QueryWrapper<Metadata> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(metadataCnName)) {
            queryWrapper.like("metadata_cn_name", URLDecoder.decode(metadataCnName, "UTF-8"));
        }
        if (StringUtils.isNotBlank(metadataEnName)) {
            queryWrapper.like("metadata_en_name", URLDecoder.decode(metadataEnName, "UTF-8"));
        }
        if (StringUtils.isNotBlank(businessDomain)) {
            queryWrapper.eq("business_domain", URLDecoder.decode(businessDomain, "UTF-8"));
        }

        metadataService.exportExcel(response, queryWrapper);
    }

    /**
     * 导入元数据Excel
     *
     * @param file Excel文件
     * @return Result<?> 导入结果
     */
    @AutoLog(value = "元数据管理表-导入Excel")
    @Operation(summary = "元数据管理表-导入Excel", description = "元数据管理表-导入Excel")
    @PostMapping(value = "/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file) {
        return metadataService.importExcel(file);
    }

    /**
     * 下载元数据导入模板
     *
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @AutoLog(value = "元数据管理表-下载导入模板")
    @Operation(summary = "元数据管理表-下载导入模板", description = "元数据管理表-下载导入模板")
    @GetMapping(value = "/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        metadataService.downloadTemplate(response);
    }

    /**
     * AI自动生成元数据字段
     *
     * @param cnName         元数据中文名称
     * @param businessDomain 业务域
     * @return Result<Map<String, String>> 生成结果
     */
    @AutoLog(value = "元数据管理表-AI生成字段")
    @Operation(summary = "元数据管理表-AI生成字段", description = "元数据管理表-AI生成字段")
    @GetMapping(value = "/generateFields")
    public Result<Map<String, String>> generateFields(
            @Parameter(name = "cnName", description = "元数据中文名称", required = true) @RequestParam String cnName,
            @Parameter(name = "businessDomain", description = "业务域", required = true) @RequestParam String businessDomain) {
        return metadataService.generateMetadataFields(cnName, businessDomain);
    }
}