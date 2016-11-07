package hoggaster.rules.indicators;

/**
 * Created by svante2 on 2016-11-07.
 */
public class NoIndicatorAvailableException extends RuntimeException {
    public final Indicator indicator;

    public NoIndicatorAvailableException(Indicator indicator, String msg) {
        super(msg);
        this.indicator = indicator;
    }
}
