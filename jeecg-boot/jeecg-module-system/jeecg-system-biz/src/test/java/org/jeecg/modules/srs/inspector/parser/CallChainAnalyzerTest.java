package org.jeecg.modules.srs.inspector.parser;

import cn.hutool.core.collection.ListUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Lists;
import org.jeecg.modules.srs.inspector.config.SrsInspectorConfig;
import org.jeecg.modules.srs.inspector.entity.CallChain;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallChainAnalyzerTest {

    @Spy
    private CodeParser codeParser;

    @InjectMocks
    private CallChainAnalyzer callChainAnalyzer;

    private Endpoint endpoint;
    private ClassOrInterfaceDeclaration controllerClass;
    private MethodDeclaration controllerMethod;
    private ClassOrInterfaceDeclaration serviceClass;
    private MethodDeclaration serviceMethod;
    private ClassOrInterfaceDeclaration mapperClass;
    private MethodDeclaration mapperMethod;

//    @BeforeEach
//    void setUp() {
//        // 初始化测试接口信息
//        endpoint = new Endpoint();
//        endpoint.setId("test-endpoint-id");
//        endpoint.setControllerName("com.example.controller.TestController");
//        endpoint.setMethodName("testMethod");
//
//        // 创建模拟的Controller类
//        controllerClass = mock(ClassOrInterfaceDeclaration.class);
//        when(controllerClass.getFullyQualifiedName()).thenReturn(Optional.of("com.example.controller.TestController"));
//
//        // 创建模拟的Controller方法
//        controllerMethod = mock(MethodDeclaration.class);
//        when(controllerMethod.getNameAsString()).thenReturn("testMethod");
//
//        // 创建模拟的Service类
//        serviceClass = mock(ClassOrInterfaceDeclaration.class);
//        when(serviceClass.getFullyQualifiedName()).thenReturn(Optional.of("com.example.service.TestService"));
//
//        // 创建模拟的Service方法
//        serviceMethod = mock(MethodDeclaration.class);
//        when(serviceMethod.getNameAsString()).thenReturn("testServiceMethod");
//
//        // 创建模拟的Mapper类
//        mapperClass = mock(ClassOrInterfaceDeclaration.class);
//        when(mapperClass.getFullyQualifiedName()).thenReturn(Optional.of("com.example.mapper.TestMapper"));
//
//        // 创建模拟的Mapper方法
//        mapperMethod = mock(MethodDeclaration.class);
//        when(mapperMethod.getNameAsString()).thenReturn("testMapperMethod");
//    }

    @Test
    void testBuildCallChain() throws IOException {
        // 创建模拟的Java文件和编译单元
        SrsInspectorConfig config = new SrsInspectorConfig();
        config.setScanPaths(ListUtil.of("/Users/baodan/develop/isoftstone/北金所债权管理系统/code/old_source-2025-9-1/all/nc-issuance-book-dcm/nc-issuance-book-dcm-module"));
        List<File> javaFiles = new ArrayList<>();

        for (String scanPath : config.getScanPaths()) {
            javaFiles.addAll(codeParser.scanJavaFiles(scanPath));
        }

        // Initialize the call chain analyzer
        callChainAnalyzer.init(javaFiles);
        Endpoint endpoint1 = new Endpoint();
        endpoint1.setControllerName("cn.nc.issuance.book.dcm.facade.controller.pricingplacing.PreGeneratePlacingResultController");
        endpoint1.setMethodName("doService");
        endpoint1.setId("test");
        // Build the call chains for the endpoint
        List<Endpoint> endpoints = Collections.singletonList(endpoint1);
        List<CallChain> callChains = callChainAnalyzer.buildCallChain(endpoint1);

    }

    @Test
    void testBuildCallChain_NoMethodCalls() throws IOException {
        // 创建模拟的Java文件和编译单元
        File mockFile = mock(File.class);
        CompilationUnit mockCU = mock(CompilationUnit.class);

        // 配置CodeParser解析文件返回模拟的编译单元
        when(codeParser.parseFile(mockFile)).thenReturn(mockCU);

        // 配置编译单元接受访问者并访问Controller类
        doAnswer(invocation -> {
            VoidVisitorAdapter<Void> visitor = invocation.getArgument(0);
            visitor.visit(controllerClass, null);
            return null;
        }).when(mockCU).accept(any(VoidVisitorAdapter.class), eq(null));

        // 配置Controller类不是接口且返回方法
        when(controllerClass.isInterface()).thenReturn(false);
        when(controllerClass.getMethods()).thenReturn(NodeList.nodeList(controllerMethod));

        // 配置CodeParser返回空的方法调用列表
        when(codeParser.getMethodCalls(controllerMethod)).thenReturn(List.of());

        // 初始化调用链分析器
        callChainAnalyzer.init(List.of(mockFile));

        // 构建调用链
        List<CallChain> callChains = callChainAnalyzer.buildCallChain(endpoint);

        // 验证结果：只有Controller节点
        assertNotNull(callChains);
        assertEquals(1, callChains.size());
        assertEquals("Controller方法", callChains.get(0).getDescription());
    }

    @Test
    void testBuildCallChain_MethodNotFound() throws IOException {
        // 创建模拟的Java文件和编译单元
        File mockFile = mock(File.class);
        CompilationUnit mockCU = mock(CompilationUnit.class);

        // 配置CodeParser解析文件返回模拟的编译单元
        when(codeParser.parseFile(mockFile)).thenReturn(mockCU);

        // 配置编译单元接受访问者但不访问任何类
        doAnswer(invocation -> null).when(mockCU).accept(any(VoidVisitorAdapter.class), eq(null));

        // 初始化调用链分析器
        callChainAnalyzer.init(List.of(mockFile));

        // 构建调用链
        List<CallChain> callChains = callChainAnalyzer.buildCallChain(endpoint);

        // 验证结果：只有Controller节点，因为方法不在methodMap中
        assertNotNull(callChains);
        assertEquals(1, callChains.size());
        assertEquals("Controller方法", callChains.get(0).getDescription());
    }
}