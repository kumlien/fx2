package hoggaster.domain;

/**
 * Created by svante2 on 2015-11-27.
 */
public class InvalidInstrumentException extends RuntimeException {
    public InvalidInstrumentException(String msg) {
        super(msg);
    }
}
