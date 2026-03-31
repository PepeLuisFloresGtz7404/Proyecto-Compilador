package com.compiler;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.compiler.lexer.DfaMinimizer;
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

public class DfaMinimizationTest {
    @Test
    public void testMinimization_abd() {
        String regex = "a(b*|c+)?d";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        Set<Character> alphabet = new HashSet<>();
        alphabet.add('a');
        alphabet.add('b');
        alphabet.add('c');
        alphabet.add('d');
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        DFA minimized = DfaMinimizer.minimizeDfa(dfa, alphabet);
        DfaSimulator dfaSimulator = new DfaSimulator();
        assertTrue(dfaSimulator.simulate(minimized, "abd"), "Minimized DFA should accept 'abd'");
        assertTrue(dfaSimulator.simulate(minimized, "acd"), "Minimized DFA should accept 'acd'");
        assertTrue(dfaSimulator.simulate(minimized, "abbbd"), "Minimized DFA should accept 'abbbd'");
        assertTrue(dfaSimulator.simulate(minimized, "acccd"), "Minimized DFA should accept 'acccd'");
        assertTrue(dfaSimulator.simulate(minimized, "ad"), "Minimized DFA should accept 'ad'");
        assertFalse(dfaSimulator.simulate(minimized, "a"), "Minimized DFA should not accept 'a'");
        assertFalse(dfaSimulator.simulate(minimized, "d"), "Minimized DFA should not accept 'd'");
    }
}
