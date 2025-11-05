package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jeecg.modules.srs.inspector.entity.CallChain;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;



/**
 * 调用链分析器
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Component
public class CallChainAnalyzer {

    private CodeParser codeParser;
    private Map<String, ClassOrInterfaceDeclaration> classMap;
    private Map<String, MethodDeclaration> methodMap;

    @Autowired
    public CallChainAnalyzer(CodeParser codeParser) {
        this.codeParser = codeParser;
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
    }

    /**
     * 初始化分析器，加载所有类和方法
     * @param javaFiles Java文件列表
     * @throws IOException IO异常
     */
    public void init(List<File> javaFiles) throws IOException {
        for (File file : javaFiles) {
            CompilationUnit cu = codeParser.parseFile(file);
            
            // 遍历所有类和接口
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cls, Void arg) {
                        String className = cls.getFullyQualifiedName().orElse("");
                        classMap.put(className, cls);
                        
                        // 遍历所有方法
                        for (MethodDeclaration method : cls.getMethods()) {
                            String methodKey = className + "." + method.getNameAsString();
                            methodMap.put(methodKey, method);
                        }

                    super.visit(cls, arg);
                }
            }, null);
        }
    }

    /**
     * 构建接口的调用链
     * @param endpoint 接口信息
     * @return 调用链列表
     */
    public List<CallChain> buildCallChain(Endpoint endpoint) {
        List<CallChain> callChains = new ArrayList<>();
        
        // 构建Controller节点
        CallChain controllerChain = new CallChain();
        controllerChain.setEndpointId(endpoint.getId());
        controllerChain.setLevel(0);
        controllerChain.setCallType(0); // 0-Controller
        controllerChain.setClassName(endpoint.getControllerName());
        controllerChain.setMethodName(endpoint.getMethodName());
        controllerChain.setDescription("Controller方法");
        callChains.add(controllerChain);
        
        // 查找Controller方法调用的Service方法
        String controllerMethodKey = endpoint.getControllerName() + "." + endpoint.getMethodName();
        MethodDeclaration controllerMethod = methodMap.get(controllerMethodKey);
        
        if (controllerMethod != null) {
            List<MethodCallExpr> methodCalls = codeParser.getMethodCalls(controllerMethod);
            
            for (MethodCallExpr methodCall : methodCalls) {
                // 简单处理：假设是通过字段调用的方法，如userService.getUser()
                if (methodCall.getScope().isPresent() && methodCall.getScope().get() instanceof NameExpr) {
                    NameExpr scope = (NameExpr) methodCall.getScope().get();
                    String fieldName = scope.getNameAsString();
                    String methodName = methodCall.getNameAsString();
                    
                    // 查找对应的Service类和方法
                    ClassOrInterfaceDeclaration controllerClass = classMap.get(endpoint.getControllerName());
                    if (controllerClass != null) {
                        // 查找字段对应的类型
                        String serviceClassName = getFieldType(controllerClass, fieldName);
                        if (!serviceClassName.isEmpty()) {
                            // 构建Service节点
                            CallChain serviceChain = new CallChain();
                            serviceChain.setEndpointId(endpoint.getId());
                            serviceChain.setLevel(1);
                            serviceChain.setCallType(1); // 1-Service
                            serviceChain.setClassName(serviceClassName);
                            serviceChain.setMethodName(methodName);
                            serviceChain.setDescription("Service方法");
                            callChains.add(serviceChain);
                            
                            // 查找Service方法调用的Mapper方法
                            String serviceMethodKey = serviceClassName + "." + methodName;
                            MethodDeclaration serviceMethod = methodMap.get(serviceMethodKey);
                            
                            if (serviceMethod != null) {
                                List<MethodCallExpr> serviceMethodCalls = codeParser.getMethodCalls(serviceMethod);
                                
                                for (MethodCallExpr serviceMethodCall : serviceMethodCalls) {
                                    if (serviceMethodCall.getScope().isPresent() && serviceMethodCall.getScope().get() instanceof NameExpr) {
                                        NameExpr serviceScope = (NameExpr) serviceMethodCall.getScope().get();
                                        String mapperFieldName = serviceScope.getNameAsString();
                                        String mapperMethodName = serviceMethodCall.getNameAsString();
                                        
                                        // 查找对应的Mapper类
                                        ClassOrInterfaceDeclaration serviceClass = classMap.get(serviceClassName);
                                        if (serviceClass != null) {
                                            String mapperClassName = getFieldType(serviceClass, mapperFieldName);
                                            if (!mapperClassName.isEmpty()) {
                                                // 检查是否是Mapper对象（带有@Mapper注解）
                                                ClassOrInterfaceDeclaration mapperClass = classMap.get(mapperClassName);
                                                boolean isMapper = mapperClass != null && mapperClass.getAnnotations().stream()
                                                        .anyMatch(anno -> anno.getNameAsString().equals("Mapper"));
                                                
                                                // 构建Mapper节点
                                                CallChain mapperChain = new CallChain();
                                                mapperChain.setEndpointId(endpoint.getId());
                                                mapperChain.setLevel(isMapper ? 2 : 1); // 如果是Mapper对象，层级为2，否则为1
                                                mapperChain.setCallType(isMapper ? 2 : 1); // 2-Mapper, 1-Service
                                                mapperChain.setClassName(mapperClassName);
                                                mapperChain.setMethodName(mapperMethodName);
                                                mapperChain.setDescription(isMapper ? "Mapper方法" : "Service方法");
                                                
                                                // 如果是Mapper对象，查找对应的XML文件中的SQL内容
                                                if (isMapper) {
                                                    String sqlContent = findMapperXmlSql(mapperClassName, mapperMethodName);
                                                    mapperChain.setSqlContent(sqlContent);
                                                }
                                                
                                                callChains.add(mapperChain);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return callChains;
    }

    /**
     * 获取类中字段的类型
     * @param cls 类声明
     * @param fieldName 字段名
     * @return 字段类型的全限定名
     */
    private String getFieldType(ClassOrInterfaceDeclaration cls, String fieldName) {
        // 查找类中的字段并返回其全限定名
        return cls.getFields().stream()
                .filter(field -> field.getVariables().stream()
                        .anyMatch(var -> var.getNameAsString().equals(fieldName)))
                .findFirst()
                .map(field -> {
                    // 获取字段的类型
                    String typeName = field.getElementType().asString();
                    
                    // 如果是基本类型，直接返回
                    if (typeName.equals("int") || typeName.equals("long") || typeName.equals("double") || 
                        typeName.equals("float") || typeName.equals("boolean") || typeName.equals("char") || 
                        typeName.equals("byte") || typeName.equals("short")) {
                        return typeName;
                    }
                    
                    // 如果类型名已经包含包名（有.分隔符），直接返回
                    if (typeName.contains(".")) {
                        return typeName;
                    }
                    
                    // 对于简单类型名，需要解析导入语句来获取全限定名
                    return resolveFullQualifiedName(cls, typeName);
                })
                .orElse("");
    }
    
    /**
     * 解析类型的全限定名
     * @param cls 类声明
     * @param typeName 类型名
     * @return 全限定名
     */
    private String resolveFullQualifiedName(ClassOrInterfaceDeclaration cls, String typeName) {
        // 获取类的编译单元
        CompilationUnit cu = cls.findCompilationUnit().orElse(null);
        if (cu == null) {
            return typeName;
        }
        
        // 检查导入语句
        for (com.github.javaparser.ast.ImportDeclaration importDecl : cu.getImports()) {
            String importName = importDecl.getNameAsString();

            // 处理通配符导入（如 import baoming.*）
            if (importDecl.isAsterisk()) {
                String packageName = importDecl.getNameAsString();
                List<String> typeNamelist = classMap.keySet().stream().filter(item->item.contains(packageName) && item.endsWith(typeName)).toList();

//                String fullName = packageName + "." + typeName;
                
                // 检查类是否存在于通配符导入的包中
                if(!typeNamelist.isEmpty()) {
                    String fullName = typeNamelist.get(0);
                    // 如果类型是接口，查找实现类
                    if (isInterface(fullName)) {
                        String implClassName = findImplementationClass(fullName);
                        if (!implClassName.isEmpty()) {
                            return implClassName;
                        }
                    }
                    return fullName;
                }
            }
            
            // 处理明确导入（如 import baoming.User）
            if (importName.endsWith("." + typeName)) {
                // 如果类型是接口，查找实现类
                if (isInterface(importName)) {
                    String implClassName = findImplementationClass(importName);
                    if (!implClassName.isEmpty()) {
                        return implClassName;
                    }
                }
                return importName;
            }
        }
        
        // 检查是否在同一个包中
        String packageName = cu.getPackageDeclaration().map(pkg -> pkg.getNameAsString()).orElse("");
        if (!packageName.isEmpty()) {
            String fullName = packageName + "." + typeName;
            // 如果类型是接口，查找实现类
            if (isInterface(fullName)) {
                String implClassName = findImplementationClass(fullName);
                if (!implClassName.isEmpty()) {
                    return implClassName;
                }
            }
            return fullName;
        }
        
        // 默认返回类型名
        return typeName;
    }
    
    /**
     * 判断类型是否为接口
     * @param fullName 全限定名
     * @return 是否为接口
     */
    private boolean isInterface(String fullName) {
        ClassOrInterfaceDeclaration cls = classMap.get(fullName);
        return cls != null && cls.isInterface();
    }
    
    /**
     * 查找接口的实现类
     * @param interfaceName 接口全限定名
     * @return 实现类的全限定名
     */
    private String findImplementationClass(String interfaceName) {
        for (Map.Entry<String, ClassOrInterfaceDeclaration> entry : classMap.entrySet()) {
            ClassOrInterfaceDeclaration cls = entry.getValue();
            if (!cls.isInterface() && cls.getImplementedTypes().stream()
                    .anyMatch(type -> type.getNameAsString().equals(interfaceName.substring(interfaceName.lastIndexOf('.') + 1)))) {
                return entry.getKey();
            }
        }
        return "";
    }
    
    /**
     * 查找与 Mapper 类对应的 XML 文件中的 SQL 内容
     * @param mapperClassName Mapper 类的全限定名
     * @param methodName 方法名
     * @return SQL 内容，如果未找到则返回空字符串
     */
    private String findMapperXmlSql(String mapperClassName, String methodName) {
        try {
            // 获取 Mapper 类的简单名称
            String mapperSimpleName = mapperClassName.substring(mapperClassName.lastIndexOf('.') + 1);
            
            // 搜索工程中的 resources 目录
            String resourcesPath = "/Users/baodan/develop/git/trae_demo/JeecgBoot/jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/resources";
            File resourcesDir = new File(resourcesPath);
            if (!resourcesDir.exists()) {
                return "";
            }
            
            // 递归搜索 XML 文件
            File xmlFile = findMapperXmlFile(resourcesDir, mapperSimpleName);
            if (xmlFile == null) {
                return "";
            }
            
            // 解析 XML 文件
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            // 查找与方法名匹配的 SQL 节点
            NodeList nodeList = document.getElementsByTagName("select");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (methodName.equals(element.getAttribute("id"))) {
                        return element.getTextContent().trim();
                    }
                }
            }
            
            // 检查其他可能的 SQL 节点类型（如 insert、update、delete）
            String[] sqlNodeTypes = {"insert", "update", "delete"};
            for (String nodeType : sqlNodeTypes) {
                nodeList = document.getElementsByTagName(nodeType);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        if (methodName.equals(element.getAttribute("id"))) {
                            return element.getTextContent().trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 递归搜索与 Mapper 类名匹配的 XML 文件
     * @param dir 搜索目录
     * @param mapperSimpleName Mapper 类的简单名称
     * @return 匹配的 XML 文件，如果未找到则返回 null
     */
    private File findMapperXmlFile(File dir, String mapperSimpleName) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                File result = findMapperXmlFile(file, mapperSimpleName);
                if (result != null) {
                    return result;
                }
            } else if (file.getName().equals(mapperSimpleName + ".xml")) {
                return file;
            }
        }
        return null;
    }
}