package com.compiler.lexer;

import java.util.Map;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

/**
 * Constructor de analizadores léxicos con capacidad de manejar múltiples reglas.
 */
public class TokenizerBuilder {
    private final RegexParser regexParser;
    
    public TokenizerBuilder() {
        this.regexParser = new RegexParser();
    }
    
    /**
     * Genera un tokenizer a partir de patrones regex y tipos de token.
     */
    public Tokenizer buildTokenizer(Map<String, String> tokenRules, Set<Character> alphabet) {
        Tokenizer lexicalAnalyzer = new Tokenizer();
        int rulePriority = 1000; // Iniciar con prioridad alta
        
        for (Map.Entry<String, String> ruleEntry : tokenRules.entrySet()) {
            String typeOfToken = ruleEntry.getKey();
            String regexPattern = ruleEntry.getValue();
            
            try {
                // Convertir regex a NFA
                NFA nonDeterministicAutomaton = regexParser.parse(regexPattern);
                
                // Transformar NFA a DFA (usando método estático)
                DFA deterministicAutomaton = NfaToDfaConverter.convertNfaToDfa(nonDeterministicAutomaton, alphabet);
                
                // Minimizar DFA (usando método estático)
                DFA optimizedDfa = DfaMinimizer.minimizeDfa(deterministicAutomaton, alphabet);
                
                // Agregar regla al tokenizer
                lexicalAnalyzer.addRule(optimizedDfa, typeOfToken, rulePriority--);
                
            } catch (Exception processingError) {
                throw new RuntimeException("Error processing token rule for " + typeOfToken + 
                                         " with regex: " + regexPattern, processingError);
            }
        }
        
        return lexicalAnalyzer;
    }
}