package hoggaster.user;

import hoggaster.domain.Instrument;

/**
 * Represents the ownership of an instrument
 */
public class InstrumentOwnership {

	public final Instrument instrument;
	
	private Long number;

	public InstrumentOwnership(Instrument instrument, Long number) {
		this.instrument = instrument;
		this.number = number;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public Long getNumber() {
		return number;
	}

}
