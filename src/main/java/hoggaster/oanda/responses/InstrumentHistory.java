package hoggaster.oanda.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InstrumentHistory {
	
	public final String instrument;
	public final String granularity;
	public final List<OandaCandle> candles;
	
	@JsonCreator
	public InstrumentHistory(@JsonProperty(value="instrument") String instrument, @JsonProperty(value="granularity")String granularity,
			@JsonProperty(value="candles")List<OandaCandle> candles) {
		this.instrument = instrument;
		this.granularity = granularity;
		this.candles = candles;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentHistory [instrument=").append(instrument)
				.append(", granularity=").append(granularity)
				.append(", candles=").append(candles).append("]");
		return builder.toString();
	}
}
