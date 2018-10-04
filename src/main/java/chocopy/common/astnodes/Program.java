package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Program extends Node {

    public final List<Declaration> declarations;
    public final List<Stmt> statements;

    public Program(Location left, Location right, List<Declaration> declarations, List<Stmt> statements) {
        super(left, right);
        this.declarations = declarations;
        this.statements = statements;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
