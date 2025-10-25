package com.compiler.parser.lr;

import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LR1Automaton automaton;

    // merged LALR states and transitions
    private java.util.List<java.util.Set<LR1Item>> lalrStates = new java.util.ArrayList<>();
    private java.util.Map<Integer, java.util.Map<Symbol, Integer>> lalrTransitions = new java.util.HashMap<>();
    
    // ACTION table: state -> terminal -> Action
    public static class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final com.compiler.parser.grammar.Production reduceProd; // for REDUCE

        private Action(Type type, Integer state, com.compiler.parser.grammar.Production prod) {
            this.type = type; this.state = state; this.reduceProd = prod;
        }

        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(com.compiler.parser.grammar.Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }
    }

    private final java.util.Map<Integer, java.util.Map<Symbol, Action>> action = new java.util.HashMap<>();
    private final java.util.Map<Integer, java.util.Map<Symbol, Integer>> gotoTable = new java.util.HashMap<>();
    private final java.util.List<String> conflicts = new java.util.ArrayList<>();
    private int initialState = 0;

    public LALR1Table(LR1Automaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Builds the LALR(1) parsing table.
     */
    public void build() {
        // Step 1: Build the LR(1) automaton
        automaton.build();
        
        // Step 2: Merge LR(1) states to create LALR(1) states
        java.util.List<java.util.Set<LR1Item>> lr1States = automaton.getStates();
        java.util.Map<Integer, java.util.Map<Symbol, Integer>> lr1Transitions = automaton.getTransitions();
        
        // Group states by their kernels
        java.util.Map<java.util.Set<KernelEntry>, java.util.List<Integer>> kernelGroups = new java.util.HashMap<>();
        
        for (int i = 0; i < lr1States.size(); i++) {
            java.util.Set<LR1Item> state = lr1States.get(i);
            java.util.Set<KernelEntry> kernel = new java.util.HashSet<>();
            
            for (LR1Item item : state) {
                kernel.add(new KernelEntry(item.production, item.dotPosition));
            }
            
            kernelGroups.computeIfAbsent(kernel, k -> new java.util.ArrayList<>()).add(i);
        }
        
        // Step 2c: Create mapping from LR(1) to LALR(1) state IDs
        java.util.Map<Integer, Integer> lr1ToLalr = new java.util.HashMap<>();
        
        // Step 2b: Create merged LALR states
        int lalrStateId = 0;
        for (java.util.List<Integer> group : kernelGroups.values()) {
            // Check if this group contains the initial state (state 0 of LR(1))
            boolean isInitialStateGroup = group.contains(0);
            
            // Merge all items from states in this group
            java.util.Map<KernelEntry, java.util.Set<Symbol>> mergedItems = new java.util.HashMap<>();
            
            for (int lr1StateId : group) {
                lr1ToLalr.put(lr1StateId, lalrStateId);
                java.util.Set<LR1Item> state = lr1States.get(lr1StateId);
                
                for (LR1Item item : state) {
                    KernelEntry kernel = new KernelEntry(item.production, item.dotPosition);
                    mergedItems.computeIfAbsent(kernel, k -> new java.util.HashSet<>()).add(item.lookahead);
                }
            }
            
            // Create the merged LALR state
            java.util.Set<LR1Item> lalrState = new java.util.HashSet<>();
            for (java.util.Map.Entry<KernelEntry, java.util.Set<Symbol>> entry : mergedItems.entrySet()) {
                KernelEntry kernel = entry.getKey();
                for (Symbol lookahead : entry.getValue()) {
                    lalrState.add(new LR1Item(kernel.production, kernel.dotPosition, lookahead));
                }
            }
            
            lalrStates.add(lalrState);
            
            // Set the initial state for LALR
            if (isInitialStateGroup) {
                initialState = lalrStateId;
            }
            
            lalrStateId++;
        }
        
        // Step 3: Build transitions for LALR automaton
        for (java.util.Map.Entry<Integer, java.util.Map<Symbol, Integer>> entry : lr1Transitions.entrySet()) {
            int lr1From = entry.getKey();
            int lalrFrom = lr1ToLalr.get(lr1From);
            
            for (java.util.Map.Entry<Symbol, Integer> trans : entry.getValue().entrySet()) {
                Symbol symbol = trans.getKey();
                int lr1To = trans.getValue();
                int lalrTo = lr1ToLalr.get(lr1To);
                
                lalrTransitions.computeIfAbsent(lalrFrom, k -> new java.util.HashMap<>()).put(symbol, lalrTo);
            }
        }
        
        // Step 4: Fill ACTION and GOTO tables
        fillActionGoto();
    }

    private void fillActionGoto() {
        action.clear();
        gotoTable.clear();
        conflicts.clear();
        
        Symbol endOfInput = new Symbol("$", SymbolType.TERMINAL);
        String augmentedStartName = automaton.getAugmentedLeftName();
        
        // Iterate through each LALR state
        for (int s = 0; s < lalrStates.size(); s++) {
            java.util.Set<LR1Item> state = lalrStates.get(s);
            
            for (LR1Item item : state) {
                Symbol x = item.getSymbolAfterDot();
                
                if (x != null) {
                    // SHIFT action for terminals
                    if (x.type == SymbolType.TERMINAL) {
                        java.util.Map<Symbol, Integer> trans = lalrTransitions.get(s);
                        if (trans != null && trans.containsKey(x)) {
                            int t = trans.get(x);
                            
                            // Get or create the action map for this state
                            java.util.Map<Symbol, Action> stateActions = action.computeIfAbsent(s, k -> new java.util.HashMap<>());
                            
                            // Check if there's already an action for this symbol
                            Action existingAction = null;
                            for (java.util.Map.Entry<Symbol, Action> entry : stateActions.entrySet()) {
                                if (entry.getKey().name.equals(x.name)) {
                                    existingAction = entry.getValue();
                                    break;
                                }
                            }
                            
                            if (existingAction != null) {
                                conflicts.add("Shift/Reduce conflict at state " + s + " on symbol " + x.name);
                            } else {
                                stateActions.put(x, Action.shift(t));
                            }
                        }
                    }
                } else {
                    // Dot at end - REDUCE or ACCEPT
                    com.compiler.parser.grammar.Production prod = item.production;
                    
                    // Check for ACCEPT: S' -> S • with lookahead $
                    if (prod.left.name.equals(augmentedStartName) && item.lookahead.equals(endOfInput)) {
                        action.computeIfAbsent(s, k -> new java.util.HashMap<>()).put(endOfInput, Action.accept());
                    } else {
                        // REDUCE action
                        Symbol lookahead = item.lookahead;
                        
                        // Get or create the action map for this state
                        java.util.Map<Symbol, Action> stateActions = action.computeIfAbsent(s, k -> new java.util.HashMap<>());
                        
                        // Check if there's already an action for this lookahead
                        Action existingAction = null;
                        Symbol existingSymbol = null;
                        for (java.util.Map.Entry<Symbol, Action> entry : stateActions.entrySet()) {
                            if (entry.getKey().name.equals(lookahead.name)) {
                                existingAction = entry.getValue();
                                existingSymbol = entry.getKey();
                                break;
                            }
                        }
                        
                        if (existingAction != null) {
                            if (existingAction.type == Action.Type.SHIFT) {
                                conflicts.add("Shift/Reduce conflict at state " + s + " on symbol " + lookahead.name);
                            } else {
                                conflicts.add("Reduce/Reduce conflict at state " + s + " on symbol " + lookahead.name);
                            }
                        } else {
                            stateActions.put(lookahead, Action.reduce(prod));
                        }
                    }
                }
            }
        }
        
        // Populate GOTO table
        for (int s = 0; s < lalrStates.size(); s++) {
            java.util.Map<Symbol, Integer> trans = lalrTransitions.get(s);
            if (trans != null) {
                for (java.util.Map.Entry<Symbol, Integer> entry : trans.entrySet()) {
                    Symbol symbol = entry.getKey();
                    int t = entry.getValue();
                    
                    if (symbol.type == SymbolType.NON_TERMINAL) {
                        gotoTable.computeIfAbsent(s, k -> new java.util.HashMap<>()).put(symbol, t);
                    }
                }
            }
        }
    }
    
    // ... (Getters and KernelEntry class can remain as is)
    public java.util.Map<Integer, java.util.Map<Symbol, Action>> getActionTable() { return action; }
    public java.util.Map<Integer, java.util.Map<Symbol, Integer>> getGotoTable() { return gotoTable; }
    public java.util.List<String> getConflicts() { return conflicts; }
    private static class KernelEntry {
        public final com.compiler.parser.grammar.Production production;
        public final int dotPosition;
        KernelEntry(com.compiler.parser.grammar.Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof KernelEntry)) return false;
            KernelEntry o = (KernelEntry) obj;
            return dotPosition == o.dotPosition && production.equals(o.production);
        }
        @Override
        public int hashCode() {
            int r = production.hashCode();
            r = 31 * r + dotPosition;
            return r;
        }
    }
    public java.util.List<java.util.Set<LR1Item>> getLALRStates() { return lalrStates; }
    public java.util.Map<Integer, java.util.Map<Symbol, Integer>> getLALRTransitions() { return lalrTransitions; }
    public int getInitialState() { return initialState; }
}