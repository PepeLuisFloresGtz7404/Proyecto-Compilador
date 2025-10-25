package com.compiler.parser.grammar;

/**
 * Represents a symbol in a grammar (terminal or non-terminal).
 * Each symbol has a name and a type.
 */
public class Symbol {
    /** The name of the symbol. */
    public final String name;
    /** The type of the symbol (terminal or non-terminal). */
    public final SymbolType type;

    /**
     * Constructs a Symbol with the specified name and type.
     *
     * @param name the name of the symbol
     * @param type the type of the symbol (terminal or non-terminal)
     * @throws IllegalArgumentException if name or type is null
     */
    public Symbol(String name, SymbolType type) {
        if (name == null || type == null) {
            throw new IllegalArgumentException("Name and type must not be null");
        }
        this.name = name;
        this.type = type;
    }

    /**
     * Checks if this symbol is equal to another object.
     * Two symbols are equal if their names and types are equal.
     *
     * @param obj the object to compare
     * @return true if the symbols are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Symbol)) return false;
        Symbol other = (Symbol) obj;
        return name.equals(other.name) && type == other.type;
    }

    /**
     * Returns the hash code for this symbol.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}