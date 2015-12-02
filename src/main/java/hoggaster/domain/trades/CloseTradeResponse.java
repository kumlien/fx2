package hoggaster.domain.trades;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by svante.kumlien on 02.12.15.
 */
public class CloseTradeResponse {

    public final Broker broker;

    public final String brokerTradeId;

    public final BigDecimal price;

    public final CurrencyPair currencyPair;

    public final BigDecimal profit;

    public final OrderSide side;

    public final Instant time;


    public CloseTradeResponse(Broker broker, String brokerTradeId, BigDecimal price, CurrencyPair currencyPair, BigDecimal profit, OrderSide side, Instant time) {
        this.broker = broker;
        this.brokerTradeId = brokerTradeId;
        this.price = price;
        this.currencyPair = currencyPair;
        this.profit = profit;
        this.side = side;
        this.time = time;
    }
}
