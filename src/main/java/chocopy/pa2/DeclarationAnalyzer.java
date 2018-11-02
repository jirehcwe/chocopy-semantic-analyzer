package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.ClassDefType;
import chocopy.common.analysis.types.FunctionDefType;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.astnodes.*;

import java.util.function.Function;

/**
 * Analyzes declarations to create a top-level symbol table
 */
public class DeclarationAnalyzer extends AbstractNodeAnalyzer<SymbolType> {

    private SymbolTable<SymbolType> sym = new SymbolTable<>();  // Changes with scope
    private final SymbolTable<SymbolType> globals = StudentAnalysis.globalScope;        // always global
    private final Errors errors;

    public DeclarationAnalyzer(Errors errors) {
        this.errors = errors;
    }


    public SymbolTable<SymbolType> getGlobals() {
        return globals;
    }

    public SymbolType analyze(Program program) {

//        for (String string: globals.getDeclaredSymbols()) {
//            System.out.println(string);
//        }
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
            if (globals.get(name) != null) {
                errors.add(new SemanticError(id, "Duplicate declaration of identifier in same scope: " + name));
            } else {
//                System.out.println("Name:" + name + " type: " + type.toString());
                globals.put(name, type);
            }
        }

        return null;
    }

    @Override
    public SymbolType analyze(VarDef varDef) {
            return ValueType.annotationToValueType(varDef.var.type);
    }

    @Override
    public SymbolType analyze(ClassDef classdef) {
        ClassDefType current = new ClassDefType(classdef.superClass.name, classdef.getIdentifier().name);
        current.currentScope = sym;      //copying outer scope to current symbol table
        for (Declaration decl : classdef.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;
            SymbolType type = decl.dispatch(this);
            if (current.currentScope.get(name) != null) {
                errors.add(new SemanticError(id, "Cannot re-define attribute: " + name));

            }
            else {
                current.currentScope.put(name, type);
            }
        }
        return current;
    }

    @Override
    public SymbolType analyze(FuncDef funcdef){
        FunctionDefType current = new FunctionDefType();
        current.currentScope = sym;
        current.params = funcdef.params;
        current.funcName = funcdef.name.name;
        current.returnType = funcdef.returnType;
        for (Declaration decl : funcdef.declarations) {
            Identifier id = decl.getIdentifier();
            String name = id.name;
            SymbolType type = decl.dispatch(this);
            SymbolType existingSymbol = current.currentScope.get(name);
            if (existingSymbol == null){
                current.currentScope.put(name, type);
                continue;
            }

            //existing symbol exists, but is not a func
            if (!(type instanceof FunctionDefType)) {
                errors.add(new SemanticError(id, "Cannot re-define attribute: " + name));
                System.out.println("existing symbol exists, but is not a func");
                continue;
            }


            FunctionDefType existingFunc = (FunctionDefType)existingSymbol;

            //parameter checking
            for (TypedVar var1: funcdef.params) {
                for (TypedVar var2: existingFunc.params) {
                    if(var1.equals(var2)) { //need to check if var1 is a subclass of var2, not just the actual type
                        continue;
                    }else {
                        errors.add(new SemanticError(id, "Method overridden with different type signature: " + name));

                    }
                }
            }

            if (current.returnType != existingFunc.returnType) {
                errors.add(new SemanticError(id, "Method overridden with different type signature: " + name));
            }




        }
        return current;
    }


    @Override
    public SymbolType analyze(ReturnStmt returnStmt){
        if (sym.declares("__init__")){
            errors.add(new SemanticError(returnStmt, "Return statements should not appear in method: __init__"));
        }
        return null;
    }

}
