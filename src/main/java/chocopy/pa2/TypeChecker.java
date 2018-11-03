package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.analysis.types.FunctionDefType;
import chocopy.common.analysis.types.ClassDefType;
import chocopy.common.astnodes.*;

import static chocopy.common.analysis.types.ValueType.INT_TYPE;
import static chocopy.common.analysis.types.ValueType.BOOL_TYPE;
import static chocopy.common.analysis.types.ValueType.STR_TYPE;
import static chocopy.common.analysis.types.ValueType.OBJECT_TYPE;

public class TypeChecker extends AbstractNodeAnalyzer<ValueType> {

    // The current symbol table (changes depending on the function being analyzed)
    private SymbolTable<SymbolType> sym;
    private SymbolTable<SymbolType> saved;
    private FunctionDefType currFunc = null;
    private FunctionDefType savedFunc = null;

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
    public ValueType analyze(FuncDef fdef) {
        saved = sym;
        sym = ((FunctionDefType) sym.get(fdef.name.name)).currentScope;
        savedFunc = currFunc;
        currFunc = ((FunctionDefType) sym.get(fdef.name.name));
        for (Declaration decl : fdef.declarations) {
            decl.dispatch(this);
        }
        for (Stmt stmt : fdef.statements) {
            stmt.dispatch(this);
        }
        sym = saved;
        currFunc = savedFunc;
        return null;
    }

    @Override
    public ValueType analyze(ClassDef cdef) {
        saved = sym;
        sym = ((ClassDefType) sym.get(cdef.name.name)).currentScope;
        for (Declaration decl : cdef.declarations) {
            decl.dispatch(this);
        }
        sym = saved;
        return null;
    }

    @Override
    public ValueType analyze(ExprStmt s) {
        s.expr.dispatch(this);
        return null;
    }

    @Override
    public ValueType analyze(MemberAssignStmt mas) {
        mas.objectMember.dispatch(this);
        mas.objectMember.inferredType = null;
        mas.value.dispatch(this);
        if (mas.objectMember.typeError != null && mas.objectMember.typeError.substring(0, 28).equals("There is no attribute named ")) {
            mas.typeError = mas.objectMember.typeError;
            mas.objectMember.typeError = null;
        }
        return null;
    }


    @Override
    public ValueType analyze(MemberExpr me) {
        String classType = ((ClassValueType) me.object.dispatch(this)).className;
        saved = sym;
        sym = ((ClassDefType) sym.get(classType)).currentScope;
        me.inferredType = me.member.dispatch(this);
        me.member.inferredType = null;
        sym = saved;
        if (me.member.typeError != null && me.member.typeError.substring(0, 16).equals("Not a variable: ")) {
            me.member.typeError = null;
            typeError(me, String.format("There is no attribute named `%s` in class `%s`", me.member.name, me.object.inferredType));
        }
        return me.inferredType;
    }

    @Override
    public ValueType analyze(ReturnStmt rs) {
        if (rs.value == null) {
            typeError(rs, String.format("Expected type `%s`; got `None`", currFunc.returnType));
            return null;
        }

        ValueType returntype = rs.value.dispatch(this);
        if (!(returntype.equals(currFunc.returnType)))
            if (rs.value instanceof NoneLiteral)
                typeError(rs, String.format("Expected type `%s`; got `None`", currFunc.returnType));
            else
                typeError(rs, String.format("Expected type `%s`; got type `%s`", currFunc.returnType, returntype));
        return null;
    }

    @Override
    public ValueType analyze(MethodCallExpr mce) {
        ClassValueType classtype = (ClassValueType) mce.method.dispatch(this);
        if (!(sym.get(classtype.className) instanceof FunctionDefType)) {
            typeError(mce, String.format("There is no method named `%s` in class `%s`", mce.method.member.name, mce.method.object.inferredType));
            mce.method.inferredType = null;
            mce.method.typeError = null;
            mce.inferredType = OBJECT_TYPE;
            return mce.inferredType;
        }
        FunctionDefType fdt = (FunctionDefType) sym.get(classtype.className);
        //System.out.println(mce.args.size() + "    " + fdt.params.size());
        if (fdt.params.size() != mce.args.size()+1) {
            typeError(mce, String.format("Expected %d arguments; got %d", fdt.params.size()-1, mce.args.size()));
            // analyze args anyway
            for (Expr e : mce.args)
                e.dispatch(this);
            mce.method.inferredType = null;
            mce.inferredType = fdt.returnType;
            return mce.inferredType;
        }
        int index = 1;
        ValueType foo = null;
        // Check args
        for (Expr e : mce.args) {
            //System.out.println(mce.args.size() + "    " + fdt.params.size());
            foo = e.dispatch(this);
            if (!foo.equals(fdt.params.get(index)))
                if (e instanceof NoneLiteral)
                    typeError(mce, String.format("Expected type `%s`; got `None` in parameter %d", fdt.params.get(index), index));
                else
                    typeError(mce, String.format("Expected type `%s`; got type `%s` in parameter %d", fdt.params.get(index), foo, index));
            index++;
        }
        mce.method.inferredType = null;
        mce.inferredType = fdt.returnType;
        return mce.inferredType;
    }


