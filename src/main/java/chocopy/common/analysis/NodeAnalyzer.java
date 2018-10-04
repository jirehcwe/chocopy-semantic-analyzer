package chocopy.common.analysis;

import chocopy.common.astnodes.*;

/**
 * This interface can be used to separate logic for
 * various concrete classes in the AST class hierarchy.
 *
 * @param <T> the type of analysis result
 */
public interface NodeAnalyzer<T> {

    T analyze(BinaryExpr node);
    T analyze(BooleanLiteral node);
    T analyze(CallExpr node);
    T analyze(ClassDef node);
    T analyze(ClassType node);
    T analyze(Errors node);
    T analyze(ExprStmt node);
    T analyze(ForStmt node);
    T analyze(FuncDef node);
    T analyze(GlobalDecl node);
    T analyze(Identifier node);
    T analyze(IfStmt node);
    T analyze(IndexAssignExpr node);
    T analyze(IndexAssignStmt node);
    T analyze(IndexExpr node);
    T analyze(IntegerLiteral node);
    T analyze(ListExpr node);
    T analyze(ListType node);
    T analyze(MemberAssignExpr node);
    T analyze(MemberAssignStmt node);
    T analyze(MemberExpr node);
    T analyze(MethodCallExpr node);
    T analyze(NoneLiteral node);
    T analyze(NonLocalDecl node);
    T analyze(PassStmt node);
    T analyze(Program node);
    T analyze(ReturnStmt node);
    T analyze(SemanticError node);
    T analyze(StringLiteral node);
    T analyze(SyntaxError node);
    T analyze(TypedVar node);
    T analyze(UnaryExpr node);
    T analyze(VarAssignExpr node);
    T analyze(VarAssignStmt node);
    T analyze(VarDef node);
    T analyze(WhileStmt node);


}
