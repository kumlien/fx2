package hoggaster.domain.depots.web;

/**
 * Created by svante.kumlien on 02.12.15.
 */
public class DepotNotFoundException extends RuntimeException {
    public DepotNotFoundException(String msg) {
        super(msg);
    }

}
