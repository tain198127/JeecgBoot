package org.jeecg.modules.system.pdfindicator.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.pdfindicator.entity.PdfIndicatorValidation;
import org.jeecg.modules.system.pdfindicator.service.IPdfIndicatorValidationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: PDF指标校验结果
 * @Author: jeecg-boot
 * @Date:   2025-06-27
 * @Version: V1.0
 */
@RestController
@RequestMapping("/pdfindicator/pdfIndicatorValidation")
@Slf4j
public class PdfIndicatorValidationController extends JeecgController<PdfIndicatorValidation, IPdfIndicatorValidationService> {
    @Autowired
    private IPdfIndicatorValidationService pdfIndicatorValidationService;

    /**
     * 分页列表查询
     *
     * @param pdfIndicatorValidation
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(PdfIndicatorValidation pdfIndicatorValidation,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        IPage<PdfIndicatorValidation> pageList = pdfIndicatorValidationService.page(new Page<>(pageNo, pageSize), QueryGenerator.initQueryWrapper(pdfIndicatorValidation, req.getParameterMap()));
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param pdfIndicatorValidation
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody PdfIndicatorValidation pdfIndicatorValidation) {
        pdfIndicatorValidationService.save(pdfIndicatorValidation);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param pdfIndicatorValidation
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody PdfIndicatorValidation pdfIndicatorValidation) {
        pdfIndicatorValidationService.updateById(pdfIndicatorValidation);
        return Result.OK("编辑成功！");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        pdfIndicatorValidationService.removeById(id);
        return Result.OK("删除成功！");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.pdfIndicatorValidationService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "PDF指标校验结果-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
        PdfIndicatorValidation pdfIndicatorValidation = pdfIndicatorValidationService.getById(id);
        if(pdfIndicatorValidation==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(pdfIndicatorValidation);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param pdfIndicatorValidation
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PdfIndicatorValidation pdfIndicatorValidation) {
        return super.exportXls(request, pdfIndicatorValidation, PdfIndicatorValidation.class, "PDF指标校验结果");
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
        return super.importExcel(request, response, PdfIndicatorValidation.class);
    }

}
