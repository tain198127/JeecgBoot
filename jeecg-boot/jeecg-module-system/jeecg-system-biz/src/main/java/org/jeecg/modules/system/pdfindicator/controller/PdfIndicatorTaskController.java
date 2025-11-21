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
import org.jeecg.modules.system.pdfindicator.entity.PdfIndicatorTask;
import org.jeecg.modules.system.pdfindicator.service.IPdfIndicatorTaskService;
import org.springframework.web.bind.annotation.RequestBody;

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
 * @Description: PDF指标抽取任务表
 * @Author: jeecg-boot
 * @Date:   2025-05-20
 * @Version: V1.0
 */
@RestController
@RequestMapping("/pdfindicator/pdfIndicatorTask")
@Slf4j
public class PdfIndicatorTaskController extends JeecgController<PdfIndicatorTask, IPdfIndicatorTaskService> {
	@Autowired
	private IPdfIndicatorTaskService pdfIndicatorTaskService;

	/**
	 * 分页列表查询
	 *
	 * @param pdfIndicatorTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(PdfIndicatorTask pdfIndicatorTask,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		QueryWrapper<PdfIndicatorTask> queryWrapper = QueryGenerator.initQueryWrapper(pdfIndicatorTask, req.getParameterMap());
		Page<PdfIndicatorTask> page = new Page<PdfIndicatorTask>(pageNo, pageSize);
		IPage<PdfIndicatorTask> pageList = pdfIndicatorTaskService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param pdfIndicatorTask
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody PdfIndicatorTask pdfIndicatorTask) {
		pdfIndicatorTaskService.save(pdfIndicatorTask);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param pdfIndicatorTask
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody PdfIndicatorTask pdfIndicatorTask) {
		pdfIndicatorTaskService.updateById(pdfIndicatorTask);
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
		pdfIndicatorTaskService.removeById(id);
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
		this.pdfIndicatorTaskService.removeByIds(Arrays.asList(ids.split(",")));
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
		PdfIndicatorTask pdfIndicatorTask = pdfIndicatorTaskService.getById(id);
		if(pdfIndicatorTask==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(pdfIndicatorTask);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param pdfIndicatorTask
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdfIndicatorTask pdfIndicatorTask) {
        return super.exportXls(request, pdfIndicatorTask, PdfIndicatorTask.class, "PDF指标抽取任务表");
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
        return super.importExcel(request, response, PdfIndicatorTask.class);
    }

    /**
     * 创建PDF指标抽取任务
     *
     * @param pdfId PDF文档ID
     * @param indicatorTypes 要抽取的指标类型列表
     * @return 任务ID
     */
    @PostMapping(value = "/createTask")
    public Result<?> createTask(@RequestParam(name="pdfId", required=true) String pdfId,
                                @RequestParam(name="indicatorTypes", required=true) List<String> indicatorTypes) {
        try {
            PdfIndicatorTask task = pdfIndicatorTaskService.createTask(pdfId, indicatorTypes);
            return Result.OK(task.getTaskId(), "任务创建成功！");
        } catch (Exception e) {
            log.error("创建任务失败：", e);
            return Result.error("创建任务失败：" + e.getMessage());
        }
    }

    /**
     * 执行PDF指标抽取任务
     *
     * @param taskId 任务ID
     * @return 执行结果
     */
    @PostMapping(value = "/executeTask")
    public Result<?> executeTask(@RequestParam(name="taskId", required=true) String taskId) {
        try {
            Map<String, Object> result = pdfIndicatorTaskService.executeTask(taskId);
            return Result.OK(result, "任务执行成功！");
        } catch (Exception e) {
            log.error("执行任务失败：", e);
            return Result.error("执行任务失败：" + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping(value = "/queryTaskStatus")
    public Result<?> queryTaskStatus(@RequestParam(name="taskId", required=true) String taskId) {
        try {
            Map<String, Object> status = pdfIndicatorTaskService.queryTaskStatus(taskId);
            return Result.OK(status);
        } catch (Exception e) {
            log.error("查询任务状态失败：", e);
            return Result.error("查询任务状态失败：" + e.getMessage());
        }
    }

    /**
     * 查询任务结果
     *
     * @param taskId 任务ID
     * @return 任务结果
     */
    @GetMapping(value = "/queryTaskResult")
    public Result<?> queryTaskResult(@RequestParam(name="taskId", required=true) String taskId) {
        try {
            Map<String, Object> result = pdfIndicatorTaskService.queryTaskResult(taskId);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("查询任务结果失败：", e);
            return Result.error("查询任务结果失败：" + e.getMessage());
        }
    }

}
