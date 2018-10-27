package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import  chocopy.common.analysis.types.ListValueType;
import chocopy.common.astnodes.*;

import static chocopy.common.analysis.types.ValueType.INT_TYPE;
import static chocopy.common.analysis.types.ValueType.BOOL_TYPE;
import static chocopy.common.analysis.types.ValueType.STR_TYPE;
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
    public ValueType analyze(BooleanLiteral b) {
        return (b.inferredType = ValueType.BOOL_TYPE);
    }

    @Override
    public ValueType analyze(BinaryExpr e) {
        ValueType t1 = e.left.dispatch(this);
        ValueType t2 = e.right.dispatch(this);

        switch (e.operator) {
            case "+":
                if (INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) {
                    return (e.inferredType = INT_TYPE);
                } else if (STR_TYPE.equals(t1) && STR_TYPE.equals(t2)) {
                    return (e.inferredType = STR_TYPE);
                } else if (t1 instanceof ListValueType && t2 instanceof ListValueType && t1.equals(t2)) {
                    return (e.inferredType = t1); // UGLY WORKAROUND FOR LISTS, FIX LATER?
                } else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    if (INT_TYPE.equals(t1) || INT_TYPE.equals(t2)) {
                        return (e.inferredType = INT_TYPE);
                    } else {
                        return (e.inferredType = OBJECT_TYPE);
                    }
                }
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
            case "==":
            case "!=":
                if (INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) {
                    return (e.inferredType = BOOL_TYPE);
                } else if (BOOL_TYPE.equals(t1) && BOOL_TYPE.equals(t2)) {
                    return (e.inferredType = BOOL_TYPE);
                } else if (STR_TYPE.equals(t1) && STR_TYPE.equals(t2)) {
                    return (e.inferredType = BOOL_TYPE);
                } else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    return (e.inferredType = BOOL_TYPE);
                }
            case "<=":
            case ">=":
            case ">":
            case "<":
                if (INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) {
                    return (e.inferredType = BOOL_TYPE);
                } else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    return (e.inferredType = BOOL_TYPE);
                }
            case "and":
            case "or":
                if (BOOL_TYPE.equals(t1) && BOOL_TYPE.equals(t2)) {
                    return (e.inferredType = BOOL_TYPE);
                } else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    return (e.inferredType = BOOL_TYPE);
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
