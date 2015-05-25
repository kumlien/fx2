package hoggaster.candles;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BidAskCandleRepo extends MongoRepository<BidAskCandle, String> {
	
	
}
