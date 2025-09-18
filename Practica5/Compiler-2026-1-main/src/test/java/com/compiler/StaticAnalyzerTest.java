package com.compiler;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;
import com.compiler.parser.syntax.StaticAnalyzer;

public class StaticAnalyzerTest {
    // Helper symbols for tests
    private static final Symbol EPSILON = new Symbol("ε", SymbolType.TERMINAL);
    private static final Symbol END = new Symbol("$", SymbolType.TERMINAL);

    @Test
    public void testFirstSetsSimpleGrammar() {
        // Grammar: S -> a S | b
        String grammarDef = "S -> a S | b";
        Grammar grammar = new Grammar(grammarDef);
        Symbol S = grammar.getStartSymbol();
        Symbol a = grammar.getTerminals().stream().filter(sym -> sym.name.equals("a")).findFirst().orElse(null);
        Symbol b = grammar.getTerminals().stream().filter(sym -> sym.name.equals("b")).findFirst().orElse(null);
        StaticAnalyzer analyzer = new StaticAnalyzer(grammar);
        Map<Symbol, Set<Symbol>> firstSets = null;
        try {
            firstSets = analyzer.getFirstSets();
        } catch (UnsupportedOperationException e) {
            throw new AssertionError("getFirstSets not implemented yet");
        }
        assertNotNull(firstSets);
        assertTrue(firstSets.get(S).contains(a));
        assertTrue(firstSets.get(S).contains(b));
    }

    @Test
    public void testFollowSetsSimpleGrammar() {
        // Grammar: S -> a S | b
        String grammarDef = "S -> a S | b";
        Grammar grammar = new Grammar(grammarDef);
        Symbol S = grammar.getStartSymbol();
        StaticAnalyzer analyzer = new StaticAnalyzer(grammar);
        Map<Symbol, Set<Symbol>> followSets = null;
        try {
            followSets = analyzer.getFollowSets();
        } catch (UnsupportedOperationException e) {
            throw new AssertionError("getFollowSets not implemented yet");
        }
        assertNotNull(followSets);
        assertTrue(followSets.get(S).contains(END));
    }

    @Test
    public void testFirstSetsEpsilonProduction() {
        // Grammar: S -> ε | a
        String grammarDef = "S -> ε | a";
        Grammar grammar = new Grammar(grammarDef);
        Symbol S = grammar.getStartSymbol();
        Symbol a = grammar.getTerminals().stream().filter(sym -> sym.name.equals("a")).findFirst().orElse(null);
        StaticAnalyzer analyzer = new StaticAnalyzer(grammar);
        Map<Symbol, Set<Symbol>> firstSets = null;
        try {
            firstSets = analyzer.getFirstSets();
        } catch (UnsupportedOperationException e) {
            throw new AssertionError("getFirstSets not implemented yet");
        }
        assertNotNull(firstSets);
        assertTrue(firstSets.get(S).contains(a));
        assertTrue(firstSets.get(S).contains(EPSILON));
    }

    @Test
    public void testFollowSetsMultipleNonTerminals() {
        // Grammar: S -> A B, A -> a, B -> b
        String grammarDef = "S -> A B\nA -> a\nB -> b";
        Grammar grammar = new Grammar(grammarDef);
        @SuppressWarnings("unused")
        Symbol S = grammar.getStartSymbol();
        Symbol A = grammar.getNonTerminals().stream().filter(sym -> sym.name.equals("A")).findFirst().orElse(null);
        Symbol B = grammar.getNonTerminals().stream().filter(sym -> sym.name.equals("B")).findFirst().orElse(null);
        Symbol b = grammar.getTerminals().stream().filter(sym -> sym.name.equals("b")).findFirst().orElse(null);
        StaticAnalyzer analyzer = new StaticAnalyzer(grammar);
        Map<Symbol, Set<Symbol>> followSets = null;
        try {
            followSets = analyzer.getFollowSets();
        } catch (UnsupportedOperationException e) {
            throw new AssertionError("getFollowSets not implemented yet");
        }
        assertNotNull(followSets);
        assertTrue(followSets.get(A).contains(b));
        assertTrue(followSets.get(B).contains(END));
    }
}
