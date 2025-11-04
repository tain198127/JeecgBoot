package org.jeecg.modules.srs.inspector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.Parameter;
import org.jeecg.modules.srs.inspector.mapper.ParameterMapper;
import org.jeecg.modules.srs.inspector.service.IParameterService;
import org.springframework.stereotype.Service;

/**
 * @Description: 参数
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class ParameterServiceImpl extends ServiceImpl<ParameterMapper, Parameter> implements IParameterService {

    @Override
    public List<Parameter> getParamsByEndpointId(String endpointId, Integer paramType) {
        LambdaQueryWrapper<Parameter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Parameter::getEndpointId, endpointId);
        if (paramType != null) {
            queryWrapper.eq(Parameter::getParamType, paramType);
        }
        return this.list(queryWrapper);
    }
}
