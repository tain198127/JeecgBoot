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
import org.jeecg.modules.system.pdfindicator.entity.PdfDocumentSlice;
import org.jeecg.modules.system.pdfindicator.service.IPdfDocumentSliceService;

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
 * @Description: PDF文档切片表
 * @Author: jeecg-boot
 * @Date:   2025-05-20
 * @Version: V1.0
 */
@RestController
@RequestMapping("/pdfindicator/pdfDocumentSlice")
@Slf4j
public class PdfDocumentSliceController extends JeecgController<PdfDocumentSlice, IPdfDocumentSliceService> {
	@Autowired
	private IPdfDocumentSliceService pdfDocumentSliceService;

	/**
	 * 分页列表查询
	 *
	 * @param pdfDocumentSlice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(PdfDocumentSlice pdfDocumentSlice,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		QueryWrapper<PdfDocumentSlice> queryWrapper = QueryGenerator.initQueryWrapper(pdfDocumentSlice, req.getParameterMap());
		Page<PdfDocumentSlice> page = new Page<PdfDocumentSlice>(pageNo, pageSize);
		IPage<PdfDocumentSlice> pageList = pdfDocumentSliceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param pdfDocumentSlice
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody PdfDocumentSlice pdfDocumentSlice) {
		pdfDocumentSliceService.save(pdfDocumentSlice);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param pdfDocumentSlice
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody PdfDocumentSlice pdfDocumentSlice) {
		pdfDocumentSliceService.updateById(pdfDocumentSlice);
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
		pdfDocumentSliceService.removeById(id);
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
		this.pdfDocumentSliceService.removeByIds(Arrays.asList(ids.split(",")));
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
		PdfDocumentSlice pdfDocumentSlice = pdfDocumentSliceService.getById(id);
		if(pdfDocumentSlice==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(pdfDocumentSlice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param pdfDocumentSlice
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdfDocumentSlice pdfDocumentSlice) {
        return super.exportXls(request, pdfDocumentSlice, PdfDocumentSlice.class, "PDF文档切片表");
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
        return super.importExcel(request, response, PdfDocumentSlice.class);
    }

}
