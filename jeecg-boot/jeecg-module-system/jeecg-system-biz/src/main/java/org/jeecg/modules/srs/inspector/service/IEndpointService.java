package org.jeecg.modules.srs.inspector.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.srs.inspector.entity.Endpoint;

/**
 * @Description: 接口
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
public interface IEndpointService extends IService<Endpoint> {

    /**
     * 分页查询接口列表
     * @param page 分页信息
     * @param endpoint 查询条件
     * @return 分页结果
     */
    IPage<Endpoint> queryPageList(Page<Endpoint> page, Endpoint endpoint);

    /**
     * 根据接口ID获取详情
     * @param id 接口ID
     * @return 接口详情
     */
    Endpoint getEndpointDetail(String id);
}
