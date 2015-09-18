package hoggaster.rules.indicators;

import hoggaster.rules.MarketUpdateType;

/**
 * Available granularities for candlesticks.
 */
public enum CandleStickGranularity {

    END_OF_DAY("D", MarketUpdateType.ONE_DAY_CANDLE), MINUTE("M1", MarketUpdateType.ONE_MINUTE_CANDLE);

    public final String oandaStyle;

    public final MarketUpdateType type;

    private CandleStickGranularity(String oandaStyle, MarketUpdateType type) {
        this.oandaStyle = oandaStyle;
        this.type = type;
    }
}
