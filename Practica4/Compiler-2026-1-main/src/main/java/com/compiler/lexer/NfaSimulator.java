package com.compiler.lexer;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import java.util.HashSet;
import java.util.Set;

/**
 * NfaSimulator
 * ------------
 * This class provides functionality to simulate a Non-deterministic Finite Automaton (NFA)
 * on a given input string. It determines whether the input string is accepted by the NFA by processing
 * each character and tracking the set of possible states, including those reachable via epsilon (ε) transitions.
 *
 * Simulation steps:
 * - Initialize the set of current states with the ε-closure of the NFA's start state.
 * - For each character in the input, compute the next set of states by following transitions labeled with that character,
 *   and include all states reachable via ε-transitions from those states.
 * - After processing the input, check if any of the current states is a final (accepting) state.
 *
 * The class also provides a helper method to compute the ε-closure of a given state, which is the set of all states
 * reachable from the given state using only ε-transitions.
 */
public class NfaSimulator {
    /**
     * Default constructor for NfaSimulator.
     */
    public NfaSimulator() {
        // Constructor doesn't need specific implementation
    }

    /**
     * Simulates the NFA on the given input string.
     * Starts at the NFA's start state and processes each character, following transitions and epsilon closures.
     * If any final state is reached after processing the input, the string is accepted.
     *
     * @param nfa The NFA to simulate.
     * @param input The input string to test.
     * @return True if the input is accepted by the NFA, false otherwise.
     */
    public boolean simulate(NFA nfa, String input) {
        // Initialize current states with epsilon-closure of NFA start state
        Set<State> currentStates = new HashSet<>();
        addEpsilonClosure(nfa.startState, currentStates);
        
        // Process each character in the input string
        for (char c : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            
            // For each current state
            for (State state : currentStates) {
                // For each transition from this state
                for (State nextState : state.getTransitions(c)) {
                    // Add epsilon-closure of the destination state
                    addEpsilonClosure(nextState, nextStates);
                }
            }
            
            // Update current states
            currentStates = nextStates;
        }
        
        // Check if any current state is final
        for (State state : currentStates) {
            if (state.isFinal()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Computes the epsilon-closure: all states reachable from 'start' using only epsilon (null) transitions.
     *
     * @param start The starting state.
     * @param closureSet The set to accumulate reachable states.
     */
    private void addEpsilonClosure(State start, Set<State> closureSet) {
        // If start is not already in the closure set
        if (!closureSet.contains(start)) {
            // Add start to closure set
            closureSet.add(start);
            
            // For each epsilon transition from start
            for (State epsilonState : start.getEpsilonTransitions()) {
                // Recursively add epsilon-closure of destination state
                addEpsilonClosure(epsilonState, closureSet);
            }
        }
    }
}