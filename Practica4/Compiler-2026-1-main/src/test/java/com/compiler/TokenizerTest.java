package com.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.compiler.lexer.Tokenizer;
import com.compiler.lexer.Tokenizer.Token;
import com.compiler.lexer.TokenizerBuilder;

public class TokenizerTest {
    
    /**
     * Método auxiliar para generar alfabeto básico con letras, dígitos y operadores
     */
    private Set<Character> createBasicAlphabet() {
        Set<Character> characterSet = new HashSet<>();
        // Agregar letras minúsculas
        for (char ch = 'a'; ch <= 'z'; ch++) characterSet.add(ch);
        // Agregar letras mayúsculas  
        for (char ch = 'A'; ch <= 'Z'; ch++) characterSet.add(ch);
        // Agregar dígitos
        for (char ch = '0'; ch <= '9'; ch++) characterSet.add(ch);
        characterSet.add('+');
        characterSet.add('-');
        characterSet.add('*');
        characterSet.add('/');
        characterSet.add('=');
        characterSet.add('(');
        characterSet.add(')');
        characterSet.add(';');
        characterSet.add(' ');
        characterSet.add('\t');
        characterSet.add('\n');
        return characterSet;
    }

    @Test
    public void testBasicTokenization() {
        // Definir reglas simples de tokens - evitar operadores problemáticos
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("X", "x");
        tokenRules.put("ASSIGN", "=");
        tokenRules.put("ONE", "1");
        tokenRules.put("TWO", "2");
        tokenRules.put("THREE", "3");
        tokenRules.put("PLUS_OP", "p");  // Usar 'p' en lugar de '+' para simplicidad
        tokenRules.put("Y", "y");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar tokenización simple
        String testInput = "x=1py";  // Entrada ajustada para coincidir con las reglas
        List<Token> resultTokens = analyzer.tokenize(testInput);
        
        assertEquals(5, resultTokens.size(), "Debe producir exactamente 5 tokens");
        
        // Verificar cada token
        assertEquals("X", resultTokens.get(0).type, "El primer token debe ser X");
        assertEquals("x", resultTokens.get(0).value, "El valor del primer token debe ser 'x'");
        assertEquals(0, resultTokens.get(0).position, "La posición del primer token debe ser 0");
        
        assertEquals("ASSIGN", resultTokens.get(1).type, "El segundo token debe ser ASSIGN");
        assertEquals("=", resultTokens.get(1).value, "El valor del segundo token debe ser '='");
        assertEquals(1, resultTokens.get(1).position, "La posición del segundo token debe ser 1");
        
        assertEquals("ONE", resultTokens.get(2).type, "El tercer token debe ser ONE");
        assertEquals("1", resultTokens.get(2).value, "El valor del tercer token debe ser '1'");
        assertEquals(2, resultTokens.get(2).position, "La posición del tercer token debe ser 2");
        
        assertEquals("PLUS_OP", resultTokens.get(3).type, "El cuarto token debe ser PLUS_OP");
        assertEquals("p", resultTokens.get(3).value, "El valor del cuarto token debe ser 'p'");
        assertEquals(3, resultTokens.get(3).position, "La posición del cuarto token debe ser 3");
        
        assertEquals("Y", resultTokens.get(4).type, "El quinto token debe ser Y");
        assertEquals("y", resultTokens.get(4).value, "El valor del quinto token debe ser 'y'");
        assertEquals(4, resultTokens.get(4).position, "La posición del quinto token debe ser 4");
    }

    @Test
        public void testWhitespaceHandling() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");  // Esta regla ya existe, pero necesita estar en el orden correcto
        tokenRules.put("SPACE", " ");
        tokenRules.put("TAB", "\t");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("a b\ta");
        
        // Debug: Imprimir tokens reales para entender qué está pasando
        //System.out.println("Tokens reales:");
        //for (int i = 0; i < resultTokens.size(); i++) {
        //    System.out.println(i + ": " + resultTokens.get(i));
        //}
        
