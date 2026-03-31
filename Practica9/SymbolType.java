package com.compiler.parser.grammar;

/**
 * Enum to distinguish between Terminal and Non-Terminal symbols in a grammar.
 * <p>
 * TERMINAL: Represents a terminal symbol (token) in the grammar.
 * NON_TERMINAL: Represents a non-terminal symbol (syntactic variable) in the grammar.
 */
public enum SymbolType {
    /**
     * Terminal symbol (token) in the grammar.
     */
    TERMINAL,

    /**
     * Non-terminal symbol (syntactic variable) in the grammar.
     */
    NON_TERMINAL
}