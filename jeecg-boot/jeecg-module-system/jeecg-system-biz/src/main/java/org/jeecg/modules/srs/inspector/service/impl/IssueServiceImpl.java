package org.jeecg.modules.srs.inspector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.Issue;
import org.jeecg.modules.srs.inspector.mapper.IssueMapper;
import org.jeecg.modules.srs.inspector.service.IIssueService;
import org.springframework.stereotype.Service;

/**
 * @Description: 问题
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class IssueServiceImpl extends ServiceImpl<IssueMapper, Issue> implements IIssueService {

    @Override
    public List<Issue> getIssuesByEndpointId(String endpointId) {
        LambdaQueryWrapper<Issue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Issue::getEndpointId, endpointId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Issue> getIssuesByTaskId(String taskId) {
        LambdaQueryWrapper<Issue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Issue::getEndpointId, taskId);
        return this.list(queryWrapper);
    }
}
