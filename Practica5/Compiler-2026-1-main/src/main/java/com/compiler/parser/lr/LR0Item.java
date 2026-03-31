package com.compiler.parser.lr;

import com.compiler.parser.grammar.Production;

/**
 * Represents an LR(0) item, which is a production with a dot (•)
 * at some position in the right-hand side.
 * Task for Practice 8.
 */
public class LR0Item {
    public final Production production;
    public final int dotPosition;


    /**
     * Constructs an LR(0) item with the given production and dot position.
     * @param production The production rule.
     * @param dotPosition The position of the dot in the right-hand side.
     * Detailed pseudocode:
     * 1. Assign the production to the field.
     * 2. Assign the dotPosition to the field.
     * 3. Optionally, validate that dotPosition is within valid bounds (0 <= dotPosition <= production.right.size()).
     */
    public LR0Item(Production production, int dotPosition) {
        // TODO: Implement constructor logic as described above.
        throw new UnsupportedOperationException("Not implemented");
    }

    // TODO: Implement equals, hashCode, and a method to get the symbol after the dot.
    /*
     * Detailed pseudocode:
     *
     * equals(Object obj):
     *   1. If obj is not an LR0Item, return false.
     *   2. Compare production and dotPosition for equality.
     *   3. Return true if both match, false otherwise.
     *
     * hashCode():
     *   1. Compute hash using production and dotPosition.
     *   2. Return the combined hash value.
     *
     * getSymbolAfterDot():
     *   1. If dotPosition < production.right.size():
     *        - Return production.right.get(dotPosition).
     *      Else:
     *        - Return null (dot is at the end).
     */
}