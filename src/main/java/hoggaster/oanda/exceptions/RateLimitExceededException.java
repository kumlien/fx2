package hoggaster.oanda.exceptions;

/**
 * Created by svante.kumlien on 12.10.15.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
