package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

/**
 * 基于 Code Metrics 的复杂度计算器
 * 考虑嵌套深度的加权复杂度
 */
public class CodeMetricsComplexityCalculator {

    /**
     * 计算类的总复杂度（所有方法复杂度之和）
     */
    public static int calculateClassComplexity(CompilationUnit cu) {
        int totalComplexity = 0;

        // 遍历所有类
        for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            totalComplexity += calculateClassComplexity(clazz);
        }

        return totalComplexity;
    }

    /**
     * 计算单个类的复杂度
     */
    public static int calculateClassComplexity(ClassOrInterfaceDeclaration clazz) {
        int totalComplexity = 0;

        // 遍历类中所有方法
        for (MethodDeclaration method : clazz.findAll(MethodDeclaration.class)) {
            totalComplexity += calculateMethodComplexity(method);
        }

        return totalComplexity;
    }

    /**
     * 计算方法的加权复杂度（考虑嵌套深度）
     */
    public static int calculateMethodComplexity(MethodDeclaration method) {
        WeightedComplexityVisitor visitor = new WeightedComplexityVisitor();
        visitor.visit(method, 0); // 初始嵌套深度为0
        return visitor.getTotalComplexity();
    }

    /**
     * 加权复杂度访问者
     * 每个决策点的复杂度 = 1 + 嵌套深度
     */
    private static class WeightedComplexityVisitor extends VoidVisitorAdapter<Integer> {
        private int totalComplexity = 0;

        public int getTotalComplexity() {
            return totalComplexity;
        }

        /**
         * 添加复杂度点
         * @param nestingDepth 当前嵌套深度
         */
        private void addComplexity(int nestingDepth) {
            totalComplexity += (1 + nestingDepth);
        }

        // if 语句
        @Override
        public void visit(IfStmt n, Integer depth) {
            addComplexity(depth);

            // 访问 then 分支
            if (n.getThenStmt() != null) {
                n.getThenStmt().accept(this, depth + 1);
            }

            // 访问 else 分支
            if (n.getElseStmt().isPresent()) {
                Statement elseStmt = n.getElseStmt().get();
                // 如果是 else if，不增加嵌套深度
                if (elseStmt instanceof IfStmt) {
                    elseStmt.accept(this, depth);
                } else {
                    elseStmt.accept(this, depth + 1);
                }
            }

            // 访问条件表达式
            n.getCondition().accept(this, depth);
        }

        // for 循环
        @Override
        public void visit(ForStmt n, Integer depth) {
            addComplexity(depth);

            // 访问循环体
            if (n.getBody() != null) {
                n.getBody().accept(this, depth + 1);
            }

            // 访问初始化、比较和更新表达式
            n.getInitialization().forEach(expr -> expr.accept(this, depth));
            n.getCompare().ifPresent(expr -> expr.accept(this, depth));
            n.getUpdate().forEach(expr -> expr.accept(this, depth));
        }

        // for-each 循环
        @Override
        public void visit(ForEachStmt n, Integer depth) {
            addComplexity(depth);

            if (n.getBody() != null) {
                n.getBody().accept(this, depth + 1);
            }

            n.getIterable().accept(this, depth);
        }

        // while 循环
        @Override
        public void visit(WhileStmt n, Integer depth) {
            addComplexity(depth);

            if (n.getBody() != null) {
                n.getBody().accept(this, depth + 1);
            }

            n.getCondition().accept(this, depth);
        }

        // do-while 循环
        @Override
        public void visit(DoStmt n, Integer depth) {
            addComplexity(depth);

            if (n.getBody() != null) {
                n.getBody().accept(this, depth + 1);
            }

            n.getCondition().accept(this, depth);
        }

        // switch 语句
        @Override
        public void visit(SwitchStmt n, Integer depth) {
            // switch 本身不增加复杂度

            // 访问每个 case
            for (SwitchEntry entry : n.getEntries()) {
                entry.accept(this, depth);
            }

            n.getSelector().accept(this, depth);
        }

        // switch 的每个 case
        @Override
        public void visit(SwitchEntry n, Integer depth) {
            // 每个 case 增加复杂度（除了 default）
            if (!n.getLabels().isEmpty()) {
                addComplexity(depth);
            }

            // 访问 case 语句体
            for (Statement stmt : n.getStatements()) {
                stmt.accept(this, depth + 1);
            }
        }

        // try-catch 语句
        @Override
        public void visit(TryStmt n, Integer depth) {
            // try 块本身不增加复杂度
            if (n.getTryBlock() != null) {
                n.getTryBlock().accept(this, depth);
            }

            // 每个 catch 增加复杂度
            for (CatchClause catchClause : n.getCatchClauses()) {
                catchClause.accept(this, depth);
            }

            // finally 块不增加复杂度
            n.getFinallyBlock().ifPresent(block -> block.accept(this, depth));
        }

        // catch 块
        @Override
        public void visit(CatchClause n, Integer depth) {
            addComplexity(depth);

            if (n.getBody() != null) {
                n.getBody().accept(this, depth + 1);
            }
        }

        // 三元运算符
        @Override
        public void visit(ConditionalExpr n, Integer depth) {
            addComplexity(depth);

            n.getCondition().accept(this, depth);
            n.getThenExpr().accept(this, depth + 1);
            n.getElseExpr().accept(this, depth + 1);
        }

        // 逻辑运算符 && 和 ||
        @Override
        public void visit(BinaryExpr n, Integer depth) {
            if (n.getOperator() == BinaryExpr.Operator.AND ||
                    n.getOperator() == BinaryExpr.Operator.OR) {
                addComplexity(depth);
            }

            n.getLeft().accept(this, depth);
            n.getRight().accept(this, depth);
        }

        // continue 语句
        @Override
        public void visit(ContinueStmt n, Integer depth) {
            addComplexity(depth);
            super.visit(n, depth);
        }

        // break 语句
        @Override
        public void visit(BreakStmt n, Integer depth) {
            addComplexity(depth);
            super.visit(n, depth);
        }

        // return 语句（在某些度量标准中计算）
        @Override
        public void visit(ReturnStmt n, Integer depth) {
            // 某些实现会为 return 增加复杂度
            // addComplexity(depth);
            super.visit(n, depth);
        }

        // throw 语句
        @Override
        public void visit(ThrowStmt n, Integer depth) {
            addComplexity(depth);
            super.visit(n, depth);
        }

        // 处理块语句，但不改变深度
        @Override
        public void visit(BlockStmt n, Integer depth) {
            for (Statement stmt : n.getStatements()) {
                stmt.accept(this, depth);
            }
        }
    }
}
