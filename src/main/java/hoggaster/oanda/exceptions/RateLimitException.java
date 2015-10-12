package hoggaster.oanda.exceptions;

/**
 * Created by svante.kumlien on 12.10.15.
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
