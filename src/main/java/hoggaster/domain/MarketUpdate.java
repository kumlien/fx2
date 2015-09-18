package hoggaster.domain;

import hoggaster.rules.MarketUpdateType;

/**
 * Base class for updates coming from a market, like a new price tick or a new candle.
 * <p>
 * TODO Add instrument?
 */
public abstract class MarketUpdate {

    public abstract MarketUpdateType getType();

}
