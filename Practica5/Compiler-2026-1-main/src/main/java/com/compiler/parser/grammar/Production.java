package com.compiler.parser.grammar;

import java.util.List;

/**
 * Represents a production rule in the grammar (e.g., E -> E + T).
 */
public class Production {
    /** Non-terminal symbol on the left side of the production. */
    public final Symbol left;
    /** Sequence of symbols on the right side of the production. */
    public final List<Symbol> right;

    /**
     * Constructs a Production with the specified left non-terminal and right sequence of symbols.
     *
     * @param left  the non-terminal symbol on the left side of the production
     * @param right the sequence of symbols on the right side of the production
     * @throws IllegalArgumentException if left or right is null
     */
    public Production(Symbol left, List<Symbol> right) {
        if (left == null) {
            throw new IllegalArgumentException("Left symbol cannot be null");
        }
        if (right == null) {
            throw new IllegalArgumentException("Right symbols list cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the non-terminal symbol on the left side of the production.
     *
     * @return the left Symbol of the production
     */
    public Symbol getLeft() {
        return left;
    }

    /**
     * Returns the sequence of symbols on the right side of the production.
     *
     * @return the list of right Symbols of the production
     */
    public List<Symbol> getRight() {
        return right;
    }


}