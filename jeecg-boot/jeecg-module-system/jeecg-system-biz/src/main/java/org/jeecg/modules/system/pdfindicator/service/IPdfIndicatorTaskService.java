package org.jeecg.modules.system.pdfindicator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.pdfindicator.entity.PdfIndicatorTask;

import java.util.List;
import java.util.Map;

/**
 * @Description: PDF指标抽取任务表
 * @Author: jeecg-boot
 * @Date:   2025-06-13
 * @Version: V1.0
 */
public interface IPdfIndicatorTaskService extends IService<PdfIndicatorTask> {

    /**
     * 创建PDF指标抽取任务
     * @param pdfId PDF文档ID
     * @param indicatorTypes 要抽取的指标类型列表
     * @return 创建的任务信息
     */
    PdfIndicatorTask createTask(String pdfId, List<String> indicatorTypes);

    /**
     * 执行PDF指标抽取任务
     * @param taskId 任务ID
     * @return 任务执行结果
     * @throws Exception 执行异常
     */
    Map<String, Object> executeTask(String taskId) throws Exception;

    /**
     * 查询任务执行状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> queryTaskStatus(String taskId);

    /**
     * 查询任务执行结果
     * @param taskId 任务ID
     * @return 任务执行结果
     */
    Map<String, Object> queryTaskResult(String taskId);

}
