package hoggaster.depot;

import hoggaster.domain.Instrument;
import hoggaster.domain.MarketUpdate;

/**
 * Created by svante2 on 2015-10-11.
 */
public interface Depot {
    void sell(Instrument instrument, int requestedUnits, String robotId);

    void buy(Instrument instrument, int requestedUnits, MarketUpdate marketUpdate, String robotId);
}
