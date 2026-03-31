package com.compiler.parser.lr;

import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;

/**
 * Builds the canonical collection of LR(0) items (the DFA automaton).
 * Main task for Practice 8.
 */
public class LRAutomaton {
    private final Grammar grammar;

    public LRAutomaton(Grammar grammar) {
        this.grammar = grammar;
    }

    /**
     * Implements the CLOSURE operation on a set of LR(0) items.
     * @param items The initial set of items.
     * @return The closed set of items.
     *
     * Detailed pseudocode:
     * 1. Initialize closureSet = items.
     * 2. Repeat until no new items are added:
     *      a. For each item [A -> α • B β] in closureSet, where B is a non-terminal and the dot is before B:
     *          i. For each production B -> γ in the grammar:
     *              - If [B -> • γ] is not in closureSet:
     *                  - Add [B -> • γ] to closureSet.
     * 3. Return closureSet.
     */
    private Set<LR0Item> closure(Set<LR0Item> items) {
        // TODO: Implement the CLOSURE operation as described above.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Implements the GOTO operation on a state and a symbol.
     * @param state The current set of items (state).
     * @param symbol The transition symbol.
     * @return The new state (set of items).
     *
     * Detailed pseudocode:
     * 1. Initialize nextItems = empty set.
     * 2. For each item [A -> α • X β] in state, where X == symbol:
     *      a. Add [A -> α X • β] to nextItems.
     * 3. Return closure(nextItems).
     */
    private Set<LR0Item> goTo(Set<LR0Item> state, Symbol symbol) {
        // TODO: Implement the GOTO operation as described above.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Builds the complete LR(0) automaton.
     *
     * Detailed pseudocode:
     * 1. Create the initial item [S' -> • S], where S' is the augmented start symbol.
     * 2. Compute the closure of the initial item to form the initial state.
     * 3. Initialize a set of states with the initial state.
     * 4. Initialize a worklist (queue) with the initial state.
     * 5. While the worklist is not empty:
     *      a. Remove a state from the worklist.
     *      b. For each grammar symbol X:
     *          i. Compute goTo(state, X) to get nextState.
     *          ii. If nextState is not empty and not already in the set of states:
     *              - Add nextState to the set of states and to the worklist.
     *      c. Record transitions between states for each symbol.
     * 6. Store all states and transitions in the automaton.
     */
    public void build() {
        // TODO: Use closure() and goTo() to generate all states of the automaton as described above.
        throw new UnsupportedOperationException("Not implemented");
    }
}