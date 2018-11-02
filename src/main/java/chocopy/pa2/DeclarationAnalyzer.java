package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassDefType;
import chocopy.common.analysis.types.FunctionDefType;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.astnodes.*;

import java.util.*;
import java.util.function.Function;

/**
 * Analyzes declarations to create a top-level symbol table
 */
public class DeclarationAnalyzer extends AbstractNodeAnalyzer<SymbolType> {

    private SymbolTable<SymbolType> sym = new SymbolTable<>();  // Changes with scope
    private final SymbolTable<SymbolType> globals = sym;        // always global
    private final Errors errors;
    private boolean isInit = false;

    public DeclarationAnalyzer(Errors errors) {
        this.errors = errors;
        ClassDefType foo;
        FunctionDefType bar;
        FunctionDefType init;

        // Setting up predefined classes and functions

        foo = new ClassDefType("none", "object");
        init = new FunctionDefType();
        init.funcName = "__init__";
        init.returnType = ValueType.OBJECT_TYPE;
        init.params = new ArrayList<SymbolType>();
        init.params.add(ValueType.OBJECT_TYPE);
        foo.currentScope = new SymbolTable<>();
        foo.currentScope.put("__init__", init);
        sym.put("object", foo);

        foo = new ClassDefType("none", "str");
        init = new FunctionDefType();
        init.funcName = "__init__";
        init.returnType = ValueType.OBJECT_TYPE;
        init.params = new ArrayList<SymbolType>();
        init.params.add(ValueType.STR_TYPE);
        foo.currentScope = new SymbolTable<>();
        foo.currentScope.put("__init__", init);
        sym.put("str", foo);

        foo = new ClassDefType("none", "int");
        init = new FunctionDefType();
        init.funcName = "__init__";
        init.returnType = ValueType.INT_TYPE;
        init.params = new ArrayList<SymbolType>();
        init.params.add(ValueType.STR_TYPE);
        foo.currentScope = new SymbolTable<>();
        foo.currentScope.put("__init__", init);
        sym.put("int", foo);

        foo = new ClassDefType("none", "bool");
        init = new FunctionDefType();
        init.funcName = "__init__";
        init.returnType = ValueType.BOOL_TYPE;
        init.params = new ArrayList<SymbolType>();
        init.params.add(ValueType.STR_TYPE);
        foo.currentScope = new SymbolTable<>();
        foo.currentScope.put("__init__", init);
        sym.put("bool", foo);

        bar = new FunctionDefType();
        bar.funcName = "len";
        bar.returnType = ValueType.INT_TYPE;
        bar.params = new ArrayList<SymbolType>();
        bar.params.add(ValueType.OBJECT_TYPE);
        sym.put("len", bar);

        bar = new FunctionDefType();
        bar.funcName = "print";
        bar.returnType = ValueType.OBJECT_TYPE;
        bar.params = new ArrayList<SymbolType>();
        bar.params.add(ValueType.OBJECT_TYPE);
        sym.put("print", bar);

        bar = new FunctionDefType();
        bar.funcName = "input";
        bar.returnType = ValueType.STR_TYPE;
        bar.params = new ArrayList<SymbolType>();
        sym.put("input", bar);
    }


    public SymbolTable<SymbolType> getGlobals() {
        return sym;
    }

    public SymbolType analyze(Program program) {

        // Process all global declarations recursively
        for (Declaration decl : program.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;

            // Process declaration
            SymbolType type = decl.dispatch(this);

            // Skip if error
            if (type == null) {
                continue;
            }

            // Add declaration to symbol table if it is new
            if (sym.get(name) != null) {
                errors.add(new SemanticError(id, "Duplicate declaration of identifier in same scope: " + name));
            } else {
                sym.put(name, type);
            }
        }

        return null;
    }

    @Override
    public SymbolType analyze(VarDef varDef) {
        return ValueType.annotationToValueType(varDef.var.type);
    }

    private boolean compareFunc(FunctionDefType f1, FunctionDefType f2) {
        List<SymbolType> a = f1.params;
        List<SymbolType> b = f2.params;
        if (a.size() != b.size())
            return false;
        for (int i=1; i<a.size(); i++) {
            if (!a.equals(b))
                return false;
        }
        if (!f1.returnType.equals(f2.returnType))
            return false;
        return true;
    }

