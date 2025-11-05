package org.jeecg.modules.srs.inspector.service;

/**
 * 模型服务接口
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
public interface IModelService {

    /**
     * 生成API文档
     * @param content 调用链和代码内容
     * @return 生成的文档
     */
    String generateDocumentation(String content);
}
