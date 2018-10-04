package chocopy.common.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolTable<T> {

    private final Map<String, T> tab = new HashMap<>();
    private final SymbolTable<T> parent;

    public SymbolTable(SymbolTable<T> parent) {
        this.parent = parent;
    }

    public SymbolTable() {
        this.parent = null;
    }

    /** Returns the mapping in this scope or in the parent scope using a recursive traversal */
    public T get(String name) {
        if (tab.containsKey(name)) {
            return tab.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            return null;
        }
    }

    /** Adds a new mapping in the current scope, possibly shadowing mappings in the parent scope. */
    public SymbolTable<T> put(String name, T value) {
        tab.put(name, value);
        return this; // for chaining
    }

    /** Returns whether a name has been mapped in the CURRENT scope. */
    public boolean declares(String name) {
        return tab.containsKey(name);
    }

    /** Returns all the names declared in the CURRENT scope. */
    public Set<String> getDeclaredSymbols() { return tab.keySet(); }

    /** Returns the parent symbol table, if it exists. */
    public SymbolTable<T> getParent() {
        return this.parent;
    }

}
