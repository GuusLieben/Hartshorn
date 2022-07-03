package org.dockbox.hartshorn.hsl.interpreter;

import org.dockbox.hartshorn.hsl.runtime.RuntimeError;
import org.dockbox.hartshorn.hsl.token.Token;

import java.util.HashMap;
import java.util.Map;

public class VariableScope {

    private final VariableScope enclosing;
    private final Map<String, Object> valuesMap = new HashMap<>();

    public VariableScope() {
        this.enclosing = null;
    }

    public VariableScope(final VariableScope enclosing) {
        this.enclosing = enclosing;
    }

    public Map<String, Object> values() {
        return this.valuesMap;
    }

    public Object get(final Token name) {
        if (this.valuesMap.containsKey(name.lexeme())) {
            return this.valuesMap.get(name.lexeme());
        }

        // If the variable isn’t found in this scope, we simply try the enclosing one
        if (this.enclosing != null) return this.enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    public void define(final String name, final Object value) {
        this.valuesMap.put(name, value);
    }

    public void assign(final Token name, final Object value) {
        if (this.valuesMap.containsKey(name.lexeme())) {
            this.valuesMap.put(name.lexeme(), value);
            return;
        }
        // If the variable isn’t in this scope, it checks the outer one, recursively
        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    void assignAt(final int distance, final Token name, final Object value) {
        this.ancestor(name, distance).valuesMap.put(name.lexeme(), value);
    }
    public boolean contains(final String name) {
        return this.valuesMap.containsKey(name);
    }

    public Object getAt(final Token at, final int distance, final String name) {
        return this.ancestor(at, distance).valuesMap.get(name);
    }

    public Object getAt(final Token name, final int distance) {
        return this.getAt(name, distance, name.lexeme());
    }

    VariableScope ancestor(final Token name, final int distance) {
        VariableScope variableScope = this;
        for (int i = 0; i < distance; i++) {
            variableScope = variableScope.enclosing;
            if (variableScope == null) {
                throw new RuntimeError(name, "No enclosing scope at distance %s for active scope.".formatted(distance));
            }
        }
        return variableScope;
    }

    public VariableScope enclosing() {
        return this.enclosing;
    }
}