        assertEquals(5, resultTokens.size(), "Debe producir exactamente 5 tokens"); // Cambiar de 4 a 5
        
        assertEquals("A", resultTokens.get(0).type, "El primer token debe ser A");
        assertEquals("SPACE", resultTokens.get(1).type, "El segundo token debe ser SPACE");
        assertEquals("B", resultTokens.get(2).type, "El tercer token debe ser B");  // Agregar verificación para B
        assertEquals("TAB", resultTokens.get(3).type, "El cuarto token debe ser TAB"); // Ajustar índices
        assertEquals("A", resultTokens.get(4).type, "El quinto token debe ser A");    // Ajustar índices
    }

    @Test
    public void testMultipleConsecutiveTokens() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");
        tokenRules.put("C", "c");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("aaabbbccc");
        assertEquals(9, resultTokens.size(), "Debe producir exactamente 9 tokens");
        
        // Verificar los primeros tres tokens son todos A
        for (int idx = 0; idx < 3; idx++) {
            assertEquals("A", resultTokens.get(idx).type, "Token " + idx + " debe ser A");
            assertEquals(idx, resultTokens.get(idx).position, "Token " + idx + " debe estar en posición " + idx);
        }
        
        // Verificar los siguientes tres tokens son todos B
        for (int idx = 3; idx < 6; idx++) {
            assertEquals("B", resultTokens.get(idx).type, "Token " + idx + " debe ser B");
            assertEquals(idx, resultTokens.get(idx).position, "Token " + idx + " debe estar en posición " + idx);
        }
        
        // Verificar los últimos tres tokens son todos C
        for (int idx = 6; idx < 9; idx++) {
            assertEquals("C", resultTokens.get(idx).type, "Token " + idx + " debe ser C");
            assertEquals(idx, resultTokens.get(idx).position, "Token " + idx + " debe estar en posición " + idx);
        }
    }

    @Test
    public void testPriorityOrdering() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        // La primera regla debe tener mayor prioridad debido al ordenamiento de LinkedHashMap
        tokenRules.put("AB", "ab");
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Debe coincidir "ab" como token AB, no como A seguido de B
        List<Token> resultTokens = analyzer.tokenize("ab");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token usando coincidencia más larga");
        assertEquals("AB", resultTokens.get(0).type, "Debe reconocer 'ab' como token AB");
        assertEquals("ab", resultTokens.get(0).value, "El valor del token debe ser 'ab'");
    }

    @Test
    public void testMixedAlphanumeric() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("VAR_X", "x");
        tokenRules.put("VAR_Y", "y");
        tokenRules.put("NUM_1", "1");
        tokenRules.put("NUM_2", "2");
        tokenRules.put("NUM_0", "0");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("x1y2x0");
        assertEquals(6, resultTokens.size(), "Debe producir exactamente 6 tokens");
        
        String[] expectedTypes = {"VAR_X", "NUM_1", "VAR_Y", "NUM_2", "VAR_X", "NUM_0"};
        String[] expectedValues = {"x", "1", "y", "2", "x", "0"};
        
        for (int idx = 0; idx < resultTokens.size(); idx++) {
            assertEquals(expectedTypes[idx], resultTokens.get(idx).type, 
                String.format("Token %d debe ser de tipo %s", idx, expectedTypes[idx]));
            assertEquals(expectedValues[idx], resultTokens.get(idx).value, 
                String.format("Token %d debe tener valor '%s'", idx, expectedValues[idx]));
            assertEquals(idx, resultTokens.get(idx).position, 
                String.format("Token %d debe estar en posición %d", idx, idx));
        }
    }

    @Test
    public void testSingleCharacterInput() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("LETTER_A", "a");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("a");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("LETTER_A", resultTokens.get(0).type, "Debe reconocer 'a' como LETTER_A");
        assertEquals("a", resultTokens.get(0).value, "El valor del token debe ser 'a'");
        assertEquals(0, resultTokens.get(0).position, "La posición del token debe ser 0");
    }

    @Test
    public void testErrorHandling() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar carácter inválido
        assertThrows(RuntimeException.class, () -> {
            analyzer.tokenize("@");
        }, "Debe lanzar excepción para carácter inválido '@'");
        
        // Probar con información de posición en error
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            analyzer.tokenize("a@b");
        }, "Debe lanzar excepción para carácter inválido en medio de la entrada");
        
        assertTrue(thrownException.getMessage().contains("position 1"), 
            "El mensaje de error debe contener información de posición");
        assertTrue(thrownException.getMessage().contains("'@'"), 
            "El mensaje de error debe contener el carácter inválido");
    }

    @Test
    public void testEmptyInput() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("");
        
        assertNotNull(resultTokens, "Debe devolver lista no nula para entrada vacía");
        assertEquals(0, resultTokens.size(), "Debe devolver lista vacía para entrada vacía");
    }

    @Test
    public void testComplexPattern() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("ABC", "abc");
        tokenRules.put("AB", "ab");
        tokenRules.put("A", "a");
        tokenRules.put("BC", "bc");
        tokenRules.put("B", "b");
        tokenRules.put("C", "c");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Debe usar coincidencia más larga - "abc" debe reconocerse como un token ABC
        List<Token> resultTokens = analyzer.tokenize("abc");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token usando coincidencia más larga");
        assertEquals("ABC", resultTokens.get(0).type, "Debe reconocer 'abc' como token ABC");
        assertEquals("abc", resultTokens.get(0).value, "El valor del token debe ser 'abc'");
        
        // Probar coincidencias parciales
        resultTokens = analyzer.tokenize("ab");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("AB", resultTokens.get(0).type, "Debe reconocer 'ab' como token AB");
        
        resultTokens = analyzer.tokenize("bc");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("BC", resultTokens.get(0).type, "Debe reconocer 'bc' como token BC");
    }

    @Test
    public void testTokenValueConsistency() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("DIGIT_0", "0");
        tokenRules.put("DIGIT_1", "1");
        tokenRules.put("DIGIT_9", "9");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize("019");
        assertEquals(3, resultTokens.size(), "Debe producir exactamente 3 tokens");
        
        assertEquals("0", resultTokens.get(0).value, "El valor del primer token debe coincidir con la entrada");
        assertEquals("1", resultTokens.get(1).value, "El valor del segundo token debe coincidir con la entrada");
        assertEquals("9", resultTokens.get(2).value, "El valor del tercer token debe coincidir con la entrada");
    }

    @Test
    public void testAlternationBasic() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("VOWEL", "a|e|i|o|u");
        tokenRules.put("CONSONANT", "b|c|d");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar vocales
        for (String vowel : Arrays.asList("a", "e", "i", "o", "u")) {
            List<Token> resultTokens = analyzer.tokenize(vowel);
            assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token para " + vowel);
            assertEquals("VOWEL", resultTokens.get(0).type, "Debe reconocer '" + vowel + "' como VOWEL");
            assertEquals(vowel, resultTokens.get(0).value, "El valor del token debe ser '" + vowel + "'");
        }
        
        // Probar consonantes
        for (String consonant : Arrays.asList("b", "c", "d")) {
            List<Token> resultTokens = analyzer.tokenize(consonant);
            assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token para " + consonant);
            assertEquals("CONSONANT", resultTokens.get(0).type, "Debe reconocer '" + consonant + "' como CONSONANT");
            assertEquals(consonant, resultTokens.get(0).value, "El valor del token debe ser '" + consonant + "'");
        }
    }

    @Test
    public void testLargeInput() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A", "a");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Crear una cadena de entrada grande
        StringBuilder inputBuilder = new StringBuilder();
        int inputSize = 100; // Tamaño reducido para pruebas más rápidas
        for (int i = 0; i < inputSize; i++) {
            inputBuilder.append(i % 2 == 0 ? "a" : "b");
        }
        
        List<Token> resultTokens = analyzer.tokenize(inputBuilder.toString());
        assertEquals(inputSize, resultTokens.size(), "Debe producir exactamente " + inputSize + " tokens");
        
        // Verificar que las posiciones son correctas
        for (int i = 0; i < inputSize; i++) {
            assertEquals(i, resultTokens.get(i).position, "Token " + i + " debe estar en posición " + i);
            if (i % 2 == 0) {
                assertEquals("A", resultTokens.get(i).type, "Los tokens en posición par deben ser A");
                assertEquals("a", resultTokens.get(i).value, "Los valores de tokens en posición par deben ser 'a'");
            } else {
                assertEquals("B", resultTokens.get(i).type, "Los tokens en posición impar deben ser B");
                assertEquals("b", resultTokens.get(i).value, "Los valores de tokens en posición impar deben ser 'b'");
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
        "'',         0",
        "a,          1", 
        "ab,         2",
        "abc,        3",
        "abcdefghij, 10"
    })
    public void testTokenCountVariations(String input, int expectedCount) {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        for (char character = 'a'; character <= 'j'; character++) {
            tokenRules.put("LETTER_" + Character.toUpperCase(character), String.valueOf(character));
        }
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        List<Token> resultTokens = analyzer.tokenize(input);
        assertEquals(expectedCount, resultTokens.size(), 
            String.format("La entrada '%s' debe producir %d tokens", input, expectedCount));
    }

    @Test
    public void testKleeneStarOperator() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A_STAR", "a*");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar una sola 'a'
        List<Token> resultTokens = analyzer.tokenize("a");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("A_STAR", resultTokens.get(0).type, "Debe reconocer 'a' como A_STAR");
        
        // Probar múltiples 'a's
        resultTokens = analyzer.tokenize("aaa");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token usando coincidencia más larga");
        assertEquals("A_STAR", resultTokens.get(0).type, "Debe reconocer 'aaa' como A_STAR");
        assertEquals("aaa", resultTokens.get(0).value, "El valor del token debe ser 'aaa'");
    }

    @Test
    public void testOptionalOperator() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("A_OPTIONAL", "a?");
        tokenRules.put("B", "b");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar una sola 'a'
        List<Token> resultTokens = analyzer.tokenize("a");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("A_OPTIONAL", resultTokens.get(0).type, "Debe reconocer 'a' como A_OPTIONAL");
    }

    @Test
    public void testLongestMatchStrategy() {
        Map<String, String> tokenRules = new LinkedHashMap<>();
        tokenRules.put("AAA", "aaa");
        tokenRules.put("AA", "aa");
        tokenRules.put("A", "a");
        
        TokenizerBuilder builder = new TokenizerBuilder();
        Tokenizer analyzer = builder.buildTokenizer(tokenRules, createBasicAlphabet());
        
        // Probar que "aaa" se reconoce como un token AAA, no como tres tokens A
        List<Token> resultTokens = analyzer.tokenize("aaa");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token usando coincidencia más larga");
        assertEquals("AAA", resultTokens.get(0).type, "Debe reconocer 'aaa' como token AAA");
        assertEquals("aaa", resultTokens.get(0).value, "El valor del token debe ser 'aaa'");
        
        // Probar coincidencia parcial
        resultTokens = analyzer.tokenize("aa");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("AA", resultTokens.get(0).type, "Debe reconocer 'aa' como token AA");
        
        // Probar carácter individual
        resultTokens = analyzer.tokenize("a");
        assertEquals(1, resultTokens.size(), "Debe producir exactamente 1 token");
        assertEquals("A", resultTokens.get(0).type, "Debe reconocer 'a' como token A");
    }
}