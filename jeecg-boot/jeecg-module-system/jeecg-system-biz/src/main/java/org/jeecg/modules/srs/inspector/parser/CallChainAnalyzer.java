package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.jeecg.modules.srs.inspector.entity.CallChain;
import org.jeecg.modules.srs.inspector.entity.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.MappedStatement;



/**
 * 调用链分析器
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Slf4j
@Component
public class CallChainAnalyzer {

    // 解析 XML 文件
    Configuration mybatisConfiguration = new Configuration() {
        @Override
        public TypeAliasRegistry getTypeAliasRegistry() {
            return new TypeAliasRegistry() {
                @Override
                public <T> Class<T> resolveAlias(String alias) {
                    try {
                        return super.resolveAlias(alias);
                    } catch (Exception e) {
                        return (Class<T>) Object.class;
                    }
                }
            };
        }

    };


    private CodeParser codeParser;
    private Map<String, ClassOrInterfaceDeclaration> classMap;
    private Map<String, MethodDeclaration> methodMap;
    private Map<String, Integer> classComplexMap;
    private Map<String,Integer> methodComplexMap;

    @Autowired
    public CallChainAnalyzer(CodeParser codeParser) {
        this.codeParser = codeParser;
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
        this.classComplexMap = new HashMap();
        this.methodComplexMap = new HashMap<>();
    }

    /**
     * 初始化分析器，加载所有类和方法
     * @param javaFiles Java文件列表
     * @throws IOException IO异常
     */
    public void init(List<File> javaFiles) throws IOException {
        for (File file : javaFiles) {
            CompilationUnit cu = codeParser.parseFile(file);

            int complexLevel= CyclomaticComplexityCalculator.calculateComplexity(cu);

            // 遍历所有类和接口
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cls, Void arg) {

                        String className = cls.getFullyQualifiedName().orElse("");
                        classMap.put(className, cls);
                        classComplexMap.put(className,complexLevel);
                        // 遍历所有方法
                        for (MethodDeclaration method : cls.getMethods()) {
                            String methodKey = className + "." + method.getNameAsString();
                            methodMap.put(methodKey, method);
                            int methodComplexLevel = MethodComplexityAnalyzer.calculateMethodComplexity(method);
                            methodComplexMap.put(methodKey,methodComplexLevel);

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
            // 从 mybatisXmlMap 中获取对应的 XML 文件
//            mapperMethodMap.values().stream().filter(item->item.getMapperClassFullName().equals(mapperClassName))
            File xmlFile = mybatisXmlMap.get(mapperClassName);
            if (xmlFile == null) {
                return "";
            }

            // 使用 MyBatis 工具类解析 XML 文件

            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                new FileInputStream(xmlFile),
                    mybatisConfiguration,
                xmlFile.getAbsolutePath(),
                    mybatisConfiguration.getSqlFragments()
            );
            xmlMapperBuilder.parse();

            // 从 Configuration 中获取 MappedStatement
            String statementId = mapperClassName + "." + methodName;
            if (mybatisConfiguration.hasStatement(statementId)) {
                return mybatisConfiguration.getMappedStatement(statementId).getBoundSql(null).getSql();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // ====================== 以下为新增：Mapper 初始化与方法签名采集 ======================
    /**
     * 存放判定为 MyBatis Mapper 的 XML 文件映射，key 为 mapper 的 namespace（对应 Java 接口全限定名），value 为对应的 XML 文件
     */
    private final Map<String, File> mybatisXmlMap = new HashMap<>();

    /**
     * 统一方法签名映射表，key = mapperFullClassName + "." + methodName
     */
    private final Map<String, MapperMethodInfo> mapperMethodMap = new HashMap<>();

    /**
     * 初始化并加载指定目录下的 MyBatis Mapper XML：
     * 1) 递归扫描目录 *.xml；
     * 2) 使用 MyBatis 工具类校验是否为合法的 MyBatis XML；
     * 3) 解析 XML 文件并采集 Mapper 类名、方法名和参数类型；
     * 4) 将有效的 mapper 文件加入 mybatisXmlMap；所有方法签名加入 mapperMethodMap。
     *
     * @param directoryPath 待扫描的根目录绝对路径或相对路径
     */
    public void initmapper(String directoryPath) {
        mybatisXmlMap.clear();
        mapperMethodMap.clear();

        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }
        File root = new File(directoryPath);
        if (!root.exists() || !root.isDirectory()) {
            return;
        }

        List<File> xmlFiles = new ArrayList<>();
        collectXmlFiles(root, xmlFiles);

        for (File xml : xmlFiles) {
            try {
                // 使用 MyBatis 工具类验证 XML 是否为合法的 MyBatis Mapper
                if (!isValidMyBatisXml(xml)) {
                    continue;
                }




                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                    new FileInputStream(xml),
                        mybatisConfiguration,
                    xml.getAbsolutePath(),
                        mybatisConfiguration.getSqlFragments()
                );
                xmlMapperBuilder.parse();


            } catch (Exception ignore) {
                log.debug("识别mapper失败", ignore);
                // 非法或不可解析的 XML 直接忽略
            }
        }
        // 获取解析后的 MappedStatement 信息
        @NotNull List<MappedStatement> statements = mybatisConfiguration.getMappedStatements().stream().distinct().collect(Collectors.toList());

        for (int i = 0; i < statements.size();i++) {
            try {
                if(statements.get(i) instanceof MappedStatement) {
                    MappedStatement stat = statements.get(i);
                    try {
                        String namespace = stat.getId().substring(0, stat.getId().lastIndexOf('.'));
                        String methodName = stat.getId().substring(stat.getId().lastIndexOf('.') + 1);
                        MappedStatement mappedStatement = mybatisConfiguration.getMappedStatement(stat.getId());
                        String parameterType = mappedStatement.getParameterMap().getType() != null ?
                                mappedStatement.getParameterMap().getType().getName() : "";

                        // 存入 mybatisXmlMap 和 mapperMethodMap
                        mybatisXmlMap.put(namespace, new File(stat.getResource()));
                        mapperMethodMap.put(stat.getId(), new MapperMethodInfo(namespace, methodName, parameterType));
                    } catch (Exception ex) {
                        log.error("stat:{}报错", stat, ex);
                    }
                }
            }
            catch (Exception eex){
                log.error("解析mappered statment失败:{}",i,eex);
            }
        }
    }

    /**
     * 使用 MyBatis 工具类验证 XML 是否为合法的 MyBatis Mapper
     * @param xmlFile XML 文件
     * @return 是否为合法的 MyBatis Mapper
     */
    private boolean isValidMyBatisXml(File xmlFile) {
        try {
            // 使用 MyBatis 的 XMLMapperBuilder 进行验证

            mybatisConfiguration.setSafeResultHandlerEnabled(true); // 忽略无法解析的实体类
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                new FileInputStream(xmlFile),
                    mybatisConfiguration,
                xmlFile.getAbsolutePath(),
                    mybatisConfiguration.getSqlFragments()
            );
            xmlMapperBuilder.parse();
            return true;
        } catch (Exception e) {
            log.debug("XML 文件格式合法，但解析时遇到错误: {}", e.getMessage());
            return true; // 只要格式是 MyBatis 格式，就认为是合法的
        }
    }

    /**
     * 递归收集目录下的 .xml 文件
     */
    private void collectXmlFiles(File dir, List<File> out) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                collectXmlFiles(f, out);
            } else if (f.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {
                out.add(f);
            }
        }
    }

    /**
     * 将 parameterType 尽量解析为全限定名：
     * - 若为已知别名（MyBatis 常见别名），映射为对应 Java 全名；
     * - 若已是全限定名（包含'.'），原样返回；
     * - 若为空或未知，返回原值（可能为空字符串）。
     */
    private String resolveParameterTypeFQN(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String t = raw.trim();
        if (t.contains(".")) {
            return t; // 认为已是全限定名
        }
        // MyBatis 常见内置别名映射（不区分大小写）
        String k = t.toLowerCase(Locale.ROOT);
        Map<String, String> alias = getMyBatisAliasMap();
        if (alias.containsKey(k)) {
            return alias.get(k);
        }
        // 兜底返回原值（无法解析）
        return t;
    }

    private Map<String, String> getMyBatisAliasMap() {
        Map<String, String> m = new HashMap<>();
        m.put("byte", "java.lang.Byte");
        m.put("long", "java.lang.Long");
        m.put("short", "java.lang.Short");
        m.put("int", "java.lang.Integer");
        m.put("integer", "java.lang.Integer");
        m.put("double", "java.lang.Double");
        m.put("float", "java.lang.Float");
        m.put("boolean", "java.lang.Boolean");
        m.put("string", "java.lang.String");
        m.put("date", "java.util.Date");
        m.put("decimal", "java.math.BigDecimal");
        m.put("bigdecimal", "java.math.BigDecimal");
        m.put("object", "java.lang.Object");
        m.put("map", "java.util.Map");
        m.put("hashmap", "java.util.HashMap");
        m.put("list", "java.util.List");
        m.put("arraylist", "java.util.ArrayList");
        m.put("collection", "java.util.Collection");
        m.put("iterator", "java.util.Iterator");
        m.put("long[]", "long[]");
        m.put("int[]", "int[]");
        m.put("integer[]", "java.lang.Integer[]");
        m.put("string[]", "java.lang.String[]");
        return m;
    }

    /** 对外暴露：获取识别到的 MyBatis XML 映射（namespace -> xml文件） */
    public Map<String, File> getMybatisXmlMap() { return Collections.unmodifiableMap(mybatisXmlMap); }

    /** 对外暴露：获取统一方法签名映射（key=namespace.methodId） */
    public Map<String, MapperMethodInfo> getMapperMethodMap() { return Collections.unmodifiableMap(mapperMethodMap); }

    /**
     * Mapper 方法签名信息
     */
    public static class MapperMethodInfo {
        private final String mapperClassFullName;
        private final String methodName;
        private final String parameterTypeFullName;

        public MapperMethodInfo(String mapperClassFullName, String methodName, String parameterTypeFullName) {
            this.mapperClassFullName = mapperClassFullName;
            this.methodName = methodName;
            this.parameterTypeFullName = parameterTypeFullName;
        }

        public String getMapperClassFullName() { return mapperClassFullName; }
        public String getMethodName() { return methodName; }
        public String getParameterTypeFullName() { return parameterTypeFullName; }
    }
}
