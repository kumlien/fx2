package hoggaster.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public enum Broker {
    
    
	
	OANDA (null, null);
	
//	IG (
//		OffsetTime.of(17, 0, 0, 0, ZoneOffset.of("America/New_York")), 
//		OffsetTime.of(16, 59, 59, 0, ZoneOffset.of("America/New_York"));
	
	
	public final OffsetTime opensAt;
	public final OffsetTime closesAt;
	
	
	private Broker(OffsetTime opensAt, OffsetTime closesAt) {
	    ZoneId NEW_YORK_TIME_ZONE_ID = ZoneId.of("America/New_York");
	    LocalTime opening = LocalTime.of(17, 00, 00);
	    ZoneOffset offset = NEW_YORK_TIME_ZONE_ID.getRules().getOffset(LocalDateTime.now());
	    OffsetTime ot = OffsetTime.of(opening, offset);
	    
	    this.opensAt = opensAt;
	    this.closesAt = closesAt;
	}

}
