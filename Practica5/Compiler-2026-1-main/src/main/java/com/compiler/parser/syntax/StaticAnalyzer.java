package com.compiler.parser.syntax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;
    
    // Symbol representing epsilon (empty string)
    private static final Symbol EPSILON = new Symbol("ε", SymbolType.TERMINAL);
    // Symbol representing end of input
    private static final Symbol END_OF_INPUT = new Symbol("$", SymbolType.TERMINAL);

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        // Initialize FIRST sets for all symbols
        initializeFirstSets();
        
        boolean changed = true;
        while (changed) {
            changed = false;
            
            // For each production in the grammar
            for (Production production : grammar.getProductions()) {
                Symbol leftSide = production.getLeft();
                var rightSide = production.getRight();
                
                Set<Symbol> firstOfA = firstSets.get(leftSide);
                int originalSize = firstOfA.size();
                
                // Calculate FIRST for this production
                calculateFirstForProduction(leftSide, rightSide);
                
                // Check if the set changed
                if (firstOfA.size() > originalSize) {
                    changed = true;
                }
            }
        }
        
        return new HashMap<>(firstSets);
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // First, calculate FIRST sets if not already done
        if (firstSets.isEmpty()) {
            getFirstSets();
        }
        
        // Initialize FOLLOW sets for all non-terminals
        initializeFollowSets();
        
        // Add $ to FOLLOW(start symbol)
        Symbol startSymbol = grammar.getStartSymbol();
        followSets.get(startSymbol).add(END_OF_INPUT);
        
        boolean changed = true;
        while (changed) {
            changed = false;
            
            // For each production B -> X1 X2 ... Xn
            for (Production production : grammar.getProductions()) {
                Symbol B = production.getLeft();
                var rightSide = production.getRight();
                
                // For each symbol Xi in the right side
                for (int i = 0; i < rightSide.size(); i++) {
                    Symbol Xi = rightSide.get(i);
                    
                    // Only process non-terminals
                    if (Xi.type != SymbolType.NON_TERMINAL) {
                        continue;
                    }
                    
                    Set<Symbol> followXi = followSets.get(Xi);
                    int originalSize = followXi.size();
                    
                    // Calculate FOLLOW for Xi
                    calculateFollowForSymbol(B, rightSide, i);
                    
                    // Check if the set changed
                    if (followXi.size() > originalSize) {
                        changed = true;
                    }
                }
            }
        }
        
        return new HashMap<>(followSets);
    }
    
    /**
     * Initialize FIRST sets for all symbols in the grammar.
     */
    private void initializeFirstSets() {
        // Initialize FIRST sets for terminals
        for (Symbol symbol : grammar.getTerminals()) {
            Set<Symbol> firstSet = new HashSet<>();
            firstSet.add(symbol);
            firstSets.put(symbol, firstSet);
        }
        
        // Initialize FIRST sets for non-terminals (empty sets)
        for (Symbol symbol : grammar.getNonTerminals()) {
            firstSets.put(symbol, new HashSet<>());
        }
    }
    
    /**
     * Initialize FOLLOW sets for all non-terminals in the grammar.
     */
    private void initializeFollowSets() {
        for (Symbol symbol : grammar.getNonTerminals()) {
            followSets.put(symbol, new HashSet<>());
        }
    }
    
    /**
     * Calculate FIRST set for a specific production A -> rightSide.
     */
    private void calculateFirstForProduction(Symbol A, java.util.List<Symbol> rightSide) {
        Set<Symbol> firstA = firstSets.get(A);
        
        // If production is A -> epsilon (empty production or explicit epsilon)
        if (rightSide.isEmpty() || 
            (rightSide.size() == 1 && rightSide.get(0).name.equals("ε"))) {
            firstA.add(EPSILON);
            return;
        }
        
        // For each symbol Xi in the right-hand side
        for (Symbol Xi : rightSide) {
            Set<Symbol> firstXi = firstSets.get(Xi);
            
            // Add FIRST(Xi) - {epsilon} to FIRST(A)
            for (Symbol symbol : firstXi) {
                if (!symbol.name.equals("ε")) {
                    firstA.add(symbol);
                }
            }
            
            // If epsilon is not in FIRST(Xi), break
            if (!containsEpsilon(firstXi)) {
                break;
            }
            
            // If we've processed all symbols and all contain epsilon
            if (Xi.equals(rightSide.get(rightSide.size() - 1))) {
                firstA.add(EPSILON);
            }
        }
    }
    
    /**
     * Calculate FOLLOW set for symbol at position i in the given production.
     */
    private void calculateFollowForSymbol(Symbol B, java.util.List<Symbol> rightSide, int i) {
        Symbol Xi = rightSide.get(i);
        Set<Symbol> followXi = followSets.get(Xi);
        
        // Look at symbols after Xi
        boolean allHaveEpsilon = true;
        
        for (int j = i + 1; j < rightSide.size(); j++) {
            Symbol Xj = rightSide.get(j);
            Set<Symbol> firstXj = firstSets.get(Xj);
            
            // Add FIRST(Xj) - {epsilon} to FOLLOW(Xi)
            for (Symbol symbol : firstXj) {
                if (!symbol.name.equals("ε")) {
                    followXi.add(symbol);
                }
            }
            
            // If epsilon is not in FIRST(Xj), we can't continue
            if (!containsEpsilon(firstXj)) {
                allHaveEpsilon = false;
                break;
            }
        }
        
        // If we reached the end of the production or all symbols after Xi derive epsilon
        if (i == rightSide.size() - 1 || allHaveEpsilon) {
            // Add FOLLOW(B) to FOLLOW(Xi)
            Set<Symbol> followB = followSets.get(B);
            followXi.addAll(followB);
        }
    }
    
    /**
     * Helper method to check if a set contains epsilon.
     */
    private boolean containsEpsilon(Set<Symbol> symbolSet) {
        return symbolSet.stream().anyMatch(s -> s.name.equals("ε"));
    }
}