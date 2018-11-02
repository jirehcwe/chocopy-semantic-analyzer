package chocopy.pa2;

import chocopy.common.analysis.AbstractNodeAnalyzer;
import chocopy.common.analysis.SymbolTable;
import chocopy.common.analysis.types.SymbolType;
import chocopy.common.analysis.types.ValueType;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.analysis.types.ClassValueType;
import chocopy.common.astnodes.*;
import proguard.evaluation.value.Value;

import static chocopy.common.analysis.types.ValueType.INT_TYPE;
import static chocopy.common.analysis.types.ValueType.BOOL_TYPE;
import static chocopy.common.analysis.types.ValueType.STR_TYPE;
import static chocopy.common.analysis.types.ValueType.OBJECT_TYPE;

public class ClassChecker extends AbstractNodeAnalyzer<ValueType> {

    private SymbolTable<SymbolType> localScope;

    /** Creates a declaration checker with a given type hierarchy and local symbol table. */
    public ClassChecker(SymbolTable<SymbolType> local) {
        this.localScope = local;
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
    public ValueType analyze(Program program){
        for (Declaration decl: program.declarations){
            decl.dispatch(this);
        }
        return null;
    }

    @Override
    public ValueType analyze(VarDef vardef){
        String name = vardef.getIdentifier().name;
        if (localScope.get(name) != null){
            typeError(vardef, String.format("Duplicate declaration of identifier in same scope: `%s`", vardef.var.identifier.name));
        }
        return null;
    }

    @Override
    public ClassValueType analyze(ClassDef classdef){

        String name = classdef.getIdentifier().name;
        if (localScope.get(name) != null){
            typeError(classdef, String.format("Duplicate declaration of identifier in same scope: `%s`", classdef.getIdentifier().name));
        } else{
            SymbolTable<SymbolType> newScope = new SymbolTable<SymbolType>(localScope); //creating new scope for this class with original scope as parent
            localScope = newScope;  //assigning the currently active scope as the new scope
            SymbolType type = classdef.dispatch(this);  //perform semantic analysis with new scope
            localScope = localScope.getParent(); //coming back from inside the new class, return current scope back to original
            localScope.put(name, type);

        }
        return null;
    }

    @Override
    public ValueType analyze(GlobalDecl globaldecl){
        String name = globaldecl.getIdentifier().name;
        if (StudentAnalysis.globalScope.declares(name)){
            typeError(globaldecl, String.format("Cannot redeclare existing variable `%s`", globaldecl.getIdentifier().name));
        } else{
            SymbolType type = globaldecl.dispatch(this);
            StudentAnalysis.globalScope.put(name, type);
        }
        return null;
    }

    public ValueType analyze(NonLocalDecl nonlocaldecl){
        String name = nonlocaldecl.getIdentifier().name;
        SymbolTable<SymbolType> parent = localScope.getParent();
        if (parent != null || parent.declares(name) == false){
            typeError(nonlocaldecl, String.format("Not a nonlocal variable `%s`", nonlocaldecl.getIdentifier().name));
        } else {
            SymbolType type = nonlocaldecl.dispatch(this);
            parent.put(name, type);
        }
        return null;
    }



    @Override
    public ValueType analyze(FuncDef funcdef){
//        String name = funcdef.getIdentifier().name;
//        List<TypedVar> varlist= funcdef.params;
//
//        if (localScope.get(name) instanceof FuncDef){
//
//        }
//        if (localScope.declares(name) & funcdef.params != localScope.get(name).){
//            typeError(funcdef, String.format("Cannot declare existing function `%s`", funcdef.var.identifier.name));
//        }
        return null;
    }
}
