package hoggaster.domain;

/**
 * Created by svante.kumlien on 14.10.15.
 */
public class NoSuchCurrencyPairException extends RuntimeException {
    public NoSuchCurrencyPairException(String msg) {
        super(msg);
    }
}
