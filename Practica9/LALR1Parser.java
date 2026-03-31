package com.compiler.parser.lr;

import java.util.List;

import com.compiler.lexer.Token;
import com.compiler.parser.grammar.Symbol;

/**
 * Implements the LALR(1) parsing engine.
 * Uses a stack and the LALR(1) table to process a sequence of tokens.
 * Complementary task for Practice 9.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

   // package-private accessor for tests
   LALR1Table getTable() {
       return table;
   }

   /**
    * Parses a sequence of tokens using the LALR(1) parsing algorithm.
    * @param tokens The list of tokens from the lexer.
    * @return true if the sequence is accepted, false if a syntax error is found.
    */
   public boolean parse(List<Token> tokens) {
        // 1. Initialize stack with initial state
        java.util.Stack<Integer> stack = new java.util.Stack<>();
        stack.push(table.getInitialState());
        
        // 2. Create mutable list of tokens and add end-of-input
        List<Token> input = new java.util.ArrayList<>(tokens);
        Token endToken = new Token("$", "$");
        input.add(endToken);
        
        // 3. Initialize instruction pointer
        int ip = 0;
        
        // Get action and goto tables
        java.util.Map<Integer, java.util.Map<Symbol, LALR1Table.Action>> actionTable = table.getActionTable();
        java.util.Map<Integer, java.util.Map<Symbol, Integer>> gotoTable = table.getGotoTable();
        
        // DEBUG: Print GOTO table
        /*System.out.println("\n=== GOTO Table ===");
        for (var entry : gotoTable.entrySet()) {
            System.out.println("State " + entry.getKey() + ":");
            for (var kv : entry.getValue().entrySet()) {
                System.out.println("  on " + kv.getKey().name + " -> " + kv.getValue());
            }
        }*/
        
        // 4. Main parsing loop
        while (true) {
            // a. Get current state from top of stack
            int state = stack.peek();
            
            // b. Get current token
            Token a = input.get(ip);
            
            // DEBUG
            //System.out.println("\nState: " + state + ", Token: " + a.type + ", Stack: " + stack);
            
            // c. Look up action - need to find matching symbol in action table
            java.util.Map<Symbol, LALR1Table.Action> actionsForState = actionTable.get(state);
            
            // d. Check if action exists
            if (actionsForState == null) {
                return false;
            }
            
            // Find the action by matching symbol name
            LALR1Table.Action action = null;
            for (java.util.Map.Entry<Symbol, LALR1Table.Action> entry : actionsForState.entrySet()) {
                if (entry.getKey().name.equals(a.type)) {
                    action = entry.getValue();
                    break;
                }
            }
            
            if (action == null) {
                //System.out.println("ERROR: No action found for state " + state + " on symbol " + a.type);
                return false;
            }
            
            //System.out.println("Action: " + action.type);
            
            // e. SHIFT action
            if (action.type == LALR1Table.Action.Type.SHIFT) {
                stack.push(action.state);
                ip++;
            }
            // f. REDUCE action
            else if (action.type == LALR1Table.Action.Type.REDUCE) {
                com.compiler.parser.grammar.Production prod = action.reduceProd;
                
                // Check if it's epsilon production
                boolean isEpsilon = prod.right.size() == 1 && prod.right.get(0).name.equals("ε");
                int beta = isEpsilon ? 0 : prod.right.size();
                
                // i. Pop |beta| states from stack
                for (int i = 0; i < beta; i++) {
                    if (!stack.isEmpty()) {
                        stack.pop();
                    }
                }
                
                // ii. Get new state from top of stack
                if (stack.isEmpty()) {
                    //System.out.println("ERROR: Stack is empty after reduce");
                    return false;
                }
                int s = stack.peek();
                
                //System.out.println("After reduce, top state: " + s + ", Looking for GOTO on: " + prod.left.name);
                
                // iii. Look up GOTO state
                java.util.Map<Symbol, Integer> gotoForState = gotoTable.get(s);
                
                if (gotoForState == null) {
                    //System.out.println("ERROR: No GOTO entries for state " + s);
                    return false;
                }
                
                // Find GOTO by matching symbol name
                Integer gotoState = null;
                for (java.util.Map.Entry<Symbol, Integer> entry : gotoForState.entrySet()) {
                    if (entry.getKey().name.equals(prod.left.name)) {
                        gotoState = entry.getValue();
                        break;
                    }
                }
                
                // iv. Check if GOTO state exists
                if (gotoState == null) {
                    //System.out.println("ERROR: No GOTO found for " + prod.left.name + " in state " + s);
                    //System.out.println("Available GOTOs:");
                    for (var e : gotoForState.entrySet()) {
                        System.out.println("  " + e.getKey().name + " -> " + e.getValue());
                    }
                    return false;
                }
                
                //System.out.println("GOTO state: " + gotoState);
                
                // v. Push goto state
                stack.push(gotoState);
            }
            // g. ACCEPT action
            else if (action.type == LALR1Table.Action.Type.ACCEPT) {
                return true;
            }
            // h. Unhandled case
            else {
                return false;
            }
        }
   }
}