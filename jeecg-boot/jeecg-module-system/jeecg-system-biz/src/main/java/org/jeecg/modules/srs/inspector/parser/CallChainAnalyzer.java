package org.jeecg.modules.srs.inspector.parser;

import cn.hutool.core.bean.BeanUtil;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.jeecg.weibo.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.srs.inspector.entity.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * 调用链分析器
 *
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Slf4j
@Component
public class CallChainAnalyzer {

    private static final AtomicBoolean isClassInit = new AtomicBoolean(false);
    private static final AtomicBoolean isMapperInit = new AtomicBoolean(false);
    private static final String TARGET_INTERFACE = "com.baomidou.mybatisplus.core.mapper.BaseMapper";
    private static final String METHOD_SPANNER = "#";
    private static final String DOT = ".";

    /**
     * 存放判定为 MyBatis Mapper 的 XML 文件映射，key 为 mapper 的 namespace（对应 Java 接口全限定名），value 为对应的 XML 文件
     */
    private final Map<String, File> mybatisXmlMap = new HashMap<>();
    /**
     * 统一方法签名映射表，key = mapperFullClassName + "." + methodName
     */
    private final Map<String, MapperMethodInfo> mapperMethodMap = new HashMap<>();
    private final CodeParser codeParser;
    private final Map<String, ClassOrInterfaceDeclaration> classMap;
    private final Map<String, ClzAndMethod> methodMap;
    private final Map<String, Integer> classComplexMap;
    private final Map<String, Integer> methodComplexMap;
    private final Set<String> methodInvokeTag;
    private final Map<String, PomInfo> pomInfos =new HashMap<>();

    //reverse index 提速
    private final Map<PackageInfo,PomInfo> reverseIndexOfPackage = new HashMap<>();

    private final Map<ClassInfo, PackageInfo> reverseIndexOfClass2Pkg = new HashMap<>();
    private final Map<ClassInfo, PomInfo> reverseIndexOfClass2Pom = new HashMap<>();

    private final Map<CallChainAnalyzer.ClzAndMethod, ClassInfo> reverseIndexOfMethod2Clz = new HashMap<>();
    private final Map<CallChainAnalyzer.ClzAndMethod, PackageInfo> reverseIndexOfMethod2Pkg = new HashMap<>();
    private final Map<CallChainAnalyzer.ClzAndMethod, PomInfo> reverseIndexOfMethod2Pom = new HashMap<>();
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

    @Autowired
    public CallChainAnalyzer(CodeParser codeParser) {
        this.codeParser = codeParser;
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
        this.classComplexMap = new HashMap();
        this.methodComplexMap = new HashMap<>();
        this.methodInvokeTag = new HashSet<>();
    }

    /**
     * 跟idea的方法名对齐
     *
     * @param className
     * @param methodName
     * @return
     */
    public static String methodKey(String className, String methodName) {
        return className + METHOD_SPANNER + methodName;
    }

    /**
     * 将dotKey中最后一个"."替换为"#"
     *
     * @param dotKey 输入的字符串，格式为 className.methodName
     * @return 替换后的字符串，格式为 className#methodName
     */
    public static String processDotKey2SharpKey(String dotKey) {
        if (dotKey != null && dotKey.contains(".")) {
            int lastDotIndex = dotKey.lastIndexOf(".");
            return dotKey.substring(0, lastDotIndex) + "#" + dotKey.substring(lastDotIndex + 1);
        }
        return dotKey;
    }

    public static String processSharpKey2DotKey(String mapperId) {
        if (mapperId.lastIndexOf(METHOD_SPANNER) > 0) {
            mapperId.replace(METHOD_SPANNER, DOT);
        }
        return mapperId;
    }

    /**
     * 检查类型是否是 MyBatis 核心 Mapper 接口
     *
     * @param type 类型
     * @return 如果是 MyBatis 核心 Mapper 接口，返回 true；否则返回 false
     */
    private static boolean isMyBatisMapperType(ClassOrInterfaceType type) {
        String typeName = type.getNameAsString();
//        Optional<String> scope = type.getScope().map(scopeExpr -> scopeExpr.toString());

        // 检查是否是 MyBatis-Plus 的 Mapper 接口
        return typeName.equals("BaseMapper");
    }

