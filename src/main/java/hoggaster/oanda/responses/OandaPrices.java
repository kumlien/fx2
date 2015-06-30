package hoggaster.oanda.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OandaPrices {
	
	public final List<OandaPrice> prices;

	@JsonCreator
	public OandaPrices(@JsonProperty(value="prices")List<OandaPrice> prices) {
		this.prices = prices;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Prices [prices=").append(prices).append("]");
		return builder.toString();
	}
	
	
}
