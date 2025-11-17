package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 代码解析器
 *
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-01
 */
@Component
public class CodeParser {

    private TypeSolver typeSolver;

    /**
     * 初始化解析器
     *
     * @param sourcePaths 源代码路径列表
     */
    public void init(List<String> sourcePaths) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // 添加源代码路径
        for (String path : sourcePaths) {
            combinedTypeSolver.add(new JavaParserTypeSolver(new File(path)));
        }

        typeSolver = combinedTypeSolver;
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }

    /**
     * 扫描指定目录下的所有Java文件
     *
     * @param scanPath 扫描路径
     * @return Java文件列表
     * @throws IOException IO异常
     */
    public List<File> scanJavaFiles(String scanPath) throws IOException {
        List<File> javaFiles = new ArrayList<>();
        Path path = Paths.get(scanPath);

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("test"))
                    .forEach(p -> javaFiles.add(p.toFile()));
        }

        return javaFiles;
    }

    /**
     * 解析Java文件
     *
     * @param file Java文件
     * @return 编译单元
     * @throws IOException IO异常
     */
    public CompilationUnit parseFile(File file) throws IOException {
        return StaticJavaParser.parse(file);
    }

    /**
     * 检查类是否是Controller
     *
     * @param cls 类声明
     * @return 是否是Controller
     */
    public boolean isController(ClassOrInterfaceDeclaration cls) {
        return hasAnnotation(cls, "RestController") || hasAnnotation(cls, "Controller");
    }

    /**
     * 检查方法是否有RequestMapping注解
     *
     * @param method 方法声明
     * @return 是否有RequestMapping注解
     */
    public boolean hasRequestMapping(MethodDeclaration method) {
        return hasAnnotation(method, "RequestMapping") ||
                hasAnnotation(method, "GetMapping") ||
                hasAnnotation(method, "PostMapping") ||
                hasAnnotation(method, "PutMapping") ||
                hasAnnotation(method, "DeleteMapping") ||
                hasAnnotation(method, "PatchMapping");
    }

    /**
     * 检查元素是否有指定注解
     *
     * @param element        元素
     * @param annotationName 注解名称
     * @return 是否有指定注解
     */
    private boolean hasAnnotation(Object element, String annotationName) {
        if (element instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration) element).getAnnotations().stream()
                    .anyMatch(anno -> anno.getNameAsString().equals(annotationName));
        } else if (element instanceof MethodDeclaration) {
            return ((MethodDeclaration) element).getAnnotations().stream()
                    .anyMatch(anno -> anno.getNameAsString().equals(annotationName));
        }
        return false;
    }

    /**
     * 获取类的RequestMapping路径
     *
     * @param cls 类声明
     * @return 路径
     */
    public String getClassRequestMapping(ClassOrInterfaceDeclaration cls) {
        return getRequestMappingPath(cls);
    }

    /**
     * 获取方法的RequestMapping路径
     *
     * @param method 方法声明
     * @return 路径
     */
    public String getMethodRequestMapping(MethodDeclaration method) {
        return getRequestMappingPath(method);
    }

    /**
     * 获取RequestMapping路径
     *
     * @param element 元素
     * @return 路径
     */
    private String getRequestMappingPath(Object element) {
        List<AnnotationExpr> annotations = new ArrayList<>();

        if (element instanceof ClassOrInterfaceDeclaration) {
            annotations.addAll(((ClassOrInterfaceDeclaration) element).getAnnotations());
        } else if (element instanceof MethodDeclaration) {
            annotations.addAll(((MethodDeclaration) element).getAnnotations());
        }

        for (AnnotationExpr anno : annotations) {
            String annoName = anno.getNameAsString();
            if (annoName.equals("RequestMapping") ||
                    annoName.equals("GetMapping") ||
                    annoName.equals("PostMapping") ||
                    annoName.equals("PutMapping") ||
                    annoName.equals("DeleteMapping") ||
                    annoName.equals("PatchMapping")) {

                // 简单处理：获取第一个value属性值
                return anno.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
            }
        }

        return "";
    }

    /**
     * 获取HTTP方法
     *
     * @param method 方法声明
     * @return HTTP方法
     */
    public String getHttpMethod(MethodDeclaration method) {
        if (hasAnnotation(method, "GetMapping")) {
            return "GET";
        } else if (hasAnnotation(method, "PostMapping")) {
            return "POST";
        } else if (hasAnnotation(method, "PutMapping")) {
            return "PUT";
        } else if (hasAnnotation(method, "DeleteMapping")) {
            return "DELETE";
        } else if (hasAnnotation(method, "PatchMapping")) {
            return "PATCH";
        } else if (hasAnnotation(method, "RequestMapping")) {
            // 默认GET
            return "GET";
        }
        return "";
    }

    /**
     * 检查方法是否有@Operation注解
     *
     * @param method 方法声明
     * @return 是否有@Operation注解
     */
    public boolean hasOperationAnnotation(MethodDeclaration method) {
        return hasAnnotation(method, "Operation");
    }

    /**
     * 获取方法体中的所有方法调用
     *
     * @param method 方法声明
     * @return 方法调用列表
     */
    public List<MethodCallExpr> getMethodCalls(MethodDeclaration method) {
        List<MethodCallExpr> methodCalls = new ArrayList<>();

        if (method.getBody().isPresent()) {
            BlockStmt body = method.getBody().get();
            body.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodCallExpr n, Void arg) {

                    methodCalls.add(n);
                    super.visit(n, arg);
                }
            }, null);
        }

        return methodCalls;
    }

    /**
     * 检查类是否是Service
     *
     * @param cls 类声明
     * @return 是否是Service
     */
    public boolean isService(ClassOrInterfaceDeclaration cls) {
        return hasAnnotation(cls, "Service");
    }

    /**
     * 检查类是否是Mapper
     *
     * @param cls 类声明
     * @return 是否是Mapper
     */
    public boolean isMapper(ClassOrInterfaceDeclaration cls) {
        return hasAnnotation(cls, "Mapper") || hasAnnotation(cls, "Repository");
    }
}