    @Override
    public SymbolType analyze(ClassDef classdef) {
        // Initializing new ClassDefType
        ClassDefType current = new ClassDefType(classdef.superClass.name, classdef.getIdentifier().name);
        current.currentScope = new SymbolTable<>(sym);
        sym = current.currentScope;
        SymbolType superclass = sym.get(current.superclass);
        SymbolTable<SymbolType> superScope = null;
        Set<String> superDefs = null;

        // Some checking on invalid superclasses
        if (superclass == null) {
            errors.add(new SemanticError(classdef.superClass, "Super-class not defined: " + current.superclass));
            sym = sym.getParent();
            return current;
        }
        if (!(superclass instanceof ClassDefType)) {
            errors.add(new SemanticError(classdef.superClass, "Super-class must be a class: " + current.superclass));
            sym = sym.getParent();
            return current;
        }
        if ( ((ClassDefType) superclass).className.equals("int") || ((ClassDefType) superclass).className.equals("str") || ((ClassDefType) superclass).className.equals("bool") ) {
            errors.add(new SemanticError(classdef.superClass, "Cannot extend special class: " + current.superclass));
            sym = sym.getParent();
            return current;

        }
        superScope = ((ClassDefType) superclass).currentScope;
        superDefs = superScope.getDeclaredSymbols();

        // Recursively process declarations
        for (Declaration decl : classdef.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;

            // Check duplicate
            if (sym.declares(name)) {
                errors.add(new SemanticError(id, "Duplicate declaration of identifier in same scope: " + name));
                continue;
            }

            SymbolType type = decl.dispatch(this);
            // If nonlocal or global fails, don't put in
            if (type == null)
                continue;

            // If it's an attribute
            if (type instanceof ClassValueType || type instanceof ListValueType) {
                // If the id has already been declared in superclass
                if (superDefs.contains(name)) {
                    errors.add(new SemanticError(id, "Cannot re-define attribute: " + name));
                } else {
                    sym.put(name, type);
                }
            }

            // If it's a function definition
            else {
                assert (type instanceof FunctionDefType);
                // Check if first argument is of type class
                List<SymbolType> params = ((FunctionDefType) type).params;
                if (params.size() < 1 || !(params.get(0) instanceof ClassValueType) || !((ClassValueType)params.get(0)).className.equals(current.className))
                    errors.add(new SemanticError(id, "First parameter of the following method must be of the enclosing class: " + name));

                // If the id has already been declared in superclass
                if (superDefs.contains(name)) {
                    // If the declared id is not a function def
                    if (!(superScope.get(id.name) instanceof FunctionDefType)) {
                        errors.add(new SemanticError(id, "Cannot re-define attribute: " + name));
                    }
                    // If the declared id is a function def
                    else {
                        FunctionDefType superFunc = (FunctionDefType) superScope.get(id.name);
                        FunctionDefType myFunc = (FunctionDefType) type;
                        if (compareFunc(superFunc, myFunc)) {
                            sym.put(name, type);
                        } else
                            errors.add(new SemanticError(id, "Method overridden with different type signature: " + name));
                    }
                }
                else
                    sym.put(name, type);
            }
        }
        // Copy over all unoverridden superclass attributes and functions
        for (String superDef : superDefs) {
            // If already declared (overridden), do nothing
            if (sym.getDeclaredSymbols().contains(superDef))
                continue;
            // Otherwise, copy it over
            sym.put(superDef, superScope.get(superDef));
        }

        sym = sym.getParent();
        return current;
    }

    @Override
    public SymbolType analyze(FuncDef funcdef){
        FunctionDefType current = new FunctionDefType();
        current.currentScope = new SymbolTable<>(sym);
        sym = current.currentScope;
        current.params = new ArrayList<SymbolType>();
        // Parse params, put into scope
        for (TypedVar tv : funcdef.params) {
            if (sym.declares(tv.identifier.name)) {
                errors.add(new SemanticError(tv.identifier, "Duplicate declaration of identifier in same scope: " + tv.identifier.name));
                continue;
            }

            SymbolType type = tv.dispatch(this);
            current.params.add(type);
            sym.put(tv.identifier.name, type);
        }
        current.funcName = funcdef.name.name;
        current.returnType = (ValueType) funcdef.returnType.dispatch(this);
        for (Declaration decl : funcdef.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;

            // Check duplicate
            if (sym.declares(name)) {
                errors.add(new SemanticError(id, "Duplicate declaration of identifier in same scope: " + name));
                continue;
            }

            SymbolType type = decl.dispatch(this);

            // If nonlocal or global fails, don't put in
            if (type != null)
                sym.put(name, type);

        }

        // Check for return stmt if func is __init__
        if (funcdef.name.name.equals("__init__")) {
            for (Stmt stmt : funcdef.statements) {
                if (stmt instanceof ReturnStmt)
                    errors.add(new SemanticError(funcdef.name, "Return statements should not appear in method: __init__"));
            }
        }

        sym = sym.getParent();
        return current;
    }


    @Override
    public SymbolType analyze(ReturnStmt returnStmt){
        if (sym.declares("__init__")){
            errors.add(new SemanticError(returnStmt, "Return statements should not appear in method: __init__"));
        }
        return null;
    }

    @Override
    public SymbolType analyze(TypedVar typedvar){
        return ValueType.annotationToValueType(typedvar.type);
    }

    @Override
    public SymbolType analyze(ClassType classType){
        return ValueType.annotationToValueType(classType);
    }

    @Override
    public SymbolType analyze(ListType listType){
        return ValueType.annotationToValueType(listType);
    }

    @Override
    public SymbolType analyze(GlobalDecl globaldecl) {
        if (!globals.declares(globaldecl.variable.name) || !((globals.get(globaldecl.variable.name) instanceof ClassValueType) || (globals.get(globaldecl.variable.name) instanceof ListValueType)) ) {
        errors.add(new SemanticError(globaldecl.variable, "Not a global variable: " + globaldecl.variable.name));
            return null;
        }
        return globals.get(globaldecl.variable.name);
    }

    @Override
    public SymbolType analyze(NonLocalDecl nldecl) {
        if (sym.isNonlocal(nldecl.variable.name)) {
            return sym.get(nldecl.variable.name);
        }
        errors.add(new SemanticError(nldecl.variable, "Not a nonlocal variable: " + nldecl.variable.name));
        return null;
    }

}