    public static boolean extendsInterfaceSimple(ClassOrInterfaceDeclaration declaration, String targetInterface) {
        if (declaration == null || targetInterface == null) {
            return false;
        }

        if (!declaration.isInterface()) {
            return false;
        }

        try {
            boolean isInheritBaseMapper = declaration.getExtendedTypes().stream().anyMatch(item -> isMyBatisMapperType(item));
            if (!isInheritBaseMapper) {
                isInheritBaseMapper = extendsInterface(declaration, targetInterface);
            }
            return isInheritBaseMapper;
        } catch (Exception e) {
            // 解析失败时记录警告而不是错误，因为这在某些情况下是正常的
            String className = declaration.getFullyQualifiedName().orElse(declaration.getNameAsString());
            log.debug("Failed to resolve interface: {} when checking if it extends: {}, reason: {}",
                    className, targetInterface, e.getMessage());
            return false;
        }
    }

    /**
     * 判断接口是否继承了指定的目标接口（支持多级继承）
     *
     * @param declaration     要检查的接口声明
     * @param targetInterface 目标接口的全限定名，例如 "com.baomidou.mybatisplus.core.mapper.Mapper"
     * @return true 如果继承了目标接口，否则返回 false
     */
    public static boolean extendsInterface(ClassOrInterfaceDeclaration declaration, String targetInterface) {
        // 首先判断是否是接口
        if (!declaration.isInterface()) {
            return false;
        }

        // 使用集合避免循环继承导致的无限递归
        Set<String> visited = new HashSet<>();
        return checkInterfaceHierarchy(declaration, targetInterface, visited);
    }

