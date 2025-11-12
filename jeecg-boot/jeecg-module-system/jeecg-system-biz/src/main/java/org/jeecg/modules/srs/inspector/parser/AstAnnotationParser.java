package org.jeecg.modules.srs.inspector.parser;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Java注解解析器
 * 用于读取目录下的Java文件并提取Spring注解信息
 */

public class AstAnnotationParser {

    public static Path root = null;

    public static void main(String[] args) {
        // 设置要扫描的目录路径
        String directoryPath = "/path/to/your/java/source/directory";

        // 如果提供了命令行参数，使用第一个参数作为目录路径
        if (args.length > 0) {
            directoryPath = args[0];
        }
        scan(args.length > 0 ? args[0] : ".");
    }

    public static List<ApiInfo> scan(String directoryPath) {

        root = Paths.get(directoryPath);

        AstAnnotationParser parser = new AstAnnotationParser();
        List<ApiInfo> apiInfos = parser.parseDirectory(directoryPath);




        // 同时输出到控制台（保持原有功能）
        parser.printCsvOutput(apiInfos);
        return apiInfos;
    }

    /**
     * 解析指定目录下的所有Java文件
     */
    public List<ApiInfo> parseDirectory(String directoryPath) {
        List<ApiInfo> apiInfos = new ArrayList<>();

        try {
            // 递归扫描目录下的所有.java文件
            List<Path> javaFiles = scanJavaFiles(directoryPath);

            for (Path javaFile : javaFiles) {
                List<ApiInfo> fileApiInfos = parseJavaFile(javaFile);
                apiInfos.addAll(fileApiInfos);
            }

        } catch (IOException e) {
            System.err.println("扫描目录时发生错误: " + e.getMessage());
        }

        return apiInfos;
    }

    /**
     * 递归扫描目录下的所有Java文件
     */
    private List<Path> scanJavaFiles(String directoryPath) throws IOException {
        List<Path> javaFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
        }

