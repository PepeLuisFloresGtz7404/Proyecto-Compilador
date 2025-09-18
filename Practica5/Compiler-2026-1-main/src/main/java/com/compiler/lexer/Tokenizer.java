package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * Analizador léxico que utiliza múltiples autómatas finitos determinísticos
 * para reconocer diferentes tipos de tokens.
 * Aplica estrategia de coincidencia máxima (longest match).
 */
public class Tokenizer {
    
    /**
     * Clase que representa un token identificado con su tipo, valor y posición.
     */
    public static class Token {
        public final String type;
        public final String value;
        public final int position;
        
        public Token(String type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }
        
        @Override
        public String toString() {
            return String.format("Token{type='%s', value='%s', pos=%d}", type, value, position);
        }
    }
    
    /**
     * Estructura que asocia un DFA con un tipo de token específico.
     */
    public static class TokenRule {
        public final DFA dfa;
        public final String tokenType;
        public final int priority;
        
        public TokenRule(DFA dfa, String tokenType, int priority) {
            this.dfa = dfa;
            this.tokenType = tokenType;
            this.priority = priority;
        }
    }
    
    private final List<TokenRule> rules;
    
    public Tokenizer() {
        this.rules = new ArrayList<>();
    }
    
    /**
     * Incorpora una nueva regla de tokenización al analizador.
     */
    public void addRule(DFA dfa, String tokenType, int priority) {
        rules.add(new TokenRule(dfa, tokenType, priority));
        // Ordenamiento por prioridad (mayor prioridad primero)
        rules.sort((regla1, regla2) -> Integer.compare(regla2.priority, regla1.priority));
    }
    
    /**
     * Procesa la cadena de entrada aplicando estrategia de coincidencia máxima.
     */
    public List<Token> tokenize(String input) {
        List<Token> resultTokens = new ArrayList<>();
        int currentPosition = 0;
        
        while (currentPosition < input.length()) {
            TokenMatch longestMatch = findLongestMatch(input, currentPosition);
            
            if (longestMatch != null) {
                resultTokens.add(new Token(longestMatch.tokenType, longestMatch.value, currentPosition));
                currentPosition += longestMatch.length;
            } else {
                // Sin coincidencia - manejo de error o saltar carácter
                throw new RuntimeException("No valid token found at position " + currentPosition + 
                                         " for character: '" + input.charAt(currentPosition) + "'");
            }
        }
        
        return resultTokens;
    }
    
    /**
     * Busca la coincidencia más larga posible en la posición actual.
     */
    private TokenMatch findLongestMatch(String input, int startPos) {
        TokenMatch optimalMatch = null;
        
        for (TokenRule currentRule : rules) {
            TokenMatch matchResult = tryMatch(currentRule, input, startPos);
            
            if (matchResult != null) {
                // Preferir coincidencias más largas, o mayor prioridad si igual longitud
                if (optimalMatch == null || 
                    matchResult.length > optimalMatch.length ||
                    (matchResult.length == optimalMatch.length && matchResult.priority > optimalMatch.priority)) {
                    optimalMatch = matchResult;
                }
            }
        }
        
        return optimalMatch;
    }
    
    /**
     * Intenta hacer coincidir una regla específica en la posición dada.
     */
    private TokenMatch tryMatch(TokenRule rule, String input, int startPos) {
        int longestAcceptedLength = 0;
        int scanPosition = startPos;
        
        // Simular el DFA carácter por carácter
        DfaState activeState = rule.dfa.startState;
        
        while (scanPosition < input.length() && activeState != null) {
            char inputChar = input.charAt(scanPosition);
            activeState = activeState.transitions.get(inputChar);
            
            if (activeState != null) {
                scanPosition++;
                if (activeState.isFinal) {
                    longestAcceptedLength = scanPosition - startPos;
                }
            }
        }
        
        if (longestAcceptedLength > 0) {
            String matchedValue = input.substring(startPos, startPos + longestAcceptedLength);
            return new TokenMatch(rule.tokenType, matchedValue, longestAcceptedLength, rule.priority);
        }
        
        return null;
    }
    
    /**
     * Clase auxiliar para almacenar resultados de coincidencia de tokens.
     */
    private static class TokenMatch {
        final String tokenType;
        final String value;
        final int length;
        final int priority;
        
        TokenMatch(String tokenType, String value, int length, int priority) {
            this.tokenType = tokenType;
            this.value = value;
            this.length = length;
            this.priority = priority;
        }
    }
}