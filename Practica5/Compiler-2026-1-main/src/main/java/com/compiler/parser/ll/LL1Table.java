package com.compiler.parser.ll;

import java.util.Map;

import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.syntax.StaticAnalyzer;

/**
 * Builds and represents the LL(1) parsing table.
 * Main task of Practice 7.
 */
public class LL1Table {
    // The table is a nested Map: Map<NonTerminal, Map<Terminal, Production>>
    private final Map<Symbol, Map<Symbol, Production>> table;
    private final StaticAnalyzer analyzer;

    public LL1Table(StaticAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.table = new java.util.HashMap<>();
        // TODO: Initialize the table structure
    }

    /**
     * Fills the parsing table M using FIRST and FOLLOW sets.
     * Should detect and report conflicts if the grammar is not LL(1).
     *
     * Pseudocode for LL(1) table construction:
     *
     * 1. For each production A -> α in the grammar:
     *      a. For each terminal 'a' in FIRST(α):
     *          - If 'a' is not ε:
     *              - If M[A, a] is empty:
     *                  - Set M[A, a] = production (A -> α)
     *              - Else:
     *                  - Report conflict (grammar is not LL(1))
     *      b. If ε is in FIRST(α):
     *          - For each terminal 'b' in FOLLOW(A):
     *              - If M[A, b] is empty:
     *                  - Set M[A, b] = production (A -> α)
     *              - Else:
     *                  - Report conflict (grammar is not LL(1))
     *
     * 2. After filling, the table M can be used for parsing.
     */
    public void build() {
        // TODO: Implement the LL(1) table construction algorithm as described above.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the production for a non-terminal and an input token.
     * @param nonTerminal The non-terminal at the top of the stack.
     * @param terminal The current input token.
     * @return The production to apply, or null if it is an error.
     */
    public Production getProduction(Symbol nonTerminal, Symbol terminal) {
        // TODO: Implement the table lookup.
        // General structure:
        // 1. Lookup the inner map for the given nonTerminal.
        // 2. If found, lookup the production for the given terminal.
        // 3. Return the production, or null if not found.
        throw new UnsupportedOperationException("Not implemented");
    }
}