    /**
     * 递归检查接口继承层次
     */
    private static boolean checkInterfaceHierarchy(
            ClassOrInterfaceDeclaration declaration,
            String targetInterface,
            Set<String> visited) {

        try {
            // 获取当前接口的全限定名
            String currentFullName = declaration.resolve().getQualifiedName();

            // 如果已经访问过，避免循环
            if (visited.contains(currentFullName)) {
                return false;
            }
            visited.add(currentFullName);

            // 检查当前接口是否就是目标接口
            if (currentFullName.equals(targetInterface)) {
                return true;
            }

            // 遍历所有直接继承的接口
            for (ClassOrInterfaceType extendedType : declaration.getExtendedTypes()) {
                try {
                    // 解析继承的接口类型
                    ResolvedReferenceType resolvedType = extendedType.resolve().asReferenceType();
                    String extendedInterfaceName = resolvedType.getQualifiedName();

                    // 检查直接继承的接口是否是目标接口
                    if (extendedInterfaceName.equals(targetInterface)) {
                        return true;
                    }

                    // 递归检查父接口
                    ResolvedReferenceTypeDeclaration typeDeclaration = resolvedType.getTypeDeclaration().orElse(null);
                    if (typeDeclaration != null && typeDeclaration.isInterface()) {
                        // 获取所有祖先接口
                        for (ResolvedReferenceType ancestorType : typeDeclaration.getAllAncestors()) {
                            if (ancestorType.getQualifiedName().equals(targetInterface)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 解析失败时忽略该接口，继续检查其他接口
                    log.debug("Failed to resolve extended type: " + extendedType.getNameAsString(), e);
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Failed to resolve declaration", e);
            return false;
        }
    }

    /**
     * 检查这个类属于哪个pom，补齐对应的package，class,methods
     * @param cu
     */
    private void processPomAndClass(CompilationUnit cu,ClassOrInterfaceDeclaration cd,List<ClzAndMethod> clzAndMethodList){

        if(cu.getStorage().isPresent()){
            Path path = cu.getStorage().get().getDirectory();
            String p = path.toString();
            List<String> hintsPath = pomInfos.keySet().stream().filter(item->p.contains(item)).toList();
            
            // 找到最长的路径（离当前路径最近的）
            String closestPath = hintsPath.stream()
                    .max(Comparator.comparingInt(String::length))
                    .orElse(null);
            
            if (closestPath != null) {
                log.debug("找到离当前路径最近的pom路径: {}", closestPath);
                // 这里可以继续处理closestPath，比如设置到对应的类信息中，找不到就退出
                if(!pomInfos.containsKey(closestPath)){
                    return;
                }
                PomInfo pomInfo = pomInfos.get(closestPath);
                //处理package
                String pkgName = cu.getPackageDeclaration().get().getNameAsString();
                List<PackageInfo> pkgList = pomInfo.getPackageInfoSet().stream()
                        .filter(item->item.getName().equals(pkgName))
                        .toList();
                PackageInfo packageInfo = null;
                if(pkgList!= null && pkgList.size()>0){
                    packageInfo = pkgList.get(0);
                }else{
                    packageInfo = new PackageInfo();
                    packageInfo.setName(pkgName);
                    pomInfo.getPackageInfoSet().add(packageInfo);
                }
                //处理class
                ClassInfo classInfo = null;
                List<ClassInfo> classInfoList = packageInfo.getClassInfoSet().stream()
                        .filter(item->item.getName().equals(cd.getFullyQualifiedName().get()))
                        .toList();
                if(classInfoList!=null && classInfoList.size()>0){
                    classInfo = classInfoList.get(0);
                }else {
                    classInfo = new ClassInfo();
                    classInfo.setName(cd.getFullyQualifiedName().get());

                }
                packageInfo.getClassInfoSet().add(classInfo);
                pomInfo.getClassInfoSet().add(classInfo);



                pomInfo.getMethodInfoSet().addAll(clzAndMethodList);
                packageInfo.getMethodInfoSet().addAll(clzAndMethodList);
                classInfo.getMethodInfoSet().addAll(clzAndMethodList);

                //构建缓存
                reverseIndexOfPackage.put(packageInfo,pomInfo);

                reverseIndexOfClass2Pkg.put(classInfo,packageInfo);
                reverseIndexOfClass2Pom.put(classInfo,pomInfo);

                ClassInfo finalClassInfo = classInfo;
                PackageInfo finalPackageInfo = packageInfo;
                clzAndMethodList.forEach(mtd->{
                    reverseIndexOfMethod2Clz.put(mtd, finalClassInfo);
                    reverseIndexOfMethod2Pkg.put(mtd, finalPackageInfo);
                    reverseIndexOfMethod2Pom.put(mtd, pomInfo);
                });
            }


        }
    }


    /**
     * 初始化分析器，加载所有类和方法
     *
     * @param codeBasePaths Java文件列表
     * @throws IOException IO异常
     */
    public void init(List<String> codeBasePaths) throws IOException {
        if (isClassInit.compareAndSet(false, true)) {

            for(String scanPath:codeBasePaths){
                pomInfos.putAll(codeParser.scanAllPom(scanPath));
            }

            List<File> javaFiles = new ArrayList<>();

            for (String scanPath : codeBasePaths) {
                javaFiles.addAll(codeParser.scanJavaFiles(scanPath));
            }

            // 获取所有文件的父目录路径（去重）
            List<String> parentDirs = javaFiles.stream()
                    .map(file -> file.getParentFile().getAbsolutePath())
                    .distinct()
                    .toList();
            codeParser.init(parentDirs);
            for (File file : javaFiles) {
                CompilationUnit cu = codeParser.parseFile(file);
                //判断属于哪个pom,以及package，以及class

                int complexLevel = CodeMetricsComplexityCalculator.calculateClassComplexity(cu);

                // 遍历所有类和接口
                cu.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration cls, Void arg) {

                        String className = cls.getFullyQualifiedName().orElse("");

                        classMap.put(className, cls);
                        classComplexMap.put(className, complexLevel);
                        List<ClzAndMethod> tmpClzList = new ArrayList<>();
                        // 遍历所有方法
                        for (MethodDeclaration method : cls.getMethods()) {

                            String methodKey = methodKey(className, method.getNameAsString());
                            int methodComplexLevel = CodeMetricsComplexityCalculator.calculateMethodComplexity(method);

                            ClzAndMethod clzAndMethod =new ClzAndMethod(methodKey, cls, method, (long) methodComplexLevel);
                            tmpClzList.add(clzAndMethod);
                            methodMap.put(methodKey, clzAndMethod);
                            methodComplexMap.put(methodKey, methodComplexLevel);


                        }
                        processPomAndClass(cu,cls,tmpClzList);
                        super.visit(cls, arg);
                    }
                }, null);
            }
            if (log.isDebugEnabled()) {
                classComplexMap.forEach((k, v) -> {
                    log.debug("class:{},comples:{}", k, v);
                });
                methodComplexMap.forEach((k, v) -> {
                    log.debug("method:{},complex:{}", k, v);
                });
            }
        }


    }

    /**
     * 获取被调用函数所在的类
     *
     * @param serviceClass
     * @param serviceMethodCall
     * @return
     */
    private String getCalleeClassName(ClassOrInterfaceDeclaration serviceClass, MethodCallExpr serviceMethodCall) {
        if (serviceMethodCall.getScope().isPresent() && serviceMethodCall.getScope().get() instanceof NameExpr serviceScope) {
            String callerFieldName = serviceScope.getNameAsString();
            return getFieldType(serviceClass, callerFieldName);

        } else {
            return serviceClass.getFullyQualifiedName().get();
        }


    }

    /**
     * 递归调用，把所有的调用链都扒出来。
     * 约束：1. 必须在某个包的范围内
     * 约束：2. 如果已经找到mapper就返回
     * 约束：3. 没有找到更多的chain
     * 每增加一层，就要对上一层的
     *
     * @param chain
     * @return
     */
    public List<CallChain> buildCallChainRecycle(CallChain chain, Set<CallChain> flatCallChain) {
        // 查找Service方法调用的Mapper方法
        String serviceClassName = chain.getClassName();
        String methodName = chain.getMethodName();
        String serviceMethodKey = methodKey(serviceClassName, methodName);
        ClassOrInterfaceDeclaration serviceClass = classMap.get(serviceClassName);

        // 找不到对应的类，返回空列表而不是null
        if (serviceClass == null) {
            log.debug("找不到类: {}", serviceClassName);
            return chain.getCallChainList();
        }
        int clsType = getClassType(serviceClass);

        boolean isMapper = CallChainConst.MAPPER == clsType;
        //是mapper
        if (isMapper) {
            chain.setCallType(getClassType(serviceClass));
            String sqlContent = findMapperXmlSql(serviceClassName, methodName);
            chain.setSqlContent(sqlContent);
            return chain.getCallChainList(); // 返回空列表而不是null
        }

        MethodDeclaration serviceMethod = Optional.ofNullable(methodMap.get(serviceMethodKey))
                .orElse(new ClzAndMethod(serviceMethodKey, null, null, 0L))
                .getMethodDeclaration();
        //找不到对应的方法，返回空列表而不是null
        if (serviceMethod == null) {
            try {
                throw new BusinessException("can not find the method " + serviceMethodKey);
            } catch (BusinessException be) {
                log.debug("找不到方法: {}", serviceMethodKey, be);
            } finally {
                return chain.getCallChainList();
            }
        }
        //处理递归调用问题，循环调用问题
        if (methodInvokeTag.contains(serviceMethodKey)) {
            return chain.getCallChainList();
        }
        methodInvokeTag.add(serviceMethodKey);
        List<MethodCallExpr> serviceMethodCalls = codeParser.getMethodCalls(serviceMethod);
        log.info("方法 {} 中有 {} 个方法调用", serviceMethodKey, serviceMethodCalls.size());

        for (MethodCallExpr serviceMethodCall : serviceMethodCalls) {
            {
                String callerMethodName = serviceMethodCall.getNameAsString();

                String callerClassName = getCalleeClassName(serviceClass, serviceMethodCall);
                if (callerClassName == null || callerClassName.isEmpty()) {
                    log.debug("无法解析字段类型: {} 在类 {}", callerMethodName, serviceClassName);
                    continue;
                }

                // 检查被调用的类是否存在
                ClassOrInterfaceDeclaration callerServiceClass = classMap.get(callerClassName);
                if (callerServiceClass == null) {
                    log.debug("找不到被调用类: {}", callerClassName);
                    continue;
                }
                log.debug("callClass:{}, callMethod:{}", callerClassName, callerMethodName);
                if (methodKey(callerClassName, callerMethodName).equals(serviceMethodKey)) {
                    //处理递归调用问题
                    continue;
                }
                CallChain callerChain = new CallChain();
                callerChain.setId(methodKey(callerClassName, callerMethodName));
                callerChain.setEndpointId(methodKey(callerClassName, callerMethodName));
                callerChain.setLevel(chain.getLevel() + 1);

                // 判断被调用的是Service还是Mapper
                int clzType = getClassType(callerServiceClass);
                boolean isCallerMapper = CallChainConst.MAPPER == clzType;
                callerChain.setCallType(clzType);
                callerChain.setClassName(callerClassName);
                callerChain.setMethodName(callerMethodName);
                callerChain.setDescription(isCallerMapper ? "Mapper方法" : "Service方法");
                callerChain.setClassComplexScore(Long.valueOf(Optional.ofNullable(classComplexMap.get(callerClassName)).orElse(0)));
                callerChain.setMethodComplexScore(Long.valueOf(Optional.ofNullable(methodComplexMap.get(methodKey(callerClassName, callerMethodName))).orElse(0)));
                chain.getCallChainList().add(callerChain);
                if (isCallerMapper) {
                    String sqlContent = findMapperXmlSql(callerClassName, callerMethodName);
                    callerChain.setSqlContent(sqlContent);
                }
                // 深度拷贝 CallChain 对象后再添加到 flatCallChain
                CallChain flatCallChainItem = deepCopyCallChain(callerChain);
                flatCallChain.add(flatCallChainItem);

                // 递归调用，但如果是Mapper就不再继续递归
                if (!isCallerMapper) {
                    List<CallChain> subCallChains = buildCallChainRecycle(callerChain, flatCallChain);
                    if (subCallChains != null && !subCallChains.isEmpty()) {
                        callerChain.getCallChainList().addAll(subCallChains);
                        // 深度拷贝子调用链后再添加到 flatCallChain
                        List<CallChain> flatSubCallChains = subCallChains.stream()
                                .map(this::deepCopyCallChain)
                                .collect(Collectors.toList());
                        flatCallChain.addAll(flatSubCallChains);
                    }
                }
            }
        }
        return chain.getCallChainList();
    }

    /**
     * 判断这个类的类型
     *
     * @param clazz
     * @return
     */
    public int getClassType(@NotNull ClassOrInterfaceDeclaration clazz) {
        // 使用常量定义注解名称
        final String REST_CONTROLLER = "RestController";
        final String CONTROLLER = "Controller";
        final String MAPPER = "Mapper";
        final String SERVICE = "Service";
        final String COMPONENT = "Component";
        final String REPOSITORY = "Repository";

        // 检查Controller
        boolean isController = clazz.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals(REST_CONTROLLER)
                        || annotation.getNameAsString().equals(CONTROLLER));
        if (isController) {
            return CallChainConst.CONTROLLER;
        }
        // 检查Service（包括@Component和@Repository）
        boolean isService = clazz.getAnnotations().stream()
                .anyMatch(anno -> anno.getNameAsString().equals(SERVICE)
                        || anno.getNameAsString().equals(COMPONENT)
                        || anno.getNameAsString().equals(REPOSITORY));
        if (isService) {
            return CallChainConst.SERVICE;
        }
        // 1. 检查是否有 @Mapper 注解
        boolean hasMapperAnnotation = clazz.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals(MAPPER));

        // 2. 检查是否直接或间接继承了 MyBatis 核心 Mapper 接口
        boolean extendsMyBatisMapper = extendsInterfaceSimple(clazz, TARGET_INTERFACE);

        if (hasMapperAnnotation || extendsMyBatisMapper) {
            return CallChainConst.MAPPER;
        }
        return CallChainConst.OTHER;

    }

