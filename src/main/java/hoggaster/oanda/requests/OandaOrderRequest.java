package hoggaster.oanda.requests;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.time.Instant;

@SuppressWarnings("serial")
public class OandaOrderRequest extends LinkedMultiValueMap<String, String> {

    public OandaOrderRequest(
            CurrencyPair currencyPair,
            Long units,
            OrderSide side,
            OrderType type,
            Instant expiry,
            BigDecimal price,
            BigDecimal lowerBound,
            BigDecimal upperBound) {
        add("currencyPair", currencyPair.name());
        add("units", String.valueOf(units));
        add("side", side.name());
        add("type", type.name());
        if (null != expiry) {
            add("expiry", expiry.toString());
        }
        if (null != price) {
            add("price", String.valueOf(price));
        }
        if(null != lowerBound) {
            add("lowerBound", String.valueOf(lowerBound));
        }
        if(null != upperBound) {
            add("upperBound", String.valueOf(upperBound));
        }
    }
}

