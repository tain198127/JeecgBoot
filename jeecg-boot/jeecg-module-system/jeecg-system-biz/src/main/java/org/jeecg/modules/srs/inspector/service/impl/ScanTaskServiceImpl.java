package org.jeecg.modules.srs.inspector.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.srs.inspector.entity.ScanTask;
import org.jeecg.modules.srs.inspector.mapper.ScanTaskMapper;
import org.jeecg.modules.srs.inspector.service.IScanTaskService;
import org.springframework.stereotype.Service;

/**
 * @Description: 扫描任务
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class ScanTaskServiceImpl extends ServiceImpl<ScanTaskMapper, ScanTask> implements IScanTaskService {

    @Override
    public String triggerScan(ScanTask scanTask) {
        // TODO: 实现扫描逻辑
        return null;
    }

    @Override
    public ScanTask getTaskStatus(String taskId) {
        // TODO: 实现获取任务状态逻辑
        return null;
    }
}
