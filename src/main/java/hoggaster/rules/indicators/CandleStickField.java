package hoggaster.rules.indicators;

import hoggaster.candles.BidAskCandle;

/**
 * Enum with possible values in a {@link BidAskCandle}
 * 
 * @author svante2
 *
 */
public enum CandleStickField {
    OPEN_BID, OPEN_ASK, HIGH_BID, HIGH_ASK, LOW_BID, LOW_ASK, CLOSE_BID, CLOSE_ASK;
}
