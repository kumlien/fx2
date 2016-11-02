package hoggaster.domain.robot;

import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.rules.conditions.Condition;
import hoggaster.rules.indicators.RSIIndicator;
import hoggaster.rules.indicators.candles.CandleStickField;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import hoggaster.talib.TALibService;
import hoggaster.talib.TAResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * God knows what this is... Some kind of context for each new price/candle.
 * Contains info needed by a {@link Condition} to evaluate if it's is positive
 * or not.
 * <p>
 * Collection of instances which might come in handy for {@link Condition}s when
 * evaluating. It also used as a way for a {@link Condition} to signal to the
 * {@link Robot} that it's evaluation was positive.
 */
public class RobotExecutionContext {

    public final MarketUpdate marketUpdate;

    public final CurrencyPair currencyPair;

    private final List<Condition> positiveOpenTradeConditions = new ArrayList<>();

    private final List<Condition> positiveCloseTradeConditions = new ArrayList<>();

    private final List<Condition> negativeOpenTradeConditions = new ArrayList<>();

    private final List<Condition> negativeCloseTradeConditions = new ArrayList<>();

    private final TALibService taLibService;

    private final CandleService bidAskCandleService;

    public RobotExecutionContext(MarketUpdate marketUpdate, CurrencyPair currencyPair, TALibService taLibService, CandleService bidAskCandleService) {
        Preconditions.checkNotNull(marketUpdate);
        Preconditions.checkNotNull(taLibService);
        Preconditions.checkNotNull(bidAskCandleService);
        this.marketUpdate = marketUpdate;
        this.currencyPair = currencyPair;
        this.taLibService = taLibService;
        this.bidAskCandleService = bidAskCandleService;
    }

    public void addPositiveOpenTradeCondition(Condition condition) {
        positiveOpenTradeConditions.add(condition);
    }

    public void addPositiveCloseTradeAction(Condition condition) {
        positiveCloseTradeConditions.add(condition);
    }

    public void addNegativeOpenTradeCondition(Condition condition) {
        negativeOpenTradeConditions.add(condition);
    }

    public void addNegativeCloseTradeCondition(Condition condition) {
        negativeCloseTradeConditions.add(condition);
    }


    /**
     * Calculate a sma using the specified parameters.
     * Use the {@link CandleService} to fetch the latest candles.
     *
     * @param granularity
     * @param dataPoints
     * @param field
     * @param periods
     * @return The sma for the last value in the series.
     */
    public TAResult getSMA(CandleStickGranularity granularity, int dataPoints, CandleStickField field, int periods) {
        List<Candle> candles = bidAskCandleService.getLatestCandles(currencyPair, granularity, dataPoints);
        List<Double> values = candles.stream().map(bac -> bac.getValue(field)).map(BigDecimal::doubleValue).collect(toList());
        return taLibService.sma(values, periods);
    }

    public List<Condition> getPositiveOpenTradeConditions() {
        return positiveOpenTradeConditions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RobotExecutionContext [marketUpdate=").append(marketUpdate).append(", currencyPair=").append(currencyPair).append(", positiveConditions=").append(positiveOpenTradeConditions).append("]");
        return builder.toString();
    }


    public List<Condition> getPositiveCloseTradeConditions() {
        return positiveCloseTradeConditions;
    }


    /**
     * Used by the {@link RSIIndicator}
     * First fetch the candles from the {@link CandleService}, then use the {@link TALibService} to calculate
     * the RSI values.
     *
     * @param granularity
     * @param periods
     * @param dataPointsNeeded
     * @param field
     * @return A {@link TAResult}
     */
    public TAResult getRSI(CandleStickGranularity granularity, int periods, int dataPointsNeeded, CandleStickField field) {
        List<Candle> candles = bidAskCandleService.getLatestCandles(currencyPair, granularity, dataPointsNeeded);
        List<Double> values = candles.stream()
                .map(candle -> candle.getValue(field))
                .map(BigDecimal::doubleValue)
                .collect(toList());
        return taLibService.rsi(values, periods);
    }

}
