package hoggaster.rules;

import java.math.BigDecimal;
import java.util.function.BiFunction;

public enum Comparator implements BiFunction<BigDecimal, BigDecimal, Boolean> {
    LESS_THAN("<", (l, r) -> l.compareTo(r) < 0),
    LESS_OR_EQUAL_THAN("<=", (l, r) -> l.compareTo(r) <= 0),
    GREATER_THAN(">", (l, r) -> l.compareTo(r) > 0),
    GREATER_OR_EQUAL_THAN(">=", (l, r) -> l.compareTo(r) >= 0);

    private final String symbol;
    private final BiFunction<BigDecimal, BigDecimal, Boolean> biFunction;

    private Comparator(final String symbol, final BiFunction<BigDecimal, BigDecimal, Boolean> biFunction) {
        this.symbol = symbol;
        this.biFunction = biFunction;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public Boolean apply(BigDecimal t, BigDecimal u) {
        return biFunction.apply(t, u);
    }
}
