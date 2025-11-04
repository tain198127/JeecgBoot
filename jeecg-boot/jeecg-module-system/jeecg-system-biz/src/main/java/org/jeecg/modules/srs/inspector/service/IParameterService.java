package org.jeecg.modules.srs.inspector.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.jeecg.modules.srs.inspector.entity.Parameter;

/**
 * @Description: 参数
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
public interface IParameterService extends IService<Parameter> {

    /**
     * 根据接口ID获取参数列表
     * @param endpointId 接口ID
     * @param paramType 参数类型：0-入参，1-出参
     * @return 参数列表
     */
    List<Parameter> getParamsByEndpointId(String endpointId, Integer paramType);
}
