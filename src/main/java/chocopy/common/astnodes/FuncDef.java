package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class FuncDef extends Declaration {

    public final Identifier name;
    public final List<TypedVar> params;
    public final TypeAnnotation returnType;
    public final List<Declaration> declarations;
    public final List<Stmt> statements;

    public FuncDef(Location left, Location right, Identifier name, List<TypedVar> params, TypeAnnotation returnType, List<Declaration> declarations, List<Stmt> statements) {
        super(left, right);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.declarations = declarations;
        this.statements = statements;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public Identifier getIdentifier() {
        return this.name;
    }
}
