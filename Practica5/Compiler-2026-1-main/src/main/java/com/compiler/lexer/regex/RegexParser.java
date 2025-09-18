package com.compiler.lexer.regex;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;
import java.util.Stack;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
    public RegexParser() {
        // Constructor doesn't need specific implementation
    }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
        // Convert infix to postfix using Shunting-Yard algorithm
        String postfixRegex = ShuntingYard.toPostfix(infixRegex);
        
        // Build NFA from postfix expression
        return buildNfaFromPostfix(postfixRegex);
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
        Stack<NFA> nfaStack = new Stack<>();
        
        for (char c : postfixRegex.toCharArray()) {
            if (isOperand(c)) {
                // If operand, create basic NFA and push to stack
                NFA basicNfa = createNfaForCharacter(c);
                nfaStack.push(basicNfa);
            } else {
                // If operator, apply the corresponding operation
                switch (c) {
                    case '·':
                        handleConcatenation(nfaStack);
                        break;
                    case '|':
                        handleUnion(nfaStack);
                        break;
                    case '*':
                        handleKleeneStar(nfaStack);
                        break;
                    case '+':
                        handlePlus(nfaStack);
                        break;
                    case '?':
                        handleOptional(nfaStack);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }
        
        // The final NFA should be the only element left in the stack
        if (nfaStack.size() != 1) {
            throw new IllegalStateException("Invalid postfix expression");
        }
        
        return nfaStack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty for optional operation");
        }
        
        NFA nfa = stack.pop();
        
        // Create new start and end states
        State newStart = new State();
        State newEnd = new State();
        
        // Add epsilon transition from new start to old start (for one occurrence)
        newStart.transitions.add(new Transition(null, nfa.startState));
        
        // Add epsilon transition from new start to new end (for zero occurrences)
        newStart.transitions.add(new Transition(null, newEnd));
        
        // Add epsilon transition from old end to new end
        nfa.endState.transitions.add(new Transition(null, newEnd));
        nfa.endState.isFinal = false;
        
        stack.push(new NFA(newStart, newEnd));
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty for plus operation");
        }
        
        NFA nfa = stack.pop();
        
        // Create new start and end states
        State newStart = new State();
        State newEnd = new State();
        
        // Add epsilon transition from new start to old start (for first occurrence)
        newStart.transitions.add(new Transition(null, nfa.startState));
        
        // Add epsilon transition from old end to new end (to finish)
        nfa.endState.transitions.add(new Transition(null, newEnd));
        
        // Add epsilon transition from old end to old start (for repetition)
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.isFinal = false;
        
        stack.push(new NFA(newStart, newEnd));
    }
    
    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
        State start = new State();
        State end = new State();
        
        // Add transition from start to end with the character
        start.transitions.add(new Transition(c, end));
        
        return new NFA(start, end);
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
        if (stack.size() < 2) {
            throw new IllegalStateException("Not enough NFAs for concatenation");
        }
        
        NFA second = stack.pop();
        NFA first = stack.pop();
        
        // Connect the end of first NFA to the start of second NFA with epsilon transition
        first.endState.transitions.add(new Transition(null, second.startState));
        first.endState.isFinal = false;
        
        // The result is first's start state and second's end state
        stack.push(new NFA(first.startState, second.endState));
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        if (stack.size() < 2) {
            throw new IllegalStateException("Not enough NFAs for union");
        }
        
        NFA second = stack.pop();
        NFA first = stack.pop();
        
        // Create new start and end states
        State newStart = new State();
        State newEnd = new State();
        
        // Add epsilon transitions from new start to both NFAs' start states
        newStart.transitions.add(new Transition(null, first.startState));
        newStart.transitions.add(new Transition(null, second.startState));
        
        // Add epsilon transitions from both NFAs' end states to new end state
        first.endState.transitions.add(new Transition(null, newEnd));
        second.endState.transitions.add(new Transition(null, newEnd));
        
        // Mark old end states as non-final
        first.endState.isFinal = false;
        second.endState.isFinal = false;
        
        stack.push(new NFA(newStart, newEnd));
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty for Kleene star operation");
        }
        
        NFA nfa = stack.pop();
        
        // Create new start and end states
        State newStart = new State();
        State newEnd = new State();
        
        // Add epsilon transition from new start to old start (for one or more occurrences)
        newStart.transitions.add(new Transition(null, nfa.startState));
        
        // Add epsilon transition from new start to new end (for zero occurrences)
        newStart.transitions.add(new Transition(null, newEnd));
        
        // Add epsilon transition from old end to old start (for repetition)
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        
        // Add epsilon transition from old end to new end (to finish)
        nfa.endState.transitions.add(new Transition(null, newEnd));
        
        nfa.endState.isFinal = false;
        
        stack.push(new NFA(newStart, newEnd));
    }

    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
        return c != '|' && c != '*' && c != '?' && c != '+' && 
               c != '(' && c != ')' && c != '·';
    }
}