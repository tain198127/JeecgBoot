package org.jeecg.modules.srs.inspector.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.jeecg.modules.srs.inspector.mapper.EndpointMapper;
import org.jeecg.modules.srs.inspector.service.IEndpointService;
import org.springframework.stereotype.Service;

/**
 * @Description: 接口
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class EndpointServiceImpl extends ServiceImpl<EndpointMapper, Endpoint> implements IEndpointService {

    @Override
    public IPage<Endpoint> queryPageList(Page<Endpoint> page, Endpoint endpoint) {
        // TODO: 实现分页查询逻辑
        return null;
    }

    @Override
    public Endpoint getEndpointDetail(String id) {
        // TODO: 实现获取接口详情逻辑
        return null;
    }
}
