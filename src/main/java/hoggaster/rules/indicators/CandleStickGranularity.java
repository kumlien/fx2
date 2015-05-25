package hoggaster.rules.indicators;

/**
 * Available granularities for candlesticks.
 */
public enum CandleStickGranularity {
	
	DAY("D");
	
	public final String oandaStyle;
	
	private CandleStickGranularity(String oandaStyle) {
		this.oandaStyle = oandaStyle;
	}
	
}
