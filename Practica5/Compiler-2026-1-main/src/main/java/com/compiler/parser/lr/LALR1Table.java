package com.compiler.parser.lr;

/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LRAutomaton automaton;

    public LALR1Table(LRAutomaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Builds the LALR(1) parsing table.
     * 1. Calculates lookaheads for each item in the automaton.
     * 2. Fills the ACTION and GOTO tables.
     * 3. Detects and reports Shift/Reduce and Reduce/Reduce conflicts.
     *
     * Detailed pseudocode:
     *
     * 1. For each state in the LRAutomaton:
     *      a. For each item in the state:
     *          i. If the item is of the form [A -> α • a β, lookahead] and 'a' is a terminal:
     *              - Set ACTION[state, a] = shift to state s' (where s' is the state after shifting 'a').
     *          ii. If the item is of the form [A -> α •, lookahead]:
     *              - If A is not the start symbol:
     *                  - For each lookahead symbol:
     *                      - If ACTION[state, lookahead] is empty:
     *                          - Set ACTION[state, lookahead] = reduce by A -> α
     *                      - Else:
     *                          - Report Reduce/Reduce or Shift/Reduce conflict.
     *              - If A is the start symbol and lookahead is $:
     *                  - Set ACTION[state, $] = accept
     *      b. For each non-terminal B:
     *          - If there is a transition from state on B to state s':
     *              - Set GOTO[state, B] = s'
     *
     * 2. After filling ACTION and GOTO tables, check for any conflicts and report them.
     */
    public void build() {
        // TODO: Implement the construction of the LALR(1) table as described above.
        throw new UnsupportedOperationException("Not implemented");
    }
}