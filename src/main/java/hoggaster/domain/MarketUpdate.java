package hoggaster.domain;

import hoggaster.rules.MarketUpdateType;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Base class for updates coming from a market, like a new price tick or a new candle.
 * <p>
 * TODO Add currencyPair?
 */
public abstract class MarketUpdate {

    public abstract MarketUpdateType getType();

    /**
     * @return A function calculating the value for the upper bound in an order request based on this market update
     */
    public abstract Function<BigDecimal, BigDecimal> calcUpperBound();

}