    @Override
    public ValueType analyze(CallExpr ce) {
        SymbolType type = sym.get(ce.function.name);
        if (type instanceof FunctionDefType) {
            FunctionDefType fdtype = ((FunctionDefType) type);
            if (ce.args.size() != fdtype.params.size()) {
                typeError(ce, String.format("Expected %d arguments; got %d", fdtype.params.size(), ce.args.size()));

                // analyze args but do not compare
                for (Expr args : ce.args)
                    args.dispatch(this);

                return (ce.inferredType = fdtype.returnType);
            }


            int index = 0;
            ValueType foo = null;
            for (Expr args : ce.args) {
                foo = args.dispatch(this);
                if (foo == null) {
                    index++;
                    continue; //Why? idunno. Probably unimplemented node analyze
                }
                if (!foo.equals(fdtype.params.get(index)) && !(fdtype.params.get(index).equals(OBJECT_TYPE)))
                    if (args instanceof NoneLiteral)
                        typeError(ce, String.format("Expected type `%s`; got `None` in parameter %d", fdtype.params.get(index), index));
                    else
                        typeError(ce, String.format("Expected type `%s`; got type `%s` in parameter %d", fdtype.params.get(index), foo, index));
                index++;
            }

            return (ce.inferredType = fdtype.returnType);
        }
        else if (type instanceof ClassDefType) {
            ClassDefType cdtype = (ClassDefType) type;

            FunctionDefType fdtype =(FunctionDefType) (cdtype.currentScope.get("__init__"));
            // -1 because of self param
            if (ce.args.size() != fdtype.params.size() - 1) {
                typeError(ce, String.format("Expected %d arguments; got %d", fdtype.params.size()-1, ce.args.size()));

                // analyze args but do not compare
                for (Expr args : ce.args)
                    args.dispatch(this);

                return (ce.inferredType = fdtype.returnType);
            }

            int index = 1;
            ValueType foo = null;
            for (Expr args : ce.args) {
                //System.out.println(mce.args.size() + "    " + fdt.params.size());
                foo = args.dispatch(this);
                //System.out.println(foo + "    " + fdtype.params.get(index));
                if (!foo.equals(fdtype.params.get(index)) && !(fdtype.params.get(index).equals(OBJECT_TYPE)))
                    if (args instanceof NoneLiteral)
                        typeError(ce, String.format("Expected type `%s`; got `None` in parameter %d", fdtype.params.get(index), index));
                    else
                        typeError(ce, String.format("Expected type `%s`; got type `%s` in parameter %d", fdtype.params.get(index), foo, index));
                index++;
            }




            return (ce.inferredType = new ClassValueType(cdtype.className));
        }
        else if (type == null) {
            for (Expr args : ce.args)
                args.dispatch(this);
            typeError(ce, String.format("Not a function or class: %s", ce.function.name));
            return (ce.inferredType = OBJECT_TYPE);

        }
        else {
            return null; // SHOULD NEVER GET HERE
        }
    }
    // For the While, If, and For loops, maybe have checks on condition/iterable?
    @Override
    public ValueType analyze(WhileStmt s) {
        ValueType condType = s.condition.dispatch(this);
        for (Stmt stmt : s.body)
            stmt.dispatch(this);
        return null;
    }

    @Override
    public ValueType analyze(ForStmt s) {
        ValueType condType = s.iterable.dispatch(this);
        for (Stmt stmt : s.body)
            stmt.dispatch(this);
        return null;
    }

