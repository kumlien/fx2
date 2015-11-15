package hoggaster.oanda.exceptions;

/**
 * Created by svante.kumlien on 12.10.15.
 */
public class TradingHaltedException extends RuntimeException {

    public TradingHaltedException(String message) {
        super(message);
    }
}
