package hoggaster.oanda.requests;

import hoggaster.domain.Instrument;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Instant;

@SuppressWarnings("serial")
public class OandaOrderRequest extends LinkedMultiValueMap<String, String> {

    public OandaOrderRequest(
            Instrument instrument,
            Long units,
            OrderSide side,
            OrderType type,
            Instant expiry,
            Double price) {
        add("instrument", instrument.name());
        add("units", String.valueOf(units));
        add("side", side.name());
        add("type", type.name());
        if (null != expiry) {
            add("expiry", expiry.toString());
        }
        if (null != price) {
            add("price", String.valueOf(price));
        }
    }
}