    @Override
    public ValueType analyze(IfStmt s) {
        ValueType condType = s.condition.dispatch(this);
        for (Stmt stmt : s.thenBody)
            stmt.dispatch(this);
        for (Stmt stmt : s.elseBody)
            stmt.dispatch(this);
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
    public ValueType analyze(StringLiteral s) {
        return (s.inferredType = ValueType.STR_TYPE);
    }

    @Override
    public ValueType analyze(NoneLiteral n) {
        return (n.inferredType = ValueType.OBJECT_TYPE);
    }

    private boolean isSpecialClass(String foo) {
        return foo.equals("int") || foo.equals("str") || foo.equals("bool");
    }
    @Override
    public ValueType analyze(VarDef vd) {
        ValueType literalType = vd.value.dispatch(this);

        if (vd.var.type instanceof ClassType) {
            // Ugly hack to check if var type is same as literal type.
            if (!((ClassType) (vd.var.type)).className.equals(((ClassValueType) literalType).className))
                if (! (!isSpecialClass(((ClassType) (vd.var.type)).className) && literalType.equals(OBJECT_TYPE)) )
                    // typeError(vd, String.format("Cannot declare variable `%s` of type `%s` to value type `%s`", vd.var.identifier.name,((ClassType) (vd.var.type)).className, literalType));
                    typeError(vd, String.format("Expected type `%s`; got type `%s`", ((ClassType) (vd.var.type)).className, literalType));
        } else {
            if (!literalType.equals(OBJECT_TYPE))
                typeError(vd, String.format("Cannot declare list variable `%s` to value `%s`", vd.var.identifier.name, literalType));
        }
        return null;
    }

    @Override
    public ValueType analyze(UnaryExpr e) {
        ValueType operandType = e.operand.dispatch(this);

        switch(e.operator) {
            case "-":
                if (INT_TYPE.equals(operandType))
                    return (e.inferredType = INT_TYPE);
                else {
                    typeError(e, String.format("Cannot apply operator `%s` on type `%s`", e.operator, operandType));
                    return (e.inferredType = INT_TYPE);
                }
            case "not":
                if (BOOL_TYPE.equals(operandType))
                    return (e.inferredType = BOOL_TYPE);
                else {
                    typeError(e, String.format("Cannot apply operator `%s` on type `%s`", e.operator, operandType));
                    return (e.inferredType = BOOL_TYPE);
                }
            default:
                return (e.inferredType = OBJECT_TYPE);
        }
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
                } else if (t1 instanceof ListValueType && t2 instanceof ListValueType) {
                    if (t1.equals(t2))
                        return (e.inferredType = t1); // UGLY WORKAROUND FOR LISTS, FIX LATER?
                    else
                        // If the lists are of different type, supposed to be superset, but object will always work. Edit later?
                        return (e.inferredType = new ListValueType(OBJECT_TYPE));
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
            case "is":
                if (!(BOOL_TYPE.equals(t1) || BOOL_TYPE.equals(t2) || INT_TYPE.equals(t1) || INT_TYPE.equals(t2) || STR_TYPE.equals(t1) || STR_TYPE.equals(t2)))
                    return (e.inferredType = BOOL_TYPE);
                else {
                    typeError(e, String.format("Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2));
                    return (e.inferredType = BOOL_TYPE);
                }
            default:
                return (e.inferredType = OBJECT_TYPE);
        }

    }

    @Override
    public ValueType analyze(IndexAssignStmt ias) {
        ValueType listType = ias.listElement.list.dispatch(this);
        ValueType indexType = ias.listElement.index.dispatch(this);
        ValueType valueType = ias.value.dispatch(this);
        ValueType elementType = null;

        if (listType instanceof ListValueType)
            elementType = ((ListValueType) listType).elementType;
        else {
            typeError(ias, String.format("`%s` is not a list type", ((ClassValueType) listType).className));
            return null;
        }

        if (!indexType.equals(INT_TYPE)) {
            typeError(ias, String.format("Index is of non-integer type `%s`", indexType));
            return null;
        }

        if (!(elementType.equals(valueType) || OBJECT_TYPE.equals(elementType))) // Temporary, only checks superclass of Object, or if equal
            typeError(ias, String.format("Expected type `%s`; got type `%s`", ((ClassValueType) elementType).className, ((ClassValueType) valueType).className));

        return null;
    }

    @Override
    public ValueType analyze(IndexAssignExpr iae) {
        ValueType listType = iae.listElement.list.dispatch(this);
        ValueType indexType = iae.listElement.index.dispatch(this);
        ValueType valueType = iae.value.dispatch(this);
        ValueType elementType = null;

        if (listType instanceof ListValueType)
            elementType = ((ListValueType) listType).elementType;
        else {
            typeError(iae, String.format("`%s` is not a list type", ((ClassValueType) listType).className));
            return (iae.inferredType = OBJECT_TYPE);
        }

        if (!indexType.equals(INT_TYPE)) {
            typeError(iae, String.format("Index is of non-integer type `%s`", indexType));
            return (iae.inferredType = valueType);
        }

        if (!(elementType.equals(valueType) || OBJECT_TYPE.equals(elementType))) // Temporary, only checks superclass of Object, or if equal
            typeError(iae, String.format("Expected type `%s`; got type `%s`", ((ClassValueType) elementType).className, ((ClassValueType) valueType).className));

        return (iae.inferredType = valueType);
    }

    @Override
    public ValueType analyze(IndexExpr ie) {
        ValueType listType = ie.list.dispatch(this);
        ValueType indexType = ie.index.dispatch(this);

        if (listType instanceof ClassValueType) {
            if (((ClassValueType) listType).className.equals("str")) {
                ie.inferredType = STR_TYPE;
            } else {
                typeError(ie, String.format("Cannot index into type `%s`", ((ClassValueType) listType).className));
                return (ie.inferredType = OBJECT_TYPE);
            }
        } else {
            ie.inferredType = ((ListValueType) listType).elementType;
        }

        if (!INT_TYPE.equals(indexType))
            typeError(ie, String.format("Index is of non-integer type `%s`", ((ClassValueType) indexType).className));
        return ie.inferredType;
    }

    @Override
    public ValueType analyze(VarAssignStmt vas) {
        SymbolType varType = sym.get(vas.var.name);
        ValueType valueType = vas.value.dispatch(this);

        if (valueType == null)
            return null; // Prevents crashing if not implemented yet!

        if (varType == null) {
            typeError(vas, String.format("Not a variable: %s", vas.var.name));
            return null;
        }

        if (varType instanceof ClassValueType) {
            if (!(varType.equals(valueType) || varType.equals(OBJECT_TYPE))) // Temporary, only checks superclass of Object, or if equal
                if (! (!isSpecialClass(((ClassValueType) varType).className ) && valueType.equals(OBJECT_TYPE)) )
                typeError(vas, String.format("Expected type `%s`; got type `%s`", ((ClassValueType) varType).className, ((ClassValueType) valueType).className));
        } else {
            if (!(varType.equals(valueType))) {
                if (!(vas.value instanceof ListExpr && ((ListExpr) vas.value).elements.size() == 0 || vas.value instanceof NoneLiteral))
                    typeError(vas, String.format("Expected type `%s`; got type `%s`", varType, valueType));
            }
        }

        return null;
    }

    @Override
    public ValueType analyze(VarAssignExpr vae) {
        SymbolType varType = sym.get(vae.var.name);
        ValueType valueType = vae.value.dispatch(this);

        if (valueType == null)
            return null; // Prevents crashing if not implemented yet!

        if (varType == null) {
            typeError(vae, String.format("Not a variable: %s", vae.var.name));
            return (vae.inferredType = valueType);
        }

        if (varType instanceof ClassValueType) {
            if (!(varType.equals(valueType) || varType.equals(OBJECT_TYPE) )) // Temporary, only checks superclass of Object, or if equal
                typeError(vae, String.format("Expected type `%s`; got type `%s`", ((ClassValueType) varType).className, ((ClassValueType) valueType).className));
        } else {
            if (!(varType.equals(valueType))) {
                if (!(vae.value instanceof ListExpr && ((ListExpr) vae.value).elements.size() == 0 || vae.value instanceof NoneLiteral))
                    typeError(vae, String.format("Expected type `%s`; got type `%s`", varType, valueType));
            }
        }

        return (vae.inferredType = valueType);
    }

    @Override
    public ValueType analyze(ListExpr le) {
        ValueType curr = null;
        ValueType prev = null;
        boolean first = true;
        for (Expr e : le.elements) {
            if (first) {
                first = false;
                curr = e.dispatch(this);
                prev = curr;
            } else {
                if (!prev.equals(e.dispatch(this)))
                    curr = OBJECT_TYPE; // Again, ugly workaround to default to object type. Maybe fix later.
                prev = e.dispatch(this);
            }
        }
        if (first)
            return (le.inferredType = OBJECT_TYPE); // Default to object if empty list
        return (le.inferredType = new ListValueType(curr));
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
        if (varType instanceof ClassDefType) {
            return id.inferredType = new ClassValueType(((ClassDefType) varType).className);
        }
        if (varType instanceof FunctionDefType) {
            return id.inferredType = new ClassValueType(((FunctionDefType) varType).funcName);
        }

        // On error, assume `object` type
        typeError(id, "Not a variable: " + varName);
        return (id.inferredType = ValueType.OBJECT_TYPE);
    }
}
