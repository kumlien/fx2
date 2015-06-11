package hoggaster.rules.indicators;

/**
 * Available granularities for candlesticks.
 */
public enum CandleStickGranularity {
	
	DAY("D"), MINUTE("M1");
	
	public final String oandaStyle;
	
	private CandleStickGranularity(String oandaStyle) {
		this.oandaStyle = oandaStyle;
	}
	
}
