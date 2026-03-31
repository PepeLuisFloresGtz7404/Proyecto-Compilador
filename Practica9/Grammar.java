/**
 * Provides the representation and parsing logic for a context-free grammar (CFG).
 * 
 * A Grammar consists of non-terminals, terminals, productions, and a start symbol.
 * It can be constructed from a string definition using a simple BNF-like format.
 * 
 * Example grammar definition:
 * S -> A B | ε
 * A -> a
 * B -> b
 */
package com.compiler.parser.grammar;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a complete context-free grammar.
 */
public class Grammar {
    /**
     * Set of non-terminal symbols in the grammar.
     */
    private final Set<Symbol> nonTerminals;
    /**
     * Set of terminal symbols in the grammar.
     */
    private final Set<Symbol> terminals;
    /**
     * List of all productions in the grammar.
     */
    private final List<Production> productions;
    /**
     * The start symbol of the grammar.
     */
    private final Symbol startSymbol;

    /**
     * Constructs a Grammar from a string definition.
     *
     * @param grammarDefinition The grammar definition in BNF-like format.
     * @throws IllegalArgumentException if the definition is null, empty, or invalid.
     */
    public Grammar(String grammarDefinition) {
        if (grammarDefinition == null || grammarDefinition.trim().isEmpty()) {
            throw new IllegalArgumentException("Grammar definition cannot be null or empty.");
        }

        GrammarData data = parseGrammarDefinition(grammarDefinition);

        this.nonTerminals = java.util.Collections.unmodifiableSet(data.nonTerminals);
        this.terminals = java.util.Collections.unmodifiableSet(data.terminals);
        this.productions = java.util.Collections.unmodifiableList(data.productions);
        this.startSymbol = data.startSymbol;

        validateProductions(this.productions);
    }

    /**
     * Helper class to hold parsed grammar data.
     */
    private static class GrammarData {
        Set<Symbol> nonTerminals;
        Set<Symbol> terminals;
        List<Production> productions;
        Symbol startSymbol;
    }

    /**
     * Parses the grammar definition string and builds the grammar data.
     *
     * @param grammarDefinition The grammar definition string.
     * @return GrammarData containing parsed symbols and productions.
     * @throws IllegalArgumentException if the definition is invalid.
     */
    private GrammarData parseGrammarDefinition(String grammarDefinition) {
        Set<String> nonTerminalNames = new java.util.LinkedHashSet<>();
        Set<String> terminalNames = new java.util.LinkedHashSet<>();
        List<Production> tempProductions = new java.util.ArrayList<>();
        Map<String, List<List<String>>> productionMap = new java.util.LinkedHashMap<>();

        parseLines(grammarDefinition, nonTerminalNames, productionMap);
        collectTerminals(nonTerminalNames, productionMap, terminalNames);

        Map<String, Symbol> symbolMap = buildSymbolMap(nonTerminalNames, terminalNames);
        tempProductions.addAll(buildProductions(productionMap, symbolMap));

        GrammarData data = new GrammarData();
        data.nonTerminals = nonTerminalNames.stream().map(symbolMap::get).collect(java.util.stream.Collectors.toSet());
        data.terminals = terminalNames.stream().map(symbolMap::get).collect(java.util.stream.Collectors.toSet());
        data.productions = tempProductions;
        data.startSymbol = symbolMap.get(nonTerminalNames.iterator().next());
        return data;
    }

    /**
     * Parses each line of the grammar definition and populates non-terminals and productions.
     *
     * @param grammarDefinition The grammar definition string.
     * @param nonTerminalNames Set to collect non-terminal names.
     * @param productionMap Map to collect productions for each non-terminal.
     * @throws IllegalArgumentException if a line is malformed.
     */
    private void parseLines(String grammarDefinition, Set<String> nonTerminalNames, Map<String, List<List<String>>> productionMap) {
        String[] lines = grammarDefinition.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("->");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid production line: " + line);
            }
            String lhs = parts[0].trim();
            nonTerminalNames.add(lhs);

