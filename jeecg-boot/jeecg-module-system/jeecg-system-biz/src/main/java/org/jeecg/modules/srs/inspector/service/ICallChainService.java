package org.jeecg.modules.srs.inspector.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.CallChain;

/**
 * @Description: 调用链
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
public interface ICallChainService extends IService<CallChain> {

    /**
     * 根据接口ID获取调用链
     * @param endpointId 接口ID
     * @return 调用链列表
     */
    List<CallChain> getCallChainByEndpointId(String endpointId);
}
