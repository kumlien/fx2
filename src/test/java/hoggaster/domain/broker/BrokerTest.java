package hoggaster.domain.broker;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.Test;

public class BrokerTest {

    @Test
    public void testIsIt1700InNewYorkNow() {
	ZoneId NEW_YORK_TIME_ZONE_ID = ZoneId.of("America/New_York");
	LocalTime closing = LocalTime.of(16, 59, 59);
	LocalTime nowInNewYork = LocalTime.now(NEW_YORK_TIME_ZONE_ID);
	System.out.println("" + closing + ", " +  nowInNewYork);
	if(nowInNewYork.isAfter(closing)) {
	    System.out.println("it's after closing time in NY");
	    if(closing.getMinute() == nowInNewYork.getMinute()) {
		System.out.println("And we are withing one minute after closing, let's take a day price");
	    }
	}

	ZoneOffset offset = NEW_YORK_TIME_ZONE_ID.getRules().getOffset(LocalDateTime.now());
	OffsetTime ot = OffsetTime.of(closing, offset);
    }

}