    /**
     * 扫描所有的endpoint，并扫描对应的callchain
     *
     * @return
     */
    public List<Endpoint> scanAllEndpoint() {
        List<ClassOrInterfaceDeclaration> clzList = classMap.values().stream().filter(item ->
                getClassType(item) == CallChainConst.CONTROLLER
        ).toList();
        log.info("当前这个目录下，一共有【{}】个controller类", clzList.size());
        List<ClzAndMethod> controllerMethod = methodMap.values().stream().filter(item -> {
            return clzList.stream().anyMatch(finder ->
                    {
                        if (item.getClassOrInterfaceDeclaration() == null) {
                            return false;
                        }
                        return finder.getFullyQualifiedName()
                                .equals(item.getClassOrInterfaceDeclaration().getFullyQualifiedName());
                    }
            );
        }).toList();
        log.info("当前这个目录下，一共有【{}】个controller 方法", controllerMethod.size());
        List<Endpoint> controllers = controllerMethod.stream().map(item -> {
            Endpoint endpoint = new Endpoint();
            endpoint.setId(item.methodKey);
            endpoint.setMethodName(item.getMethodDeclaration().getNameAsString());
            endpoint.setControllerName(item.getClassOrInterfaceDeclaration().getFullyQualifiedName().get());
            return endpoint;
        }).toList();
        controllers.forEach(item -> buildCallChain(item, null));
        return controllers;
    }

