package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OandaInstrument {
	
	public final String instrument;
	public final String displayName;
	public final Double pip;
	public final Long maxTradeUnits;
	public final Double precision;
	public final Long maxTrailingStop;
	public final Long minTrailingStop;
	public final Double marginRate;
	public final Boolean halted;
	
	@JsonCreator
	public OandaInstrument(
			@JsonProperty(value="instrument") String instrument, 
			@JsonProperty(value="displayName")String displayName, 
			@JsonProperty(value="pip")Double pip,
			@JsonProperty(value="maxTradeUnits")Long maxTradeUnits,
			@JsonProperty(value="precision")Double precision,
			@JsonProperty(value="maxTrailingStop")Long maxTrailingStop,
			@JsonProperty(value="minTrailingStop")Long minTrailingStop,
			@JsonProperty(value="marginRate")Double marginRate,
			@JsonProperty(value="halted")Boolean halted) {
		this.instrument = instrument;
		this.displayName = displayName;
		this.pip = pip;
		this.maxTradeUnits = maxTradeUnits;
		this.precision = precision;
		this.maxTrailingStop = maxTrailingStop;
		this.minTrailingStop = minTrailingStop;
		this.marginRate = marginRate;
		this.halted = halted;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Instrument [instrument=").append(instrument)
				.append(", displayName=").append(displayName).append(", pip=")
				.append(pip).append(", maxTradeUnits=").append(maxTradeUnits)
				.append(", precision=").append(precision)
				.append(", maxTrailingStop=").append(maxTrailingStop)
				.append(", minTrailingStop=").append(minTrailingStop)
				.append(", marginRate=").append(marginRate).append(", halted=")
				.append(halted).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instrument == null) ? 0 : instrument.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OandaInstrument other = (OandaInstrument) obj;
		if (instrument == null) {
			if (other.instrument != null)
				return false;
		} else if (!instrument.equals(other.instrument))
			return false;
		return true;
	}



}
