package hoggaster.akka.messages;

import hoggaster.domain.Instrument;

/**
 * Created by svante2 on 2015-10-07.
 */
public class BuyRequestFromRobot {

    private final Instrument instrument;


    public BuyRequestFromRobot(Instrument instrument) {
        this.instrument = instrument;
    }
}
