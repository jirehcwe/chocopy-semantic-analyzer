package chocopy.common.analysis.types;

import java.util.Objects;

import chocopy.common.astnodes.ClassType;

public class ClassValueType extends ValueType {

    public final String className;

    public ClassValueType(String className) {
        this.className = className;
    }

    public ClassValueType(ClassType classTypeAnnotation) {
        this.className = classTypeAnnotation.className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassValueType classType = (ClassValueType) o;
        return Objects.equals(className, classType.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public String toString() {
        return className;
    }
}
