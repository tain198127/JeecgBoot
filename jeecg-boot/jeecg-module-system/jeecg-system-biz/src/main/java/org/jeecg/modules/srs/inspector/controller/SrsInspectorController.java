package org.jeecg.modules.srs.inspector.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.jeecg.modules.srs.inspector.entity.ScanTask;
import org.jeecg.modules.srs.inspector.service.IEndpointService;
import org.jeecg.modules.srs.inspector.service.ICallChainService;
import org.jeecg.modules.srs.inspector.service.IIssueService;
import org.jeecg.modules.srs.inspector.service.IParameterService;
import org.jeecg.modules.srs.inspector.service.IScanTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SRS Inspector Controller
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/srs/inspector")
public class SrsInspectorController {

    @Autowired
    private IScanTaskService scanTaskService;

    @Autowired
    private IEndpointService endpointService;

    @Autowired
    private IParameterService parameterService;

    @Autowired
    private ICallChainService callChainService;

    @Autowired
    private IIssueService issueService;

    /**
     * 触发扫描
     * @param scanTask 扫描任务信息
     * @return 扫描任务ID
     */
    @PostMapping("/triggerScan")
    public Result<String> triggerScan(@RequestBody ScanTask scanTask) {
        try {
            String taskId = scanTaskService.triggerScan(scanTask);
            return Result.OK(taskId, "扫描任务已启动");
        } catch (Exception e) {
            return Result.error("扫描任务启动失败：" + e.getMessage());
        }
    }

    /**
     * 获取扫描任务状态
     * @param taskId 任务ID
     * @return 扫描任务
     */
    @GetMapping("/getTaskStatus/{taskId}")
    public Result<ScanTask> getTaskStatus(@PathVariable String taskId) {
        try {
            ScanTask scanTask = scanTaskService.getTaskStatus(taskId);
            return Result.OK(scanTask);
        } catch (Exception e) {
            return Result.error("获取任务状态失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询接口列表
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @param endpoint 查询条件
     * @return 分页结果
     */
    @GetMapping("/getEndpoints")
    public Result<IPage<Endpoint>> getEndpoints(@RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                Endpoint endpoint) {
        try {
            Page<Endpoint> page = new Page<>(pageNo, pageSize);
            IPage<Endpoint> pageList = endpointService.queryPageList(page, endpoint);
            return Result.OK(pageList);
        } catch (Exception e) {
            return Result.error("查询接口列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取接口详情
     * @param id 接口ID
     * @return 接口详情
     */
    @GetMapping("/getDetail/{id}")
    public Result<Endpoint> getDetail(@PathVariable String id) {
        try {
            Endpoint endpoint = endpointService.getEndpointDetail(id);
            return Result.OK(endpoint);
        } catch (Exception e) {
            return Result.error("获取接口详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取参数列表
     * @param endpointId 接口ID
     * @param paramType 参数类型：0-入参，1-出参
     * @return 参数列表
     */
    @GetMapping("/getParams")
    public Result<List> getParams(@RequestParam String endpointId,
                                  @RequestParam Integer paramType) {
        try {
            List params = parameterService.getParamsByEndpointId(endpointId, paramType);
            return Result.OK(params);
        } catch (Exception e) {
            return Result.error("获取参数列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取问题列表
     * @param endpointId 接口ID
     * @return 问题列表
     */
    @GetMapping("/getIssues")
    public Result<List> getIssues(@RequestParam String endpointId) {
        try {
            List issues = issueService.getIssuesByEndpointId(endpointId);
            return Result.OK(issues);
        } catch (Exception e) {
            return Result.error("获取问题列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取调用链
     * @param endpointId 接口ID
     * @return 调用链
     */
    @GetMapping("/getCallChain")
    public Result<List> getCallChain(@RequestParam String endpointId) {
        try {
            List callChain = callChainService.getCallChainByEndpointId(endpointId);
            return Result.OK(callChain);
        } catch (Exception e) {
            return Result.error("获取调用链失败：" + e.getMessage());
        }
    }

    /**
     * 生成API文档
     * @param endpointId 接口ID
     * @return 生成的文档
     */
    @GetMapping("/generateDoc")
    public Result<String> generateDoc(@RequestParam String endpointId) {
        try {
            String doc = callChainService.generateDocumentation(endpointId);
            return Result.OK(doc);
        } catch (Exception e) {
            return Result.error("生成文档失败：" + e.getMessage());
        }
    }

    /**
     * 导出SRS
     * @param taskId 任务ID
     * @return 导出结果
     */
    @GetMapping("/exportSrs/{taskId}")
    public Result<String> exportSrs(@PathVariable String taskId) {
        try {
            // TODO: 实现导出SRS逻辑
            return Result.OK("导出成功");
        } catch (Exception e) {
            return Result.error("导出SRS失败：" + e.getMessage());
        }
    }
}