    /**
     * 构建接口的调用链
     *
     * @param endpoint 接口信息
     * @return 调用链列表
     */
    public List<CallChain> buildCallChain(Endpoint endpoint, Set<CallChain> flatCallChainVar) {
        List<CallChain> callChains = new ArrayList<>();
        Set<CallChain> innerFlatCallChain = new HashSet<>();
        if (flatCallChainVar != null) {
            innerFlatCallChain = flatCallChainVar;
        }

        // 构建Controller节点
        CallChain controllerChain = new CallChain();
        controllerChain.setEndpointId(endpoint.getId());
        controllerChain.setId(endpoint.getId());
        controllerChain.setLevel(0);
        controllerChain.setCallType(getClassType(classMap.get(endpoint.getControllerName()))); // 0-Controller
        controllerChain.setClassName(endpoint.getControllerName());
        controllerChain.setMethodName(endpoint.getMethodName());
        controllerChain.setDescription("Controller方法");
        controllerChain.setClassComplexScore(Long.valueOf(Optional.ofNullable(classComplexMap.get(endpoint.getControllerName())).orElse(0)));
        controllerChain.setMethodComplexScore(Long.valueOf(Optional.ofNullable(methodComplexMap.get(methodKey(endpoint.getControllerName(), endpoint.getMethodName()))).orElse(0)));
        callChains.add(controllerChain);
        CallChain flatcontrollerChain = deepCopyCallChain(controllerChain);
        innerFlatCallChain.add(flatcontrollerChain);

        // 查找Controller方法调用的Service方法
        String controllerMethodKey = methodKey(endpoint.getControllerName(), endpoint.getMethodName());
        MethodDeclaration controllerMethod = methodMap.get(controllerMethodKey).getMethodDeclaration();

        if (controllerMethod != null) {
            List<MethodCallExpr> methodCalls = codeParser.getMethodCalls(controllerMethod);

            for (MethodCallExpr methodCall : methodCalls) {
                // 简单处理：假设是通过字段调用的方法，如userService.getUser()
                if (methodCall.getScope().isPresent() && methodCall.getScope().get() instanceof NameExpr scope) {
                    String fieldName = scope.getNameAsString();
                    String methodName = methodCall.getNameAsString();

                    // 查找对应的Service类和方法
                    ClassOrInterfaceDeclaration controllerClass = classMap.get(endpoint.getControllerName());
                    if (controllerClass != null) {
                        // 查找字段对应的类型
                        String serviceClassName = getFieldType(controllerClass, fieldName);
                        if (!serviceClassName.isEmpty()) {
                            if (classMap.get(serviceClassName) == null) {
                                log.warn("查找类:{}时，未找到对应的类", serviceClassName);
                                continue;
                            }
                            if (null == methodKey(serviceClassName, methodName)) {
                                log.warn("查找类:{}的{}方法时，未找到对应的方法", serviceClassName, methodName);
                                continue;
                            }
                            // 构建Service节点
                            CallChain serviceChain = new CallChain();
                            serviceChain.setId(methodKey(serviceClassName, methodName));
                            serviceChain.setEndpointId(endpoint.getId());
                            serviceChain.setLevel(1);
                            serviceChain.setCallType(getClassType(classMap.get(serviceClassName))); // 1-Service
                            serviceChain.setClassName(serviceClassName);
                            serviceChain.setMethodName(methodName);
                            serviceChain.setDescription("Service方法");
                            serviceChain.setClassComplexScore(Long.valueOf(Optional.ofNullable(classComplexMap.get(serviceClassName)).orElse(0)));
                            serviceChain.setMethodComplexScore(Long.valueOf(Optional.ofNullable(methodComplexMap.get(methodKey(serviceClassName, methodName))).orElse(0)));
                            controllerChain.getCallChainList().add(serviceChain);
                            // 深度拷贝 CallChain 对象后再添加到 flatCallChain
                            CallChain flatServiceChain = deepCopyCallChain(serviceChain);
                            innerFlatCallChain.add(flatServiceChain);
                            //h核心方法
                            List<CallChain> chains = buildCallChainRecycle(serviceChain, innerFlatCallChain);
                            if (chains != null && !chains.isEmpty()) {
                                serviceChain.getCallChainList().addAll(chains);
                            }
                        }
                    }
                }
            }
        }
        long allMethodComplexScore = 0;
        for (CallChain cc : innerFlatCallChain) {
            allMethodComplexScore += cc.getMethodComplexScore();
        }
        endpoint.setSumAllComplexScore(allMethodComplexScore);
        endpoint.setCallChainList(callChains);
        endpoint.setFlattenCallChain(innerFlatCallChain);
        return callChains;
    }

