package hoggaster.domain;

/**
 * Thrown for example when we try to access an oanda account with a text string.
 *
 * Created by svante on 2016-11-08.
 */
public class InvalidResourceIdentifier extends RuntimeException {
    public InvalidResourceIdentifier(String message) {
        super(message);
    }
}
