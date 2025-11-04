package org.jeecg.modules.srs.inspector.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.Issue;

/**
 * @Description: 问题
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
public interface IIssueService extends IService<Issue> {

    /**
     * 根据接口ID获取问题列表
     * @param endpointId 接口ID
     * @return 问题列表
     */
    List<Issue> getIssuesByEndpointId(String endpointId);

    /**
     * 根据任务ID获取问题列表
     * @param taskId 任务ID
     * @return 问题列表
     */
    List<Issue> getIssuesByTaskId(String taskId);
}
