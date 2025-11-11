package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

public class MethodComplexityAnalyzer {

    /**
     * 分析类中所有方法的圈复杂度
     */
    public static Map<String, Integer> analyzeAllMethods(CompilationUnit cu) {
        Map<String, Integer> methodComplexities = new HashMap<>();

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            String methodName = method.getNameAsString();
            int complexity = calculateMethodComplexity(method);
            methodComplexities.put(methodName, complexity);
        });

        return methodComplexities;
    }

    /**
     * 计算单个方法的圈复杂度
     */
    public static int calculateMethodComplexity(MethodDeclaration method) {
        ComplexityVisitor visitor = new ComplexityVisitor();
        visitor.visit(method, null);
        return visitor.getComplexity();
    }

    /**
     * 圈复杂度访问者
     */
    private static class ComplexityVisitor extends VoidVisitorAdapter<Void> {
        private int complexity = 1; // 每个方法基础复杂度为1

        public int getComplexity() {
            return complexity;
        }

        // if 语句
        @Override
        public void visit(IfStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // for 循环
        @Override
        public void visit(ForStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // for-each 循环
        @Override
        public void visit(ForEachStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // while 循环
        @Override
        public void visit(WhileStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // do-while 循环
        @Override
        public void visit(DoStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // switch 的每个 case
        @Override
        public void visit(SwitchEntry n, Void arg) {
            // 排除 default，只计算有 label 的 case
            if (!n.getLabels().isEmpty()) {
                complexity++;
            }
            super.visit(n, arg);
        }

        // catch 块
        @Override
        public void visit(CatchClause n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // 三元运算符 ? :
        @Override
        public void visit(ConditionalExpr n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        // 逻辑运算符 && 和 ||
        @Override
        public void visit(BinaryExpr n, Void arg) {
            if (n.getOperator() == BinaryExpr.Operator.AND ||
                    n.getOperator() == BinaryExpr.Operator.OR) {
                complexity++;
            }
            super.visit(n, arg);
        }

        // continue 和 break (可选)
        @Override
        public void visit(ContinueStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }

        @Override
        public void visit(BreakStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
    }
}
