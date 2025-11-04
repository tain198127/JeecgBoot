package org.jeecg.modules.srs.inspector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SRS Inspector 配置类
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Component
@ConfigurationProperties(prefix = "srs.inspector")
public class SrsInspectorConfig {

    /** 数据库连接配置 */
    private DatabaseConfig database;

    /** 扫描路径 */
    private List<String> scanPaths;

    /** 并发度 */
    private Integer concurrency = 5;

    /** 模型配置 */
    private ModelConfig model;

    /** 排除的目录 */
    private List<String> excludeDirs;

    // Getters and Setters

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public List<String> getScanPaths() {
        return scanPaths;
    }

    public void setScanPaths(List<String> scanPaths) {
        this.scanPaths = scanPaths;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public ModelConfig getModel() {
        return model;
    }

    public void setModel(ModelConfig model) {
        this.model = model;
    }

    public List<String> getExcludeDirs() {
        return excludeDirs;
    }

    public void setExcludeDirs(List<String> excludeDirs) {
        this.excludeDirs = excludeDirs;
    }

    /**
     * 数据库连接配置
     */
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        // Getters and Setters

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }

    /**
     * 模型配置
     */
    public static class ModelConfig {
        private String baseUrl;
        private String modelName;
        private Integer timeout = 30000;
        private Integer retryTimes = 3;
        private String apiKey;

        // Getters and Setters

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