    /**
     * 获取类中字段的类型
     *
     * @param cls       类声明
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
     *
     * @param cls      类声明
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
                List<String> typeNamelist = classMap.keySet().stream().filter(item -> item.contains(packageName) && item.endsWith(typeName)).toList();

                // 检查类是否存在于通配符导入的包中
                if (!typeNamelist.isEmpty()) {
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
     *
     * @param fullName 全限定名
     * @return 是否为接口
     */
    private boolean isInterface(String fullName) {
        ClassOrInterfaceDeclaration cls = classMap.get(fullName);
        return cls != null && cls.isInterface();
    }

    /**
     * 查找接口的实现类
     *
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

    // ====================== 以下为新增：Mapper 初始化与方法签名采集 ======================

    /**
     * 查找与 Mapper 类对应的 XML 文件中的 SQL 内容
     *
     * @param mapperClassName Mapper 类的全限定名
     * @param methodName      方法名
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
            log.debug("解析mybatis报错:{}.{}", mapperClassName, methodName, e);
        }
        return "";
    }

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
        if (isMapperInit.compareAndSet(false, true)) {
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

            for (int i = 0; i < statements.size(); i++) {
                try {
                    if (statements.get(i) instanceof MappedStatement) {
                        MappedStatement stat = statements.get(i);
                        try {
                            String namespace = stat.getId().substring(0, stat.getId().lastIndexOf('.'));
                            String methodName = stat.getId().substring(stat.getId().lastIndexOf('.') + 1);
                            MappedStatement mappedStatement = mybatisConfiguration.getMappedStatement(stat.getId());
                            String parameterType = mappedStatement.getParameterMap().getType() != null ?
                                    mappedStatement.getParameterMap().getType().getName() : "";

                            // 存入 mybatisXmlMap 和 mapperMethodMap
                            mybatisXmlMap.put(namespace, new File(stat.getResource()));

                            mapperMethodMap.put(processDotKey2SharpKey(stat.getId()), new MapperMethodInfo(namespace, methodName, parameterType));
                        } catch (Exception ex) {
                            log.error("stat:{}报错", stat, ex);
                        }
                    }
                } catch (Exception eex) {
                    log.error("解析mappered statment失败:{}", i, eex);
                }
            }
        }
    }

    /**
     * 使用 MyBatis 工具类验证 XML 是否为合法的 MyBatis Mapper
     *
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

//    /**
//     * 将 parameterType 尽量解析为全限定名：
//     * - 若为已知别名（MyBatis 常见别名），映射为对应 Java 全名；
//     * - 若已是全限定名（包含'.'），原样返回；
//     * - 若为空或未知，返回原值（可能为空字符串）。
//     */
//    private String resolveParameterTypeFQN(String raw) {
//        if (raw == null || raw.isEmpty()) {
//            return "";
//        }
//        String t = raw.trim();
//        if (t.contains(".")) {
//            return t; // 认为已是全限定名
//        }
//        // MyBatis 常见内置别名映射（不区分大小写）
//        String k = t.toLowerCase(Locale.ROOT);
//        Map<String, String> alias = getMyBatisAliasMap();
//        if (alias.containsKey(k)) {
//            return alias.get(k);
//        }
//        // 兜底返回原值（无法解析）
//        return t;
//    }

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

