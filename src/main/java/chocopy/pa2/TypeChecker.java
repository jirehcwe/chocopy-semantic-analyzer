package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.BinaryExpr;
import chocopy.common.astnodes.Declaration;
import chocopy.common.astnodes.ExprStmt;
import chocopy.common.astnodes.Identifier;
import chocopy.common.astnodes.IntegerLiteral;
import chocopy.common.astnodes.Node;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.Stmt;

import static chocopy.common.analysis.types.ValueType.INT_TYPE;
import static chocopy.common.analysis.types.ValueType.OBJECT_TYPE;

public class TypeChecker extends AbstractNodeAnalyzer<ValueType> {

    // The current symbol table (changes depending on the function being analyzed)
    private SymbolTable<SymbolType> sym;

    /** Creates a type checker with a given type hierarchy and global symbol table. */
    public TypeChecker(SymbolTable<SymbolType> globalSymbols) {
        this.sym = globalSymbols;
    }

    /** Inserts an error message in a node if there isn't one already */
    private void typeError(Node node, String message) {
        assert message != null;
        if (node.typeError == null) {
            // Only insert the first error at every node
            node.typeError = message;
        }
    }

    @Override
    public ValueType analyze(Program program) {
        for (Declaration decl : program.declarations) {
            decl.dispatch(this);
        }
        for (Stmt stmt : program.statements) {
            stmt.dispatch(this);
        }
        return null;
    }

    @Override
    public ValueType analyze(ExprStmt s) {
        s.expr.dispatch(this);
        return null;
    }

    @Override
    public ValueType analyze(IntegerLiteral i) {
        return (i.inferredType = ValueType.INT_TYPE);
    }

    @Override
    public ValueType analyze(BinaryExpr e) {
        ValueType t1 = e.left.dispatch(this);
        ValueType t2 = e.right.dispatch(this);

        switch (e.operator) {
            case "-":
            case "*":
            case "//":
            case "%":
                if (INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) {
                    return (e.inferredType = INT_TYPE);
                } else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    return (e.inferredType = INT_TYPE);
                }
            default:
                return (e.inferredType = OBJECT_TYPE);
        }

    }



    @Override
    public ValueType analyze(Identifier id) {
        // Get name of variable and its type in the environment
        String varName = id.name;
        SymbolType varType = sym.get(varName);

        // Assign type if possible
        if (varType instanceof ValueType) {
            return (id.inferredType = (ValueType) varType);
        }

        // On error, assume `object` type
        typeError(id, "Not a variable: " + varName);
        return (id.inferredType = ValueType.OBJECT_TYPE);
    }
}
