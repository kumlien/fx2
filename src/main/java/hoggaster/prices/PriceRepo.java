package hoggaster.prices;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceRepo extends MongoRepository<Price, Long> {
	
	
}
