package chocopy.common.analysis.types;

import chocopy.common.astnodes.ClassType;
import chocopy.common.astnodes.ListType;
import chocopy.common.astnodes.TypeAnnotation;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A ValueType references types that are assigned to variables and
 * expressions.
 *
 * In particular, ValueType can be a {@link ClassValueType} (e.g. "int") or
 * a {@link ListValueType} (e.g. "[int]").
 */

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="kind")
public abstract class ValueType extends SymbolType {

    public static final ClassValueType OBJECT_TYPE = new ClassValueType("object");
    public static final ClassValueType INT_TYPE = new ClassValueType("int");
    public static final ClassValueType STR_TYPE = new ClassValueType("str");
    public static final ClassValueType BOOL_TYPE = new ClassValueType("bool");


    public static ValueType annotationToValueType(TypeAnnotation annotation) {
        if (annotation instanceof ClassType) {
            return new ClassValueType((ClassType) annotation);
        } else {
            assert annotation instanceof ListType; // there should not be any other form of TypeAnnotation
            return new ListValueType((ListType) annotation);
        }
    }

    public static boolean isSpecialType(ValueType t) {
        return INT_TYPE.equals(t) || BOOL_TYPE.equals(t) || STR_TYPE.equals(t);
    }

}
