package com.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.compiler.lexer.NfaSimulator;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

public class RegexTest {

    @ParameterizedTest
    @CsvSource({
        // a+
        "a,      true",
        "aa,     true",
        "aaaa,   true",
        "'',     false",
        "b,      false"
    })
    void testPlusOperator(String input, boolean expected) {
        String regex = "a+";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
        // DFA
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // a?
        "'',     true",
        "a,      true",
        "aa,     false",
        "b,      false"
    })
    void testOptionalOperator(String input, boolean expected) {
        String regex = "a?";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // a|b
        "a,      true",
        "b,      true",
        "'',     false",
        "c,      false",
        "ab,     false"
    })
    void testUnionOperator(String input, boolean expected) {
        String regex = "a|b";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // ab|c
        "ab,     true",
        "c,      true",
        "a,      false",
        "b,      false",
        "ac,     false"
    })
    void testPrecedenceConcatOverUnion(String input, boolean expected) {
        String regex = "ab|c";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // alphabet.add('c');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // ab*c
        "ac,     true",
        "abc,    true",
        "abbbc,  true",
        "a,      false",
        "c,      false",
        "ab,     false",
        "bc,     false"
    })
    void testPrecedenceKleeneOverConcat(String input, boolean expected) {
        String regex = "ab*c";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // alphabet.add('c');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // (a|b)*
        "'',     true",
        "a,      true",
        "b,      true",
        "aa,     true",
        "bb,     true",
        "ab,     true",
        "ba,     true",
        "bababa, true",
        "c,      false",
        "ac,     false",
        "bc,     false"
    })
    void testGroupingKleene(String input, boolean expected) {
        String regex = "(a|b)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // a(b|c)d
        "abd,    true",
        "acd,    true",
        "ad,     false",
        "abcd,   false",
        "'abd d',false"
    })
    void testConcatWithGroupUnion(String input, boolean expected) {
        String regex = "a(b|c)d";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // alphabet.add('c');
    // alphabet.add('d');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // a(b*|c+)?
        "ad,     true",
        "abd,    true",
        "abbbd,  true",
        "acd,    true",
        "acccd,  true",
        "'a c d',false",
        "abcd,   false",
        "abbc,   false"
    })
    void testComplexNesting(String input, boolean expected) {
        String regex = "a(b*|c+)?d";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // alphabet.add('c');
    // alphabet.add('d');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // (a*)*
        "'',     true",
        "a,      true",
        "aa,     true",
        "b,      false"
    })
    void testNestedKleene(String input, boolean expected) {
        String regex = "(a*)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // a(b?)c
        "ac,     true",
        "abc,    true",
        "a,      false",
        "c,      false",
        "ab,     false",
        "bc,     false"
    })
    void testOptionalInsideConcat(String input, boolean expected) {
        String regex = "a(b?)c";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // alphabet.add('c');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }

    @ParameterizedTest
    @CsvSource({
        // (a|b)*a(a|b)*
        "a,      true",
        "aa,     true",
        "ab,     true",
        "ba,     true",
        "bab,    true",
        "bbaabb, true",
        "'',     false",
        "b,      false",
        "bb,     false",
        "bbbb,   false"
    })
    void testContainsAtLeastOneA(String input, boolean expected) {
        String regex = "(a|b)*a(a|b)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);
    // Set<Character> alphabet = new HashSet<>();
    // alphabet.add('a');
    // alphabet.add('b');
    // DFA phase commented out for now
    // DFA dfa = com.compiler.lexer.NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
    // com.compiler.lexer.DfaSimulator dfaSimulator = new com.compiler.lexer.DfaSimulator();
    // boolean actualDfa = dfaSimulator.simulate(dfa, input);
    assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
    // assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
    }
}
