package hoggaster.rules;

import hoggaster.rules.conditions.Condition;

/**
 * Used to tell a {@link Condition} on what kind of events it should react.
 */
public enum MarketUpdateType {

    PRICE, ONE_MINUTE_CANDLE, ONE_DAY_CANDLE;

    public boolean isCandle() {
        return this == ONE_MINUTE_CANDLE || this == ONE_DAY_CANDLE;
    }
}
