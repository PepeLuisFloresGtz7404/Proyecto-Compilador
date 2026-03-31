package com.compiler.parser.lr;

import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;

/**
 * Represents an LR(1) item: a production with a dot position and a lookahead symbol.
 */
public class LR1Item {
    public final Production production;
    public final int dotPosition;
    public final Symbol lookahead; // single lookahead symbol for LR(1)

    public LR1Item(Production production, int dotPosition, Symbol lookahead) {
        if (production == null) throw new IllegalArgumentException("production cannot be null");
        if (dotPosition < 0 || dotPosition > production.right.size())
            throw new IllegalArgumentException("dotPosition out of bounds");
        if (lookahead == null) throw new IllegalArgumentException("lookahead cannot be null");
        this.production = production;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
    }

    /**
     * Returns the symbol after the dot, or null if the dot is at the end.
     */
    public Symbol getSymbolAfterDot() {
        if (dotPosition < production.right.size()) {
            return production.right.get(dotPosition);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LR1Item)) return false;
        LR1Item other = (LR1Item) obj;
        return production.equals(other.production) && dotPosition == other.dotPosition && lookahead.equals(other.lookahead);
    }

    @Override
    public int hashCode() {
        int result = production.hashCode();
        result = 31 * result + dotPosition;
        result = 31 * result + lookahead.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(production.left.name).append(" -> ");
        for (int i = 0; i < production.right.size(); i++) {
            if (i == dotPosition) sb.append("• ");
            sb.append(production.right.get(i).name).append(' ');
        }
        if (dotPosition == production.right.size()) sb.append("•");
        sb.append(", ").append(lookahead.name).append(']');
        return sb.toString();
    }
}