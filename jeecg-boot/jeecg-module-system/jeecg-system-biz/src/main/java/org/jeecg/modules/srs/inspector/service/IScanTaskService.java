package org.jeecg.modules.srs.inspector.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.srs.inspector.entity.ScanTask;

/**
 * @Description: 扫描任务
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
public interface IScanTaskService extends IService<ScanTask> {

    /**
     * 触发扫描
     * @param scanTask 扫描任务信息
     * @return 扫描任务ID
     */
    String triggerScan(ScanTask scanTask);

    /**
     * 获取扫描任务状态
     * @param taskId 任务ID
     * @return 扫描任务
     */
    ScanTask getTaskStatus(String taskId);
}
