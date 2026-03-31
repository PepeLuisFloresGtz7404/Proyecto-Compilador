package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Builds the canonical collection of LR(1) items (the DFA automaton).
 * Items contain a lookahead symbol.
 */
public class LR1Automaton {
    private final Grammar grammar;
    private final List<Set<LR1Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
    private String augmentedLeftName = null;

    public LR1Automaton(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public List<Set<LR1Item>> getStates() { return states; }
    public Map<Integer, Map<Symbol, Integer>> getTransitions() { return transitions; }

    /**
     * CLOSURE for LR(1): standard algorithm using FIRST sets to compute lookaheads for new items.
     */
    private Set<LR1Item> closure(Set<LR1Item> items) {
        Set<LR1Item> closure = new HashSet<>(items);
        java.util.Queue<LR1Item> worklist = new java.util.LinkedList<>(items);
        
        // Pre-calculate FIRST sets
        Map<Symbol, Set<Symbol>> firstSets = computeFirstSets();
        Symbol epsilon = getEpsilonSymbol();
        
        while (!worklist.isEmpty()) {
            LR1Item item = worklist.poll();
            Symbol b = item.getSymbolAfterDot();
            
            // If B is a non-terminal
            if (b != null && b.type == SymbolType.NON_TERMINAL) {
                // Get beta (symbols after B)
                List<Symbol> beta = new ArrayList<>();
                for (int i = item.dotPosition + 1; i < item.production.right.size(); i++) {
                    beta.add(item.production.right.get(i));
                }
                
                // Add lookahead to beta
                beta.add(item.lookahead);
                
                // Compute FIRST(beta a)
                Set<Symbol> firstBetaA = computeFirstOfSequence(beta, firstSets, epsilon);
                
                // For each production B -> gamma
                for (Production prod : getProductionsOf(b)) {
                    // For each terminal in FIRST(beta a)
                    for (Symbol lookahead : firstBetaA) {
                        if (lookahead.type == SymbolType.TERMINAL) {
                            LR1Item newItem = new LR1Item(prod, 0, lookahead);
                            if (closure.add(newItem)) {
                                worklist.add(newItem);
                            }
                        }
                    }
                }
            }
        }
        
        return closure;
    }

    /**
     * Get all productions for a given non-terminal symbol.
     */
    private List<Production> getProductionsOf(Symbol symbol) {
        List<Production> result = new ArrayList<>();
        for (Production prod : grammar.getProductions()) {
            if (prod.left.equals(symbol)) {
                result.add(prod);
            }
        }
        return result;
    }

    /**
     * Get the epsilon symbol.
     */
    private Symbol getEpsilonSymbol() {
        return new Symbol("ε", SymbolType.TERMINAL);
    }

    /**
     * Compute FIRST sets for all symbols in the grammar.
     */
    private Map<Symbol, Set<Symbol>> computeFirstSets() {
        Map<Symbol, Set<Symbol>> firstSets = new HashMap<>();
        Symbol epsilon = getEpsilonSymbol();
        
        // Initialize FIRST sets
        for (Symbol t : grammar.getTerminals()) {
            Set<Symbol> set = new HashSet<>();
            set.add(t);
            firstSets.put(t, set);
        }
        
        for (Symbol nt : grammar.getNonTerminals()) {
            firstSets.put(nt, new HashSet<>());
        }
        
        // Iterate until no changes
        boolean changed = true;
        while (changed) {
            changed = false;
            
            for (Production prod : grammar.getProductions()) {
                Symbol lhs = prod.left;
                List<Symbol> rhs = prod.right;
                
                // If production is A -> ε
                if (rhs.size() == 1 && rhs.get(0).equals(epsilon)) {
                    if (firstSets.get(lhs).add(epsilon)) {
                        changed = true;
                    }
                    continue;
                }
                
                // For each symbol in the RHS
                boolean allHaveEpsilon = true;
                for (Symbol symbol : rhs) {
                    Set<Symbol> firstOfSymbol = firstSets.get(symbol);
                    if (firstOfSymbol != null) {
                        for (Symbol s : firstOfSymbol) {
                            if (!s.equals(epsilon)) {
                                if (firstSets.get(lhs).add(s)) {
                                    changed = true;
                                }
                            }
                        }
                        
                        if (!firstOfSymbol.contains(epsilon)) {
                            allHaveEpsilon = false;
                            break;
                        }
                    } else {
                        allHaveEpsilon = false;
                        break;
                    }
                }
                
                if (allHaveEpsilon) {
                    if (firstSets.get(lhs).add(epsilon)) {
                        changed = true;
                    }
                }
            }
        }
        
        return firstSets;
    }

    /**
     * Compute FIRST of a sequence of symbols.
     */
    private Set<Symbol> computeFirstOfSequence(List<Symbol> seq, Map<Symbol, Set<Symbol>> firstSets, Symbol epsilon) {
        Set<Symbol> result = new HashSet<>();
        
        if (seq.isEmpty()) {
            result.add(epsilon);
            return result;
        }
        
        for (int i = 0; i < seq.size(); i++) {
            Symbol x = seq.get(i);
            Set<Symbol> firstX = firstSets.get(x);
            
            if (firstX == null) {
                firstX = new HashSet<>();
                if (x.type == SymbolType.TERMINAL) {
                    firstX.add(x);
                }
            }
            
            // Add all except epsilon
            for (Symbol s : firstX) {
                if (!s.equals(epsilon)) {
                    result.add(s);
                }
            }
            
            // If epsilon not in FIRST(X), stop
            if (!firstX.contains(epsilon)) {
                break;
            }
            
            // If this is the last symbol and it can be epsilon
            if (i == seq.size() - 1 && firstX.contains(epsilon)) {
                result.add(epsilon);
            }
        }
        
        return result;
    }

    /**
     * GOTO for LR(1): moves dot over symbol and takes closure.
     */
    private Set<LR1Item> goTo(Set<LR1Item> state, Symbol symbol) {
        Set<LR1Item> movedItems = new HashSet<>();
        
        for (LR1Item item : state) {
            Symbol x = item.getSymbolAfterDot();
            if (x != null && x.equals(symbol)) {
                // Move dot one position forward
                LR1Item newItem = new LR1Item(item.production, item.dotPosition + 1, item.lookahead);
                movedItems.add(newItem);
            }
        }
        
        return closure(movedItems);
    }

    /**
     * Build the LR(1) canonical collection: states and transitions.
     */
    public void build() {
        states.clear();
        transitions.clear();
        
        // Create augmented grammar start symbol
        augmentedLeftName = grammar.getStartSymbol().name + "'";
        Symbol augmentedStart = new Symbol(augmentedLeftName, SymbolType.NON_TERMINAL);
        
        // Create augmented production: S' -> S
        Production augmentedProd = new Production(augmentedStart, 
            java.util.Arrays.asList(grammar.getStartSymbol()));
        
        // Create $ symbol for end of input
        Symbol endOfInput = new Symbol("$", SymbolType.TERMINAL);
        
        // Create initial item [S' -> • S, $]
        LR1Item initialItem = new LR1Item(augmentedProd, 0, endOfInput);
        Set<LR1Item> initialSet = new HashSet<>();
        initialSet.add(initialItem);
        
        // I0 is closure of initial item
        Set<LR1Item> i0 = closure(initialSet);
        states.add(i0);
        
        // Worklist for states to process
        java.util.Queue<Integer> worklist = new java.util.LinkedList<>();
        worklist.add(0);
        
        while (!worklist.isEmpty()) {
            int stateIdx = worklist.poll();
            Set<LR1Item> state = states.get(stateIdx);
            
            // Get all symbols that appear after the dot
            Set<Symbol> symbols = new HashSet<>();
            for (LR1Item item : state) {
                Symbol sym = item.getSymbolAfterDot();
                if (sym != null) {
                    symbols.add(sym);
                }
            }
            
            // For each symbol, compute GOTO
            for (Symbol x : symbols) {
                Set<LR1Item> j = goTo(state, x);
                
                if (!j.isEmpty()) {
                    // Check if state already exists
                    int targetIdx = -1;
                    for (int i = 0; i < states.size(); i++) {
                        if (states.get(i).equals(j)) {
                            targetIdx = i;
                            break;
                        }
                    }
                    
                    // If new state, add it
                    if (targetIdx == -1) {
                        targetIdx = states.size();
                        states.add(j);
                        worklist.add(targetIdx);
                    }
                    
                    // Add transition
                    transitions.computeIfAbsent(stateIdx, k -> new HashMap<>()).put(x, targetIdx);
                }
            }
        }
    }

    public String getAugmentedLeftName() { return augmentedLeftName; }
}