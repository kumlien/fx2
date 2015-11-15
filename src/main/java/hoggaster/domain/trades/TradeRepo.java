package hoggaster.domain.trades;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

/**
 * Created by svante2 on 2015-11-15.
 */
public interface TradeRepo extends MongoRepository<Trade, String> {

    Trade findByBrokerId(String brokerId);

    Collection<Trade> findByDepotIdAndStatus(String brokerId, TradeStatus status);
}
