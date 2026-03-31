package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // Constructor doesn't need specific implementation
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        if (regex == null || regex.isEmpty()) {
            return regex;
        }
        
        StringBuilder output = new StringBuilder();
        
        for (int i = 0; i < regex.length(); i++) {
            char current = regex.charAt(i);
            output.append(current);
            
            // Check if we need to insert concatenation operator
            if (i < regex.length() - 1) {
                char next = regex.charAt(i + 1);
                
                // Insert concatenation if:
                // - current is operand or ')' or '*' or '+' or '?'
                // - AND next is operand or '('
                boolean needsConcatenation = false;
                
                if ((isOperand(current) || current == ')' || current == '*' || 
                     current == '+' || current == '?') &&
                    (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                }
                
                if (needsConcatenation) {
                    output.append('·');
                }
            }
        }
        
        return output.toString();
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        return c != '|' && c != '*' && c != '?' && c != '+' && 
               c != '(' && c != ')' && c != '·';
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // Define operator precedence (higher number = higher precedence)
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1);  // Union has lowest precedence
        precedence.put('·', 2);  // Concatenation has medium precedence
        precedence.put('*', 3);  // Kleene star has highest precedence
        precedence.put('+', 3);  // Plus has highest precedence
        precedence.put('?', 3);  // Optional has highest precedence
        
        // Preprocess to insert explicit concatenation operators
        String preprocessed = insertConcatenationOperator(infixRegex);
        
        StringBuilder output = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();
        
        for (char c : preprocessed.toCharArray()) {
            if (isOperand(c)) {
                // If operand, add to output
                output.append(c);
            } else if (c == '(') {
                // If left parenthesis, push to stack
                operatorStack.push(c);
            } else if (c == ')') {
                // If right parenthesis, pop operators until left parenthesis
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    output.append(operatorStack.pop());
                }
                // Remove the left parenthesis
                if (!operatorStack.isEmpty()) {
                    operatorStack.pop();
                }
            } else if (precedence.containsKey(c)) {
                // If operator, pop operators with higher or equal precedence
                while (!operatorStack.isEmpty() && 
                       operatorStack.peek() != '(' &&
                       precedence.containsKey(operatorStack.peek()) &&
                       precedence.get(operatorStack.peek()) >= precedence.get(c)) {
                    output.append(operatorStack.pop());
                }
                operatorStack.push(c);
            }
        }
        
        // Pop remaining operators
        while (!operatorStack.isEmpty()) {
            output.append(operatorStack.pop());
        }
        
        return output.toString();
    }
}