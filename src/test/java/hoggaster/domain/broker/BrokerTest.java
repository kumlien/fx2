package hoggaster.domain.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoggaster.oanda.responses.OandaClosedTradeReponse;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.time.*;

public class BrokerTest {

    @Test
    public void testIsIt1700InNewYorkNow() {
        ZoneId NEW_YORK_TIME_ZONE_ID = ZoneId.of("America/New_York");
        LocalTime closing = LocalTime.of(16, 59, 59);
        LocalTime nowInNewYork = LocalTime.now(NEW_YORK_TIME_ZONE_ID);
        System.out.println("" + closing + ", " + nowInNewYork);
        if (nowInNewYork.isAfter(closing)) {
            System.out.println("it's after closing time in NY");
            if (closing.getMinute() == nowInNewYork.getMinute()) {
                System.out.println("And we are withing one minute after closing, let's take a day price");
            }
        }

        ZoneOffset offset = NEW_YORK_TIME_ZONE_ID.getRules().getOffset(LocalDateTime.now());
        OffsetTime ot = OffsetTime.of(closing, offset);
    }



    @Test
    @Ignore
    public void testDesderializeCloseOrderResponse() throws IOException {
        String raw = "{\n" +
                "  \"id\": 10027047059,\n" +
                "  \"price\": 8.69185,\n" +
                "  \"profit\": -11.3332,\n" +
                "  \"instrument\": \"USD_SEK\",\n" +
                "  \"side\": \"buy\",\n" +
                "  \"time\": \"2015-12-02T14:18:06.000000Z\"\n" +
                "}";

        final OandaClosedTradeReponse response = new ObjectMapper().readValue(raw, OandaClosedTradeReponse.class);
    }

}
