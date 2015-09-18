package hoggaster.rules;

import java.util.function.BiFunction;

public enum Comparator implements BiFunction<Double, Double, Boolean> {
    LESS_THAN("<", (l, r) -> l < r),
    LESS_OR_EQUAL_THAN("<=", (l, r) -> l <= r),
    GREATER_THAN(">", (l, r) -> l > r),
    GREATER_OR_EQUAL_THAN(">=", (l, r) -> l >= r);

    private final String symbol;
    private final BiFunction<Double, Double, Boolean> biFunction;

    private Comparator(final String symbol, final BiFunction<Double, Double, Boolean> biFunction) {
        this.symbol = symbol;
        this.biFunction = biFunction;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public Boolean apply(Double t, Double u) {
        return biFunction.apply(t, u);
    }
}
