package hoggaster.domain.trades;

import hoggaster.domain.CurrencyPair;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

/**
 * @author svante
 */
public interface TradeRepo extends MongoRepository<Trade, String> {

    Trade findByBrokerId(String brokerId);

    Collection<Trade> findByDepotIdAndStatus(String brokerId, TradeStatus status);

    Collection<Trade> findByInstrumentAndRobotId(CurrencyPair instrument, String robotId);
}
