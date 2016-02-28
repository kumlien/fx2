package hoggaster.oanda.responses.positions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by svante.kumlien on 11.11.15.
 */
public class OandaPositions {

    public final List<OandaPosition> positions;

    @JsonCreator
    public OandaPositions(@JsonProperty(value = "positions") List<OandaPosition> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return "OandaPositions{" +
                "positions=" + positions +
                '}';
    }
}
