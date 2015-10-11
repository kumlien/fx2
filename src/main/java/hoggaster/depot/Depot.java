package hoggaster.depot;

import hoggaster.domain.Instrument;

/**
 * Created by svante2 on 2015-10-11.
 */
public interface Depot {
    void sell(Instrument instrument, String robotId);

    void buy(Instrument instrument, String robotId);
}
