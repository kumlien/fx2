package hoggaster.domain;

/**
 * Created by svante2 on 2015-11-27.
 */
public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String msg) {
        super(msg);
    }
}
