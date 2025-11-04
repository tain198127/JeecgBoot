package org.jeecg.modules.srs.inspector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.CallChain;
import org.jeecg.modules.srs.inspector.mapper.CallChainMapper;
import org.jeecg.modules.srs.inspector.service.ICallChainService;
import org.springframework.stereotype.Service;

/**
 * @Description: 调用链
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class CallChainServiceImpl extends ServiceImpl<CallChainMapper, CallChain> implements ICallChainService {

    @Override
    public List<CallChain> getCallChainByEndpointId(String endpointId) {
        LambdaQueryWrapper<CallChain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CallChain::getEndpointId, endpointId)
                .orderByAsc(CallChain::getLevel);
        return this.list(queryWrapper);
    }
}