    /**
     * 对外暴露：获取识别到的 MyBatis XML 映射（namespace -> xml文件）
     */
    public Map<String, File> getMybatisXmlMap() {
        return Collections.unmodifiableMap(mybatisXmlMap);
    }

    /**
     * 对外暴露：获取统一方法签名映射（key=namespace.methodId）
     */
    public Map<String, MapperMethodInfo> getMapperMethodMap() {
        return Collections.unmodifiableMap(mapperMethodMap);
    }

    /**
     * 深度拷贝 CallChain 对象
     *
     * @param original 原始 CallChain 对象
     * @return 深度拷贝后的新对象
     */
    private CallChain deepCopyCallChain(CallChain original) {
        if (original == null) {
            return null;
        }

        // 使用 Hutool BeanUtil 进行深度拷贝
        CallChain copy = BeanUtil.copyProperties(original, CallChain.class);

        return copy;
    }

    public void generateAllClasComplex() throws IOException {
        // 生成带时间戳的文件名
        String timestamp = DateUtils.formatDate(new Date(), "yyyy-MM-dd-HH-mm-ss");
        String fileName = "classComplex" + timestamp + ".csv";


        // 构建CSV内容
        StringBuilder csvContent = new StringBuilder();
        // CSV头部
        csvContent.append("group,project,package,className,method,score\n");
        for (String key : methodMap.keySet()) {
            ClzAndMethod clzAndMethod = methodMap.get(key);
            String artifactId = reverseIndexOfMethod2Pom.containsKey(clzAndMethod)?reverseIndexOfMethod2Pom.get(clzAndMethod).getArtifactId():"";
            String groupid = reverseIndexOfMethod2Pom.containsKey(clzAndMethod)?reverseIndexOfMethod2Pom.get(clzAndMethod).getGroupId():"";
            String pkgName =reverseIndexOfMethod2Pkg.containsKey(clzAndMethod)? reverseIndexOfMethod2Pkg.get(clzAndMethod).getName():"";
            csvContent.append(groupid).append(",")
                    .append(artifactId).append(",")
                    .append(pkgName).append(",")
                    .append(clzAndMethod.getClassOrInterfaceDeclaration().getFullyQualifiedName().get()).append(",")
                    .append(clzAndMethod.getMethodKey()).append(",")
                    .append(clzAndMethod.getMethodComplexScore())
                    .append("\n");
        }


        // 写入文件
        java.nio.file.Files.write(
                java.nio.file.Paths.get(fileName),
                csvContent.toString().getBytes(StandardCharsets.UTF_8)
        );

        log.info("CSV文件已生成: {}", fileName);
    }

