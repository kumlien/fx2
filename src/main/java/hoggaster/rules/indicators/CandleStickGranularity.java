package hoggaster.rules.indicators;

public enum CandleStickGranularity {
	
	DAY("D");
	
	public final String oandaStyle;
	
	private CandleStickGranularity(String oandaStyle) {
		this.oandaStyle = oandaStyle;
	}
	
}
