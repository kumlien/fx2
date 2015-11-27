package hoggaster.domain.trades.web;

/**
 * Created by svante2 on 2015-11-27.
 */
public class TradeNotFoundException extends RuntimeException {
    public TradeNotFoundException(String msg) {
        super(msg);
    }
}
