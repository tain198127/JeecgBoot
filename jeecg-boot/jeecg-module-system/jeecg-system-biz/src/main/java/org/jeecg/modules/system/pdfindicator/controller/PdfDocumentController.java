package org.jeecg.modules.system.pdfindicator.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.pdfindicator.entity.PdfDocument;
import org.jeecg.modules.system.pdfindicator.service.IPdfDocumentService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;

 /**
 * @Description: PDF文档表
 * @Author: jeecg-boot
 * @Date:   2025-05-20
 * @Version: V1.0
 */
@RestController
@RequestMapping("/pdfindicator/pdfDocument")
@Slf4j
public class PdfDocumentController extends JeecgController<PdfDocument, IPdfDocumentService> {
	@Autowired
	private IPdfDocumentService pdfDocumentService;

	/**
	 * 分页列表查询
	 *
	 * @param pdfDocument
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(PdfDocument pdfDocument,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		QueryWrapper<PdfDocument> queryWrapper = QueryGenerator.initQueryWrapper(pdfDocument, req.getParameterMap());
		Page<PdfDocument> page = new Page<PdfDocument>(pageNo, pageSize);
		IPage<PdfDocument> pageList = pdfDocumentService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param pdfDocument
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody PdfDocument pdfDocument) {
		pdfDocumentService.save(pdfDocument);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param pdfDocument
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody PdfDocument pdfDocument) {
		pdfDocumentService.updateById(pdfDocument);
		return Result.OK("编辑成功！");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		pdfDocumentService.removeById(id);
		return Result.OK("删除成功！");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.pdfDocumentService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		PdfDocument pdfDocument = pdfDocumentService.getById(id);
		if(pdfDocument==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(pdfDocument);
	}

	/**
	 * 上传PDF文件
	 *
	 * @param file
	 * @return
	 */
	@PostMapping(value = "/upload")
	public Result<?> upload(@RequestParam("file") MultipartFile file) {
		try {
			PdfDocument pdfDocument = pdfDocumentService.uploadPdf(file);
			return Result.OK("PDF文件上传成功！", pdfDocument);
		} catch (IOException e) {
			log.error("PDF文件上传失败：", e);
			return Result.error("PDF文件上传失败：" + e.getMessage());
		}
	}

	/**
	 * 解析PDF内容
	 *
	 * @param pdfId
	 * @return
	 */
	@PostMapping(value = "/parse")
	public Result<?> parse(@RequestParam(name="pdfId",required=true) String pdfId) {
		try {
			Map<String, Object> result = pdfDocumentService.parsePdfContent(pdfId);
			return Result.OK("PDF内容解析成功！", result);
		} catch (Exception e) {
			log.error("PDF内容解析失败：", e);
			return Result.error("PDF内容解析失败：" + e.getMessage());
		}
	}

	/**
	 * 抽取PDF指标
	 *
	 * @param pdfId
	 * @return
	 */
	@PostMapping(value = "/extract")
	public Result<?> extract(@RequestParam(name="pdfId",required=true) String pdfId) {
		try {
			Map<String, Object> result = pdfDocumentService.extractIndicators(pdfId);
			return Result.OK("PDF指标抽取成功！", result);
		} catch (Exception e) {
			log.error("PDF指标抽取失败：", e);
			return Result.error("PDF指标抽取失败：" + e.getMessage());
		}
	}

	/**
	 * 智能校验PDF指标
	 *
	 * @param pdfId
	 * @return
	 */
	@PostMapping(value = "/validate")
	public Result<?> validate(@RequestParam(name="pdfId",required=true) String pdfId) {
		try {
			Map<String, Object> result = pdfDocumentService.validateIndicators(pdfId);
			return Result.OK("PDF指标智能校验成功！", result);
		} catch (Exception e) {
			log.error("PDF指标智能校验失败：", e);
			return Result.error("PDF指标智能校验失败：" + e.getMessage());
		}
	}

	/**
     * 导出excel
     *
     * @param request
     * @param pdfDocument
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdfDocument pdfDocument) {
        return super.exportXls(request, pdfDocument, PdfDocument.class, "PDF文档表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, PdfDocument.class);
    }

}
