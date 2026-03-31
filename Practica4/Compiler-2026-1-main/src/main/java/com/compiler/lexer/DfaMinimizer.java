/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * Implements DFA minimization using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
    public DfaMinimizer() {
        // Constructor vacío - no se necesita inicialización especial
    }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        if (originalDfa == null || alphabet == null) {
            return originalDfa;
        }
        
        // 1. Collect and sort all DFA states
        List<DfaState> allStates = new ArrayList<>(originalDfa.getAllStates());
        allStates.sort((s1, s2) -> Integer.compare(s1.id, s2.id));
        
        // 2. Initialize table of state pairs
        Map<Pair, Boolean> table = new HashMap<>();
        
        // Mark pairs as distinguishable if one is final and the other is not
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);
                
                // Marcar como distinguibles si uno es final y el otro no
                if (s1.isFinal() != s2.isFinal()) {
                    table.put(pair, true);
                } else {
                    table.put(pair, false);
                }
            }
        }
        
        // 3. Iteratively mark pairs as distinguishable
        boolean changed = true;
        while (changed) {
            changed = false;
            
            for (int i = 0; i < allStates.size(); i++) {
                for (int j = i + 1; j < allStates.size(); j++) {
                    DfaState s1 = allStates.get(i);
                    DfaState s2 = allStates.get(j);
                    Pair currentPair = new Pair(s1, s2);
                    
                    // Si ya están marcados como distinguibles, continuar
                    if (table.get(currentPair)) {
                        continue;
                    }
                    
                    // Verificar si deben ser marcados como distinguibles
                    for (Character symbol : alphabet) {
                        DfaState next1 = s1.getTransition(symbol);
                        DfaState next2 = s2.getTransition(symbol);
                        
                        // Si solo uno tiene transición, son distinguibles
                        if ((next1 == null) != (next2 == null)) {
                            table.put(currentPair, true);
                            changed = true;
                            break;
                        }
                        
                        // Si ambos tienen transición, verificar si van a estados distinguibles
                        if (next1 != null && next2 != null && !next1.equals(next2)) {
                            Pair nextPair = new Pair(next1, next2);
                            if (table.get(nextPair)) {
                                table.put(currentPair, true);
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // 4. Partition states into equivalence classes
        List<Set<DfaState>> partitions = createPartitions(allStates, table);
        
        // Si no hay cambios, retornar DFA original
        if (partitions.size() == allStates.size()) {
            return originalDfa;
        }
        
        // 5. Create new minimized states for each partition
        Map<Set<DfaState>, DfaState> partitionToState = new HashMap<>();
        List<DfaState> minimizedStates = new ArrayList<>();
        
        for (Set<DfaState> partition : partitions) {
            // Crear nuevo estado usando el primer estado de la partición como representante
            DfaState representative = partition.iterator().next();
            DfaState newState = new DfaState(representative.getNfaStates());
            newState.setFinal(representative.isFinal());
            
            partitionToState.put(partition, newState);
            minimizedStates.add(newState);
        }
        
        // 6. Reconstruct transitions for minimized states
        for (Set<DfaState> partition : partitions) {
            DfaState newState = partitionToState.get(partition);
            DfaState representative = partition.iterator().next();
            
            for (Character symbol : alphabet) {
                DfaState targetState = representative.getTransition(symbol);
                if (targetState != null) {
                    // Encontrar la partición que contiene el estado objetivo
                    Set<DfaState> targetPartition = null;
                    for (Set<DfaState> p : partitions) {
                        if (p.contains(targetState)) {
                            targetPartition = p;
                            break;
                        }
                    }
                    
                    if (targetPartition != null) {
                        DfaState targetNewState = partitionToState.get(targetPartition);
                        newState.addTransition(symbol, targetNewState);
                    }
                }
            }
        }
        
        // 7. Set start state and return minimized DFA
        DfaState originalStartState = originalDfa.getStartState();
        DfaState newStartState = null;
        
        for (Set<DfaState> partition : partitions) {
            if (partition.contains(originalStartState)) {
                newStartState = partitionToState.get(partition);
                break;
            }
        }
        
        return new DFA(newStartState, minimizedStates);
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        // 1. Initialize each state as its own parent
        Map<DfaState, DfaState> parent = new HashMap<>();
        for (DfaState state : allStates) {
            parent.put(state, state);
        }
        
        // 2. For each pair not marked as distinguishable, union the states
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);
                
                if (!table.get(pair)) { // Si no son distinguibles, son equivalentes
                    union(parent, s1, s2);
                }
            }
        }
        
        // 3. Group states by their root parent
        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState state : allStates) {
            DfaState root = find(parent, state);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(state);
        }
        
        // 4. Return list of partitions
        return new ArrayList<>(groups.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        // If parent[state] == state, return state
        if (parent.get(state).equals(state)) {
            return state;
        }
        
        // Else, recursively find parent and apply path compression
        DfaState root = find(parent, parent.get(state));
        parent.put(state, root); // Path compression
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        // Find roots of s1 and s2
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        
        // If roots are different, set parent of one to the other
        if (!root1.equals(root2)) {
            parent.put(root2, root1);
        }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            // Assign s1 and s2 so that s1.id <= s2.id
            if (s1.id <= s2.id) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            // Return true if both s1 and s2 ids match
            return s1.id == pair.s1.id && s2.id == pair.s2.id;
        }

        @Override
        public int hashCode() {
            // Return hash of s1.id and s2.id
            return Objects.hash(s1.id, s2.id);
        }
    }
}