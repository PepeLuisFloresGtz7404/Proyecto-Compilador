package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import java.util.*;

/**
 * NfaToDfaConverter
 * -----------------
 * This class provides a static method to convert a Non-deterministic Finite Automaton (NFA)
 * into a Deterministic Finite Automaton (DFA) using the standard subset construction algorithm.
 */
public class NfaToDfaConverter {
    /**
     * Default constructor for NfaToDfaConverter.
     */
    public NfaToDfaConverter() {
        // Constructor doesn't need specific implementation
    }

    /**
     * Converts an NFA to a DFA using the subset construction algorithm.
     * Each DFA state represents a set of NFA states. Final states are marked if any NFA state in the set is final.
     *
     * @param nfa The input NFA
     * @param alphabet The input alphabet (set of characters)
     * @return The resulting DFA
     */
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        List<DfaState> dfaStates = new ArrayList<>();
        Queue<DfaState> unmarkedStates = new LinkedList<>();
        
        // Create initial DFA state from epsilon-closure of NFA start state
        Set<State> initialNfaStates = new HashSet<>();
        initialNfaStates.add(nfa.startState);
        Set<State> initialClosure = epsilonClosure(initialNfaStates);
        
        DfaState startDfaState = new DfaState(initialClosure);
        dfaStates.add(startDfaState);
        unmarkedStates.add(startDfaState);
        
        // Process all unmarked DFA states
        while (!unmarkedStates.isEmpty()) {
            DfaState currentDfaState = unmarkedStates.poll();
            
            // For each symbol in the alphabet
            for (char symbol : alphabet) {
                // Compute move and epsilon-closure for current DFA state
                Set<State> moveResult = move(currentDfaState.getNfaStates(), symbol);
                if (!moveResult.isEmpty()) {
                    Set<State> targetNfaStates = epsilonClosure(moveResult);
                    
                    // Check if this set of NFA states already exists as a DFA state
                    DfaState targetDfaState = findDfaState(dfaStates, targetNfaStates);
                    
                    if (targetDfaState == null) {
                        // Create new DFA state
                        targetDfaState = new DfaState(targetNfaStates);
                        dfaStates.add(targetDfaState);
                        unmarkedStates.add(targetDfaState);
                    }
                    
                    // Add transition from current to target DFA state
                    currentDfaState.addTransition(symbol, targetDfaState);
                }
            }
        }
        
        // Mark DFA states as final if any NFA state in their set is final
        for (DfaState dfaState : dfaStates) {
            for (State nfaState : dfaState.getNfaStates()) {
                if (nfaState.isFinal()) {
                    dfaState.setFinal(true);
                    break;
                }
            }
        }
        
        return new DFA(startDfaState, dfaStates);
    }

    /**
     * Computes the epsilon-closure of a set of NFA states.
     * The epsilon-closure is the set of states reachable by epsilon (null) transitions.
     *
     * @param states The set of NFA states.
     * @return The epsilon-closure of the input states.
     */
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        
        // Add all initial states to the stack
        for (State state : states) {
            stack.push(state);
        }
        
        // Process states until stack is empty
        while (!stack.isEmpty()) {
            State currentState = stack.pop();
            
            // For each epsilon transition from current state
            for (State epsilonState : currentState.getEpsilonTransitions()) {
                if (!closure.contains(epsilonState)) {
                    closure.add(epsilonState);
                    stack.push(epsilonState);
                }
            }
        }
        
        return closure;
    }

    /**
     * Returns the set of states reachable from a set of NFA states by a given symbol.
     *
     * @param states The set of NFA states.
     * @param symbol The input symbol.
     * @return The set of reachable states.
     */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();
        
        // For each state in the input set
        for (State state : states) {
            // For each transition with the given symbol
            for (State destinationState : state.getTransitions(symbol)) {
                result.add(destinationState);
            }
        }
        
        return result;
    }

    /**
     * Finds an existing DFA state representing a given set of NFA states.
     *
     * @param dfaStates The list of DFA states.
     * @param targetNfaStates The set of NFA states to search for.
     * @return The matching DFA state, or null if not found.
     */
    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState dfaState : dfaStates) {
            if (dfaState.getNfaStates().equals(targetNfaStates)) {
                return dfaState;
            }
        }
        return null;
    }
}