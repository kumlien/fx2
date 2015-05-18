package hoggaster.oanda.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Instruments {
	
	private final List<OandaInstrument> instruments;

	@JsonCreator
	public Instruments(@JsonProperty(value="instruments")List<OandaInstrument> instruments) {
		this.instruments = instruments;
	}

	public List<OandaInstrument> getInstruments() {
		return instruments;
	}

	@Override
	public String toString() {
		return "Instruments [instruments=" + instruments + "]";
	}
}
