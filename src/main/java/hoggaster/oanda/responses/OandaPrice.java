package hoggaster.oanda.responses;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OandaPrice {
	
	public final String instrument;
	public final Double bid;
	public final Double ask;
	public final Date time;
	public final String status;
	
	@JsonCreator
	public OandaPrice(
			@JsonProperty(value="instrument")String instrument, 
			@JsonProperty(value="bid")Double bid, 
			@JsonProperty(value="ask")Double ask, 
			@JsonProperty(value="time")Date time,
			@JsonProperty(value="status", required=false)String status) {
		this.instrument = instrument;
		this.bid = bid;
		this.ask = ask;
		this.time = time;
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Price [instrument=").append(instrument)
				.append(", bid=").append(bid).append(", ask=").append(ask)
				.append(", time=").append(time).append(", status=")
				.append(status).append("]");
		return builder.toString();
	}
	
	
}