            String rhs = parts[1].trim();
            String[] alternatives = rhs.split("\\|");
            for (String alt : alternatives) {
                List<String> symbols = new java.util.ArrayList<>();
                for (String symbol : alt.trim().split("\\s+")) {
                    if (!symbol.isEmpty()) {
                        symbols.add(symbol);
                    }
                }
                productionMap.computeIfAbsent(lhs, k -> new java.util.ArrayList<>()).add(symbols);
            }
        }
    }

    /**
     * Collects terminal symbols from the productions.
     *
     * @param nonTerminalNames Set of non-terminal names.
     * @param productionMap Map of productions.
     * @param terminalNames Set to collect terminal names.
     */
    private void collectTerminals(Set<String> nonTerminalNames, Map<String, List<List<String>>> productionMap, Set<String> terminalNames) {
        for (List<List<String>> rhsList : productionMap.values()) {
            for (List<String> prod : rhsList) {
                for (String symbol : prod) {
                    if (!nonTerminalNames.contains(symbol) && !"ε".equals(symbol)) {
                        terminalNames.add(symbol);
                    }
                }
            }
        }
    }

    /**
     * Builds a map from symbol names to Symbol objects.
     *
     * @param nonTerminalNames Set of non-terminal names.
     * @param terminalNames Set of terminal names.
     * @return Map from symbol name to Symbol object.
     */
    private Map<String, Symbol> buildSymbolMap(Set<String> nonTerminalNames, Set<String> terminalNames) {
        Map<String, Symbol> symbolMap = new java.util.HashMap<>();
        for (String nt : nonTerminalNames) {
            symbolMap.put(nt, new Symbol(nt, SymbolType.NON_TERMINAL));
        }
        for (String t : terminalNames) {
            symbolMap.put(t, new Symbol(t, SymbolType.TERMINAL));
        }
        symbolMap.put("ε", new Symbol("ε", SymbolType.TERMINAL)); // epsilon
        return symbolMap;
    }

    /**
     * Builds the list of Production objects from the production map and symbol map.
     *
     * @param productionMap Map of productions.
     * @param symbolMap Map from symbol name to Symbol object.
     * @return List of Production objects.
     * @throws IllegalArgumentException if a symbol is undefined.
     */
    private List<Production> buildProductions(Map<String, List<List<String>>> productionMap, Map<String, Symbol> symbolMap) {
        List<Production> prodList = new java.util.ArrayList<>();
        for (String lhs : productionMap.keySet()) {
            Symbol left = symbolMap.get(lhs);
            for (List<String> rhs : productionMap.get(lhs)) {
                List<Symbol> right = new java.util.ArrayList<>();
                for (String s : rhs) {
                    Symbol sym = symbolMap.get(s);
                    if (sym == null) {
                        throw new IllegalArgumentException("Undefined symbol: " + s);
                    }
                    right.add(sym);
                }
                prodList.add(new Production(left, right));
            }
        }
        return prodList;
    }

    /**
     * Validates that all productions have defined symbols.
     *
     * @param productions List of productions to validate.
     * @throws IllegalArgumentException if a production contains undefined symbols.
     */
    private void validateProductions(List<Production> productions) {
        for (Production p : productions) {
            if (p.getLeft() == null || p.getRight().contains(null)) {
                throw new IllegalArgumentException("Production contains undefined symbol.");
            }
        }
    }

    /**
     * Returns the set of non-terminal symbols.
     * @return Unmodifiable set of non-terminals.
     */
    public Set<Symbol> getNonTerminals() {
        return nonTerminals;
    }

    /**
     * Returns the set of terminal symbols.
     * @return Unmodifiable set of terminals.
     */
    public Set<Symbol> getTerminals() {
        return terminals;
    }

    /**
     * Returns the list of productions.
     * @return Unmodifiable list of productions.
     */
    public List<Production> getProductions() {
        return productions;
    }

    /**
     * Returns the start symbol of the grammar.
     * @return The start symbol.
     */
    public Symbol getStartSymbol() {
        return startSymbol;
    }
}
