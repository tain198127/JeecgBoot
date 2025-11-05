package org.jeecg.modules.srs.inspector.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.srs.inspector.config.SrsInspectorConfig;
import org.jeecg.modules.srs.inspector.service.IModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型服务实现类
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Service
public class ModelServiceImpl implements IModelService {

    @Autowired
    private SrsInspectorConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String generateDocumentation(String content) {
        SrsInspectorConfig.ModelConfig modelConfig = config.getModel();
        if (modelConfig == null) {
            throw new RuntimeException("模型配置未初始化");
        }

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + modelConfig.getApiKey());

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelConfig.getModelName());
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "你是一个专业的API文档生成助手，请根据提供的调用链和代码内容生成清晰易懂的API说明文档。"),
                Map.of("role", "user", "content", content)
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2048);

        // 发送请求
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                modelConfig.getBaseUrl(),
                HttpMethod.POST,
                entity,
                String.class
        );

        // 解析响应
        JSONObject responseJson = JSON.parseObject(response.getBody());
        return responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }
}
