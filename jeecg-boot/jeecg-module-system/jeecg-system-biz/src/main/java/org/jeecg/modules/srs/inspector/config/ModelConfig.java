package org.jeecg.modules.srs.inspector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 模型服务配置类
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Configuration
public class ModelConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
