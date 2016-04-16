package hoggaster.oanda.streaming.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by svante2 on 2016-04-15.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventContainer {

    @JsonProperty("transaction")
    public Transaction transaction;
}
