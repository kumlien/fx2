package hoggaster.robot;

import com.google.common.base.Preconditions;
import hoggaster.candles.Candle;
import hoggaster.candles.CandleService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.MarketUpdate;
import hoggaster.rules.Condition;
import hoggaster.rules.indicators.CandleStickField;
import hoggaster.rules.indicators.CandleStickGranularity;
import hoggaster.rules.indicators.RSIIndicator;
import hoggaster.talib.TALibService;
import hoggaster.talib.TAResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final List<Condition> positiveBuyConditions = new ArrayList<>();

    private final List<Condition> positiveSellConditions = new ArrayList<>();

    private final List<Condition> negativeBuyConditions = new ArrayList<>();

    private final List<Condition> negativeSellConditions = new ArrayList<>();

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

    public void addPositiveBuyAction(Condition condition) {
        positiveBuyConditions.add(condition);
    }

    public void addPositiveSellAction(Condition condition) {
        positiveSellConditions.add(condition);
    }

    public void addNegativeBuyAction(Condition condition) {
        negativeBuyConditions.add(condition);
    }

    public void addNegativeSellAction(Condition condition) {
        negativeSellConditions.add(condition);
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
        List<Double> values = candles.stream().map(bac -> bac.getValue(field)).map(BigDecimal::doubleValue).collect(Collectors.toList());
        return taLibService.sma(values, periods);
    }

    public List<Condition> getPositiveBuyConditions() {
        return positiveBuyConditions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RobotExecutionContext [marketUpdate=").append(marketUpdate).append(", currencyPair=").append(currencyPair).append(", positiveConditions=").append(positiveBuyConditions).append("]");
        return builder.toString();
    }


    public List<Condition> getPositiveSellConditions() {
        return positiveSellConditions;
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
                .collect(Collectors.toList());
        return taLibService.rsi(values, periods);
    }

}
