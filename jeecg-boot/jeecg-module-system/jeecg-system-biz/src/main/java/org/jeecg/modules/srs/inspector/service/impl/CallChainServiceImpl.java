package org.jeecg.modules.srs.inspector.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.srs.inspector.config.SrsInspectorConfig;
import org.jeecg.modules.srs.inspector.entity.CallChain;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.jeecg.modules.srs.inspector.mapper.CallChainMapper;
import org.jeecg.modules.srs.inspector.parser.CodeParser;
import org.jeecg.modules.srs.inspector.parser.CallChainAnalyzer;
import org.jeecg.modules.srs.inspector.service.ICallChainService;
import org.jeecg.modules.srs.inspector.service.IEndpointService;
import org.jeecg.modules.srs.inspector.service.IModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Description: 调用链
 * @Author: jeecg-boot
 * @Date:   2025-01-01
 * @Version: V1.0
 */
@Service
public class CallChainServiceImpl extends ServiceImpl<CallChainMapper, CallChain> implements ICallChainService {

    private static final Logger log = LoggerFactory.getLogger(CallChainServiceImpl.class);

    @Autowired
    private SrsInspectorConfig config;

    @Autowired
    private CodeParser codeParser;

    @Autowired
    private CallChainAnalyzer callChainAnalyzer;

    @Autowired
    private IEndpointService endpointService;

    @Autowired
    private IModelService modelService;

    /**
     * 分析所有代码，并生成csv文件
     */
    public void scanAll() throws IOException {
        List<File> javaFiles = new ArrayList<>();
        for (String scanPath : config.getScanPaths()) {
            javaFiles.addAll(codeParser.scanJavaFiles(scanPath));
        }
        // Initialize the call chain analyzer
        callChainAnalyzer.init(javaFiles);
        callChainAnalyzer.initmapper(config.getScanPaths().get(0));
        List<Endpoint> controllers = callChainAnalyzer.scanAllEndpoint();
        
        // 生成CSV文件
        generateCsvFile(controllers);
    }
    
    /**
     * 生成CSV文件
     * @param controllers 接口列表
     */
    private void generateCsvFile(List<Endpoint> controllers) throws IOException {
        // 生成带时间戳的文件名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "controllers_" + timestamp + ".csv";
        
        // 构建CSV内容
        StringBuilder csvContent = new StringBuilder();
        // CSV头部
        csvContent.append("ID,METHOD,controllerName,score\n");
        
        // 写入数据
        for (Endpoint endpoint : controllers) {
            csvContent.append(endpoint.getId()).append(",")
                      .append(endpoint.getMethodName()).append(",")
                      .append(endpoint.getControllerName()).append(",")
                    .append(endpoint.getSumAllComplexScore())
                    .append("\n");
        }
        
        // 写入文件
        java.nio.file.Files.write(
            java.nio.file.Paths.get(fileName),
            csvContent.toString().getBytes("UTF-8")
        );
        
        log.info("CSV文件已生成: {}", fileName);
    }

    @Override
    public List<CallChain> getCallChainByEndpointId(String endpointId) {
        // Get the endpoint from the database
        Endpoint endpoint = endpointService.getById(endpointId);
        if (endpoint == null) {
            log.warn("Endpoint not found for ID: {}", endpointId);
            return new ArrayList<>();
        }

        try {
            // Scan Java files from the configured paths
            List<File> javaFiles = new ArrayList<>();
            for (String scanPath : config.getScanPaths()) {
                javaFiles.addAll(codeParser.scanJavaFiles(scanPath));
            }

            // Initialize the call chain analyzer
            callChainAnalyzer.init(javaFiles);

            // Build the call chains for the endpoint
            List<Endpoint> endpoints = Collections.singletonList(endpoint);
            Set<CallChain> flatCahin = new HashSet<>();
            return callChainAnalyzer.buildCallChain(endpoint,flatCahin);
        } catch (IOException e) {
            log.error("Failed to generate call chain for endpoint: {}", endpointId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String generateDocumentation(String endpointId) {
        // 1. 获取接口信息
        Endpoint endpoint = endpointService.getById(endpointId);
        if (endpoint == null) {
            throw new RuntimeException("接口不存在");
        }

        // 2. 获取调用链
        List<CallChain> callChains = getCallChainByEndpointId(endpointId);

        // 3. 构建请求内容
        StringBuilder content = new StringBuilder();
        content.append("接口信息：\n");
        content.append("URL: ").append(endpoint.getPath()).append("\n");
        content.append("HTTP方法: ").append(endpoint.getHttpMethod()).append("\n");
        content.append("Controller: ").append(endpoint.getControllerName()).append("\n");
        content.append("方法: ").append(endpoint.getMethodName()).append("\n\n");

        content.append("调用链：\n");
        for (CallChain callChain : callChains) {
            String type = callChain.getCallType() == 1 ? "Service" : callChain.getCallType() == 2 ? "Mapper" : "Other";
            content.append(type).append(": ").append(callChain.getClassName()).append(".").append(callChain.getMethodName()).append("\n");
        }

        content.append("\n代码内容：\n");
        content.append(endpoint.getCodeSnippet()).append("\n");

        // 4. 调用大模型生成文档
        return modelService.generateDocumentation(content.toString());
    }
}