        return javaFiles;
    }

    /**
     * 解析单个Java文件
     */
    private List<ApiInfo> parseJavaFile(Path javaFile) {
        List<ApiInfo> apiInfos = new ArrayList<>();
        String filePath = root.toAbsolutePath().relativize(javaFile.toAbsolutePath()).toString();//调用发起方
        try {
            // 使用JavaParser解析文件
            JavaParser javaParser = new JavaParser();
            CompilationUnit cu = javaParser.parse(javaFile).getResult().orElse(null);

            if (cu == null) {
                return apiInfos;
            }

            // 查找所有类声明
            List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);

            for (ClassOrInterfaceDeclaration clazz : classes) {
                // 检查是否包含@RestController注解
                if (!hasRestControllerAnnotation(clazz)) {
                    continue;
                }

                // 提取类级别的@RequestMapping
                String classRequestMapping = extractClassRequestMapping(clazz);

                // 提取所有方法的注解信息
                List<ApiInfo> classApiInfos = extractMethodAnnotations(clazz, classRequestMapping, filePath);
                apiInfos.addAll(classApiInfos);
            }

        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + javaFile + " - " + e.getMessage());
        }

        return apiInfos;
    }

    /**
     * 检查类是否有@RestController注解
     */
    private boolean hasRestControllerAnnotation(ClassOrInterfaceDeclaration clazz) {
        return clazz.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getNameAsString().equals("RestController"));
    }

    /**
     * 提取类级别的@RequestMapping值
     * 如果类上没有@RequestMapping注解，返回空字符串
     */
    private String extractClassRequestMapping(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMappingAnnotation = clazz.getAnnotations().stream()
                .filter(annotation -> annotation.getNameAsString().equals("RequestMapping"))
                .findFirst();

        if (!requestMappingAnnotation.isPresent()) {
            return "";
        }

        return extractPathFromAnnotation(requestMappingAnnotation.get());
    }

    /**
     * 从注解中提取路径值
     */
    private String extractPathFromAnnotation(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
            // 处理 @RequestMapping("path") 格式
            SingleMemberAnnotationExpr singleMemberAnnotation = (SingleMemberAnnotationExpr) annotation;
            if (singleMemberAnnotation.getMemberValue() instanceof StringLiteralExpr) {
                StringLiteralExpr stringLiteral = (StringLiteralExpr) singleMemberAnnotation.getMemberValue();
                return stringLiteral.getValue();
            }
        } else if (annotation instanceof NormalAnnotationExpr) {
            // 处理 @RequestMapping(value = "path") 或 @RequestMapping(path = "path") 格式
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                String name = pair.getNameAsString();
                if ("value".equals(name) || "path".equals(name)) {
                    if (pair.getValue() instanceof StringLiteralExpr) {
                        StringLiteralExpr stringLiteral = (StringLiteralExpr) pair.getValue();
                        return stringLiteral.getValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * 提取方法级别的注解信息
     */
    private List<ApiInfo> extractMethodAnnotations(ClassOrInterfaceDeclaration clazz, String classRequestMapping, String javaFilePath) {
        List<ApiInfo> apiInfos = new ArrayList<>();

        // 支持的映射注解类型
        String[] mappingAnnotations = {"RequestMapping", "PostMapping", "GetMapping", "PutMapping", "DeleteMapping", "PatchMapping"};

        // 获取类中的所有方法
        List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            // 检查方法是否有映射注解
            Optional<AnnotationExpr> mappingAnnotation = method.getAnnotations().stream()
                    .filter(annotation -> {
                        String annotationName = annotation.getNameAsString();
                        for (String mappingType : mappingAnnotations) {
                            if (mappingType.equals(annotationName)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .findFirst();

            if (!mappingAnnotation.isPresent()) {
                continue; // 跳过没有映射注解的方法
            }

            String methodName = method.getNameAsString();

            // 提取映射注解中的路径
            String methodPath = extractPathFromAnnotation(mappingAnnotation.get());

            // 拼接完整路径
            String fullPath = combinePaths(classRequestMapping, methodPath);

            // 检查是否有@Operation注解
            Optional<AnnotationExpr> operationAnnotation = method.getAnnotations().stream()
                    .filter(annotation -> annotation.getNameAsString().equals("Operation"))
                    .findFirst();

            String tags = "";
            String description = "";
            String summary = "";

            if (operationAnnotation.isPresent()) {
                // 提取@Operation中的信息
                tags = extractAttributeFromAnnotation(operationAnnotation.get(), "tags");
                description = extractAttributeFromAnnotation(operationAnnotation.get(), "description");
                summary = extractAttributeFromAnnotation(operationAnnotation.get(), "summary");
            }

            apiInfos.add(new ApiInfo(clazz.getFullyQualifiedName().get(), tags, description, summary, fullPath, javaFilePath, methodName));
        }

        return apiInfos;
    }

    /**
     * 从注解中提取指定属性的值
     */
    private String extractAttributeFromAnnotation(AnnotationExpr annotation, String attributeName) {
        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                if (attributeName.equals(pair.getNameAsString())) {
                    if (pair.getValue() instanceof StringLiteralExpr) {
                        StringLiteralExpr stringLiteral = (StringLiteralExpr) pair.getValue();
                        return stringLiteral.getValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * 从注解内容中提取指定属性的值
     */
    private String extractAttributeValue(String content, String attributeName) {
        // 匹配属性 = "值" 的模式
        Pattern pattern = Pattern.compile(
                attributeName + "\\s*=\\s*[\"']([^\"']*)[\"']",
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * 提取@RequestMapping中的路径值
     */
    private String extractRequestMappingValue(String content) {
        // 首先尝试匹配value属性
        Pattern valuePattern = Pattern.compile(
                "value\\s*=\\s*[\"']([^\"']*)[\"']",
                Pattern.MULTILINE
        );

        Matcher matcher = valuePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 如果没有value属性，尝试匹配直接的字符串值
        Pattern directPattern = Pattern.compile(
                "[\"']([^\"']*)[\"']",
                Pattern.MULTILINE
        );

        matcher = directPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * 拼接类路径和方法路径
     * 如果类路径为空，直接使用方法路径
     */
    private String combinePaths(String classPath, String methodPath) {
        if (classPath == null) classPath = "";
        if (methodPath == null) methodPath = "";

        // 去除首尾空格
        classPath = classPath.trim();
        methodPath = methodPath.trim();

        // 如果类路径为空，直接使用方法路径
        if (classPath.isEmpty()) {
            // 确保方法路径以/开头
            if (!methodPath.startsWith("/") && !methodPath.isEmpty()) {
                methodPath = "/" + methodPath;
            }
            return methodPath;
        }

        // 如果方法路径为空，直接使用类路径
        if (methodPath.isEmpty()) {
            // 确保类路径以/开头
            if (!classPath.startsWith("/")) {
                classPath = "/" + classPath;
            }
            return classPath;
        }

        // 确保路径以/开头
        if (!classPath.startsWith("/")) {
            classPath = "/" + classPath;
        }
        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }

        // 拼接路径，避免重复的/
        String fullPath = classPath + methodPath;
        fullPath = fullPath.replaceAll("/+", "/");

        return fullPath;
    }



    /**
     * 输出CSV格式的结果到控制台
     */
    public void printCsvOutput(List<ApiInfo> apiInfos) {
        // 输出CSV表头
        System.out.println("tags,description,summary,path,javaFilePath,methodName");

        // 输出数据行
        for (ApiInfo apiInfo : apiInfos) {
            String tags = escapeCsvField(apiInfo.getTags());
            String description = escapeCsvField(apiInfo.getDescription());
            String summary = escapeCsvField(apiInfo.getSummary());
            String path = escapeCsvField(apiInfo.getPath());
            String javaFilePath = escapeCsvField(apiInfo.getFilePath());
            String methodName = escapeCsvField(apiInfo.getMethodName());

            System.out.println(tags + "," + description + "," + summary + "," + path + "," + javaFilePath + "," + methodName);
        }
    }

    /**
     * 处理CSV字段中的特殊字符
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // 如果字段包含逗号、双引号或换行符，需要用双引号包围，并转义内部的双引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // 将双引号转义为两个双引号
            field = field.replace("\"", "\"\"");
            // 用双引号包围整个字段
            field = "\"" + field + "\"";
        }

        return field;
    }

    /**
     * API信息数据类
     */
    public static class ApiInfo {
        private final String classFullName;
        private final String tags;
        private final String description;
        private final String summary;
        private final String path;
        private final String filePath;
        private final String methodName;

        public ApiInfo(String classFullName, String tags, String description, String summary, String path, String filePath, String methodName) {
            this.classFullName = classFullName;
            this.tags = tags;
            this.description = description;
            this.summary = summary;
            this.path = path;
            this.filePath = filePath;
            this.methodName = methodName;
        }

        // Getters
        public String getTags() {
            return tags;
        }

        public String getDescription() {
            return description;
        }

        public String getSummary() {
            return summary;
        }

        public String getPath() {
            return path;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getClassFullName() {
            return classFullName;
        }
    }
}