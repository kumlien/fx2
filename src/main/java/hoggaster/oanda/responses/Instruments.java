package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Instruments {

    private final List<OandaInstrument> instruments;

    @JsonCreator
    public Instruments(@JsonProperty(value = "instruments") List<OandaInstrument> instruments) {
        this.instruments = instruments;
    }

    public List<OandaInstrument> getInstruments() {
        return instruments;
    }

    @Override
    public String toString() {
        return "Instruments [instruments=" + instruments + "]";
    }
}