    /**
     * 生成CSV文件
     *
     * @param controllers 接口列表
     */
    public void generateCsvFile(List<Endpoint> controllers) throws IOException {
        // 生成带时间戳的文件名
        String timestamp = DateUtils.formatDate(new Date(), "yyyy-MM-dd-HH-mm-ss");
        String fileName = "controllers_" + timestamp + ".csv";


        // 构建CSV内容
        StringBuilder csvContent = new StringBuilder();
        // CSV头部
        csvContent.append("ID,className,METHOD,callchain,score\n");

        // 写入数据
        for (Endpoint endpoint : controllers) {
            csvContent.append(endpoint.getId()).append(",")
                    .append(endpoint.getControllerName()).append(",")
                    .append(endpoint.getMethodName()).append(",")
                    .append(endpoint.getFlattenCallChain().size()).append(",")
                    .append(endpoint.getSumAllComplexScore())
                    .append("\n");
        }

        // 写入文件
        java.nio.file.Files.write(
                java.nio.file.Paths.get(fileName),
                csvContent.toString().getBytes(StandardCharsets.UTF_8)
        );

        log.info("CSV文件已生成: {}", fileName);


    }

    @Data
    @AllArgsConstructor
    public static class ClzAndMethod {
        private String methodKey;
        private ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
        private MethodDeclaration methodDeclaration;
        private Long methodComplexScore;
    }

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

        public String getMapperClassFullName() {
            return mapperClassFullName;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getParameterTypeFullName() {
            return parameterTypeFullName;
        }
    }
}
