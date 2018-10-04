package chocopy.common.analysis.types;

import java.util.Objects;

import chocopy.common.astnodes.ListType;

public class ListValueType extends ValueType {

    public final ValueType elementType;

    public ListValueType(ValueType elementType) {
        this.elementType = elementType;
    }

    public ListValueType(ListType typeAnnotation) {
        this.elementType = ValueType.annotationToValueType(typeAnnotation.elementType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListValueType listType = (ListValueType) o;
        return Objects.equals(elementType, listType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }

    @Override
    public String toString() {
        return "[" + elementType.toString() + "]";
    }
}
