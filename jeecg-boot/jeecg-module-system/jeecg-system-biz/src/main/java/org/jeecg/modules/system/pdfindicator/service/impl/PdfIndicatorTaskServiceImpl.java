package org.jeecg.modules.system.pdfindicator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.system.pdfindicator.entity.PdfIndicatorTask;
import org.jeecg.modules.system.pdfindicator.mapper.PdfIndicatorTaskMapper;
import org.jeecg.modules.system.pdfindicator.service.IPdfIndicatorTaskService;
import org.jeecg.modules.system.pdfindicator.service.IPdfDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: PDF指标抽取任务表
 * @Author: jeecg-boot
 * @Date:   2025-06-13
 * @Version: V1.0
 */
@Service
public class PdfIndicatorTaskServiceImpl extends ServiceImpl<PdfIndicatorTaskMapper, PdfIndicatorTask> implements IPdfIndicatorTaskService {

    @Autowired
    private IPdfDocumentService pdfDocumentService;

    @Autowired
    private PdfIndicatorTaskMapper pdfIndicatorTaskMapper;

    @Override
    public PdfIndicatorTask createTask(String pdfId, List<String> indicatorTypes) {
        // 创建PDF指标抽取任务
        PdfIndicatorTask task = new PdfIndicatorTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setPdfId(pdfId);
        task.setIndicatorTypes(String.join(",", indicatorTypes));
        task.setTaskStatus("created");
        task.setCreateTime(new Date());

        // 保存到数据库
        save(task);

        return task;
    }

    @Override
    public Map<String, Object> executeTask(String taskId) throws Exception {
        // 根据ID查询任务
        PdfIndicatorTask task = getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("PDF指标抽取任务不存在");
        }

        // 更新任务状态为执行中
        task.setTaskStatus("running");
        task.setStartTime(new Date());
        updateById(task);

        try {
            // 执行PDF解析
            Map<String, Object> parseResult = pdfDocumentService.parsePdfContent(task.getPdfId());
            
            // 执行指标抽取
            Map<String, Object> extractResult = pdfDocumentService.extractIndicators(task.getPdfId());
            
            // 执行智能校验
            Map<String, Object> validateResult = pdfDocumentService.validateIndicators(task.getPdfId());

            // 更新任务状态为成功
            task.setTaskStatus("success");
            task.setEndTime(new Date());
            task.setTaskResult("PDF指标抽取和智能校验完成");
            updateById(task);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("taskStatus", "success");
            result.put("parseResult", parseResult);
            result.put("extractResult", extractResult);
            result.put("validateResult", validateResult);
            result.put("message", "PDF指标抽取和智能校验任务执行成功");

            return result;
        } catch (Exception e) {
            // 更新任务状态为失败
            task.setTaskStatus("failed");
            task.setEndTime(new Date());
            task.setTaskResult("PDF指标抽取和智能校验失败: " + e.getMessage());
            updateById(task);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("taskStatus", "failed");
            result.put("message", "PDF指标抽取和智能校验任务执行失败: " + e.getMessage());

            return result;
        }
    }

    @Override
    public Map<String, Object> queryTaskStatus(String taskId) {
        // 根据ID查询任务
        PdfIndicatorTask task = getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("PDF指标抽取任务不存在");
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("taskStatus", task.getTaskStatus());
        result.put("createTime", task.getCreateTime());
        result.put("startTime", task.getStartTime());
        result.put("endTime", task.getEndTime());
        result.put("message", "任务状态查询成功");

        return result;
    }

    @Override
    public Map<String, Object> queryTaskResult(String taskId) {
        // 根据ID查询任务
        PdfIndicatorTask task = getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("PDF指标抽取任务不存在");
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("taskStatus", task.getTaskStatus());
        result.put("taskResult", task.getTaskResult());
        result.put("createTime", task.getCreateTime());
        result.put("startTime", task.getStartTime());
        result.put("endTime", task.getEndTime());
        result.put("message", "任务结果查询成功");

        return result;
    }
}
