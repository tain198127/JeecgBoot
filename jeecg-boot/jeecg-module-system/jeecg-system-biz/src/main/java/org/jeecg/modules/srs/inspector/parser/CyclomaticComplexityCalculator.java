package org.jeecg.modules.srs.inspector.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CyclomaticComplexityCalculator extends VoidVisitorAdapter<Void> {

    private int complexity = 1; // 基础复杂度为1

    public static int calculateComplexity(CompilationUnit cu) {
        CyclomaticComplexityCalculator calculator = new CyclomaticComplexityCalculator();
        calculator.visit(cu, null);
        return calculator.complexity;
    }

    @Override
    public void visit(IfStmt n, Void arg) {
        complexity++; // if语句增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        complexity++; // for循环增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        complexity++; // for-each循环增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        complexity++; // while循环增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        complexity++; // do-while循环增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchEntry n, Void arg) {
        if (!n.getLabels().isEmpty()) {
            complexity++; // 每个case增加复杂度
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        complexity++; // catch块增加复杂度
        super.visit(n, arg);
    }

    @Override
    public void visit(ConditionalExpr n, Void arg) {
        complexity++; // 三元运算符增加复杂度
        super.visit(n, arg);
    }
}
